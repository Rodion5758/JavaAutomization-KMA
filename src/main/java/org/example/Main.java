package org.example;

import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;

import java.io.File;

public class Main {
    public static void main(String[] args) throws Exception {
        Tomcat tomcat = new Tomcat();
        tomcat.setPort(9090);
        tomcat.getConnector();

        Context ctx = tomcat.addContext("", new File(".").getAbsolutePath());
        Tomcat.addServlet(ctx, "shapoval", new MyNameServlet());
        ctx.addServletMappingDecoded("/shapoval", "shapoval");
        Tomcat.addServlet(ctx, "system", new SystemInfoServlet());
        ctx.addServletMappingDecoded("/os-info", "system");

        tomcat.start();
        tomcat.getServer().await();
    }
}
