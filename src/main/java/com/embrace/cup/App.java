package com.embrace.cup;

import java.io.File;
import java.io.IOException; 
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.stream.Stream;
import java.net.URL;

import org.apache.catalina.WebResourceRoot;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.webresources.StandardRoot;

import com.embrace.cup.zoo.ConfigHolder;
import com.embrace.cup.zoo.JobExecutor;
import com.embrace.cup.zoo.Log;

// 删除错误的导入
// import io.jsonwebtoken.io.IOException;

public class App {
    public static void main(String[] args) throws Exception {
        
        // 例：支持参数控制
        if (args.length > 0 && args[0].startsWith("job:") && args[0].length() > 4) {
            runJob(args);
        } else {
            var tomcat = createTomcat();
            tomcat.start();
            tomcat.getServer().await();
        }
    }

    static void runJob(String[] args) {
        
        Log.info("main", "Run job ...");
        String jobName = args[0].substring(4).trim();
        String classFullName = ConfigHolder.APP_PACKAGE 
                                + "." + ConfigHolder.JOB_PACKAGE 
                                + "." + jobName;
        String [] jobParams = Arrays.copyOfRange(args, 1, args.length);
        try {
            Log.info("main", classFullName);
            Class<?> clazz = Class.forName(classFullName);
            Object obj = clazz.getDeclaredConstructor().newInstance();
            if (obj instanceof JobExecutor job) {
                Log.info("main", "Job starting ...");
                job.execute(jobParams);
                Log.info("main", "Job finished ");
            } else {
                throw new RuntimeException("Class is not JobExecutor");
            }
            
        } catch (ClassNotFoundException cnfe) {
            Log.info("main", "job class not found");
            cnfe.printStackTrace();
        } catch (NoSuchMethodException nsme) {
            Log.info("main", "job method not found");
            nsme.printStackTrace();
        } catch (SecurityException e3) {
            Log.info("main", "job method security error");
            e3.printStackTrace();
        } catch (InvocationTargetException ite) {
            Log.info("main", "job execute error");
            Throwable real = ite.getTargetException();
            ite.printStackTrace();
            real.printStackTrace();
        } catch (IllegalAccessException iae) {
            Log.info("main", "job access error");
            iae.printStackTrace();
        } catch (IllegalArgumentException iarge) {
            System.out.println("job params error");
            iarge.printStackTrace();
        } catch (InstantiationException ie) {
            ie.printStackTrace();
        }
    }

    static Tomcat createTomcat(){
        Tomcat tomcat = new Tomcat();
        tomcat.setPort(Integer.parseInt(ConfigHolder.APP_PORT));
        tomcat.getConnector(); // silly needed call
        
        // 修正：删除重复的 base 定义，只保留一个
        File base = new File(System.getProperty("java.io.tmpdir"), "tomcat-webapp");
        if (base.exists()) {
            deleteDirectory(base);
        }
        base.mkdirs();
        
        // 复制资源，处理异常
        try {
            copyResources("/webapp", base);
        } catch (IOException | URISyntaxException e) {
            Log.info("createTomcat", "Failed to copy resources: " + e.getMessage());
            e.printStackTrace();
        }
        
        var context = tomcat.addContext("", base.getAbsolutePath());

        WebResourceRoot resources = new StandardRoot(context);
        context.setResources(resources);
        
        Tomcat.addServlet(
            context,
            "dispatcher", 
            new com.embrace.cup.zoo.Dispatcher()
        ).setMultipartConfigElement(
            new jakarta.servlet.MultipartConfigElement(
                System.getProperty("java.io.tmpdir"),
                20 * 1024 * 1024,  // 单文件 20MB
                50 * 1024 * 1024,  // 总大小 50MB
                1024 * 1024        // 超过 1MB 写磁盘
            )
        );
        context.addServletMappingDecoded("/*", "dispatcher");

        return tomcat;
    }
    
    private static void copyResources(String resourcePath, File targetDir) throws IOException, URISyntaxException {
        URL resourceUrl = App.class.getResource(resourcePath);
        if (resourceUrl == null) {
            System.err.println("Resource does not exist: " + resourcePath);
            return;
        }
        
        // 处理文件系统和 JAR 两种情况
        if (resourceUrl.getProtocol().equals("jar")) {
            // 从 JAR 中复制
            copyFromJar(resourceUrl, resourcePath, targetDir);
        } else {
            // 从文件系统复制（开发环境）
            copyFromFileSystem(new File(resourceUrl.toURI()), targetDir);
        }
    }
    
    private static void copyFromJar(URL jarUrl, String resourcePath, File targetDir) {
        try {
            // 提取 JAR 文件路径
            String jarFile = jarUrl.getPath().split("!")[0].substring(5);
            
            // 使用 FileSystem 访问 JAR 内部
            try (FileSystem fs = FileSystems.newFileSystem(Paths.get(jarFile), (ClassLoader) null)) {
                Path sourceRoot = fs.getPath(resourcePath);
                
                if (!Files.exists(sourceRoot)) {
                    System.err.println("path in jar not found: " + resourcePath);
                    return;
                }
                
                // 遍历并复制所有文件
                try (Stream<Path> walk = Files.walk(sourceRoot)) {
                    walk.forEach(source -> {
                        try {
                            String relativePath = sourceRoot.relativize(source).toString();
                            Path destination = Paths.get(targetDir.getAbsolutePath(), relativePath);
                            
                            if (Files.isDirectory(source)) {
                                Files.createDirectories(destination);
                            } else {
                                Files.createDirectories(destination.getParent());
                                Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING);
                            }
                        } catch (IOException e) {
                            System.err.println("copy from jar failed: " + e.getMessage());
                        }
                    });
                }
            }
            
            System.out.println("copy from jar succeeded");
            
        } catch (IOException e) {
            System.err.println("copy from jar failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void copyFromFileSystem(File sourceDir, File targetDir) {
        try {
            if (!sourceDir.exists()) {
                System.err.println("source folder not found: " + sourceDir.getAbsolutePath());
                return;
            }
            
            // 遍历并复制所有文件
            try (Stream<Path> walk = Files.walk(sourceDir.toPath())) {
                walk.forEach(source -> {
                    try {
                        Path relativePath = sourceDir.toPath().relativize(source);
                        Path destination = Paths.get(targetDir.getAbsolutePath(), relativePath.toString());
                        
                        if (Files.isDirectory(source)) {
                            Files.createDirectories(destination);
                        } else {
                            Files.createDirectories(destination.getParent());
                            Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING);
                        }
                    } catch (IOException e) {
                        System.err.println("copy from filesystem failed: " + e.getMessage());
                    }
                });
            }
            
            System.out.println("copy from filesystem succeeded");
            
        } catch (IOException e) {
            System.err.println("copy from filesystem failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void deleteDirectory(File dir) {
        if (dir == null || !dir.exists()) {
            return;
        }
        if (dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File file : files) {
                    deleteDirectory(file);
                }
            }
        }
        dir.delete();
    }
    
// end of class
}