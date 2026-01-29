package com.embrace.cup.zoo;

import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;
import java.sql.*;
import java.util.*;

public class Db {

    // =========================
    // 初始化 DataSource
    // =========================
    private static DataSource dataSource;
    private static final ThreadLocal<TxContext> TX = new ThreadLocal<>();

    static {
        init();
    }

    private static void init() {
        try {
            HikariDataSource ds = new HikariDataSource();
            ds.setJdbcUrl(Config.get("db.url"));
            ds.setUsername(Config.get("db.username"));
            ds.setPassword(Config.get("db.password"));
            ds.setDriverClassName(Config.get("db.driver"));
            ds.setMaximumPoolSize(Integer.parseInt(Config.get("db.maxPoolSize")));
            ds.setPoolName("AppPool");

            dataSource = ds;
        } catch (Exception e) {
            throw new RuntimeException("Db init failed", e);
        }
    }

    // =========================
    // Transaction Context
    // =========================
    static class TxContext {
        Connection conn;
        int level = 0;
        boolean rollbackOnly = false;
        Deque<Savepoint> savepoints = new ArrayDeque<>();
    }

    // =========================
    // Transaction Control
    // =========================
    private static void begin() {
        try {
            TxContext ctx = TX.get();

            if (ctx == null) {
                Connection conn = dataSource.getConnection();
                conn.setAutoCommit(false);

                ctx = new TxContext();
                ctx.conn = conn;
                ctx.level = 1;

                TX.set(ctx);
            } else {
                ctx.level++;
                Savepoint sp = ctx.conn.setSavepoint("SP_" + ctx.level);
                ctx.savepoints.push(sp);
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static void commit() {
        TxContext ctx = TX.get();
        if (ctx == null) return;

        ctx.level--;

        if (ctx.level > 0) {
            ctx.savepoints.pop(); // 释放最近的 savepoint
            return;
        }

        try {
            if (ctx.rollbackOnly) {
                ctx.conn.rollback();
            } else {
                ctx.conn.commit();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            close(ctx.conn);
            TX.remove();
        }
    }

    private static void rollback() {
        TxContext ctx = TX.get();
        if (ctx == null) return;

        try {
            if (ctx.level > 1 && !ctx.savepoints.isEmpty()) {
                // 回滚到最近 savepoint
                ctx.conn.rollback(ctx.savepoints.pop());
            } else {
                ctx.conn.rollback();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            ctx.rollbackOnly = true;
            ctx.level--;

            if (ctx.level <= 0) {
                close(ctx.conn);
                TX.remove();
            }
        }
    }
    
    private static void close(Connection conn) {
        try {
            conn.setAutoCommit(true);
            conn.close();
        } catch (Exception ignored) {}
    }
    
    // Transaction 
    public static void tx(Runnable r) {
        begin();
        try {
            r.run();
            commit();
        } catch (Throwable t) {
            rollback();
            throw t;
        }
    }

    // =========================
    // JDBC Template like API
    // =========================
    public static int update(String sql, Object... params) {
        TxContext ctx = TX.get();
        if (ctx != null){
            try (PreparedStatement ps = prepare(ctx.conn, sql, params)) {
                return ps.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
        try (Connection conn = dataSource.getConnection();
            PreparedStatement ps = prepare(conn, sql, params)) {
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static Map<String, Object> queryOne(String sql, Object... params) {
        List<Map<String, Object>> list = queryList(sql, params);
        return list.isEmpty() ? null : list.get(0);
    }

    public static List<Map<String, Object>> queryList(String sql, Object... params) {
        TxContext ctx = TX.get();
        if (ctx != null) {
            try (PreparedStatement ps = prepare(ctx.conn, sql, params);
                    ResultSet rs = ps.executeQuery()) {
                return columnsToMapList(rs);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        } else {
            try (Connection conn = dataSource.getConnection();
                    PreparedStatement ps = prepare(conn, sql, params);
                    ResultSet rs = ps.executeQuery()) {
                return columnsToMapList(rs);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static <T> T queryOne(RowMapper<T> mapper, String sql, Object... params) {
        List<T> list = queryList(mapper, sql, params);
        return list.isEmpty() ? null : list.get(0);
    }

    public static <T> List<T> queryList(RowMapper<T> mapper, String sql, Object... params) {
        TxContext ctx = TX.get();
        if (ctx != null) {
            try (PreparedStatement ps = prepare(ctx.conn, sql, params);
                    ResultSet rs = ps.executeQuery()) {
                return columnsToModeList(rs, mapper);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        } else {
            try (Connection conn = dataSource.getConnection();
                    PreparedStatement ps = prepare(conn, sql, params);
                    ResultSet rs = ps.executeQuery()) {
                return columnsToModeList(rs, mapper);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
    
    // =========================
    // private methods for sql run
    // =========================
    private static PreparedStatement prepare(Connection conn, String sql, Object... params) 
            throws SQLException {
        
        PreparedStatement ps = conn.prepareStatement(sql);

        for (int i = 0; i < params.length; i++) {
            ps.setObject(i + 1, params[i]);
        }
        return ps;
    }

    private static List<Map<String, Object>> columnsToMapList(ResultSet rs) 
        throws SQLException {
        
        List<Map<String, Object>> list = new ArrayList<>();
        ResultSetMetaData meta = rs.getMetaData();
        int cols = meta.getColumnCount();

        while (rs.next()) {
            Map<String, Object> row = new LinkedHashMap<>();
            for (int i = 1; i <= cols; i++) {
                row.put(meta.getColumnLabel(i), rs.getObject(i));
            }
            list.add(row);
        } 
        return list;
    }

    private static <T> List<T> columnsToModeList(ResultSet rs, RowMapper<T> mapper) 
        throws SQLException {

        List<T> list = new ArrayList<>();
        while (rs.next()) {
            list.add(mapper.map(rs));
        }
        return list;
    }

    // end of class
}
