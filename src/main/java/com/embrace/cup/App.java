package com.embrace.cup;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import org.apache.catalina.WebResourceRoot;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.webresources.StandardRoot;

import com.embrace.cup.zoo.Config;
import com.embrace.cup.zoo.JobExecutor;
import com.embrace.cup.zoo.Log;

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
        String appPackage = Config.get("app.package");
        String jobPackage = Config.get("job.package");
        String classFullName = appPackage + "." + jobPackage + "." + jobName;
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
            // Method m = clazz.getMethod("execute", String[].class);
            // m.invoke(null, (Object)jobParams);
            
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
        String webPort = Config.get("web.port");
        tomcat.setPort(Integer.parseInt(webPort));
        tomcat.getConnector(); // silly needed call
        
        File base = new File("tomcat");
        base.mkdirs();
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
        
        // String cps = Config.get("controller.packages");
        // String[] cpArr = cps.split(",");
        // for (String p : cpArr) {
        //     if (!p.trim().isEmpty()) {
        //         context.addServletMappingDecoded("/"+p+"/*", "dispatcher");
        //     }
        // }
        
        // context.addServletMappingDecoded("/api/*", "dispatcher");
        
        // Tomcat.addServlet(
        //     context,
        //     "default", 
        //     new com.embrace.cup.zoo.DefaultServlet()
        // );
        
        // context.addServletMappingDecoded("/*", "default");


        return tomcat;
    }
// end of class
}
