package org.example;

import com.sun.management.OperatingSystemMXBean;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.management.ManagementFactory;

public class SystemInfoServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        OperatingSystemMXBean os = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();

        long totalRamMb = os.getTotalMemorySize() / (1024 * 1024);
        long freeRamMb = os.getFreeMemorySize() / (1024 * 1024);
        int cpuCores = os.getAvailableProcessors();
        String osName = os.getName() + " " + os.getVersion();
        String arch = os.getArch();

        resp.setContentType("text/html");
        PrintWriter out = resp.getWriter();
        out.println("<h1>System Info</h1>");
        out.println("<ul>");
        out.println("<li><b>OS:</b> " + osName + "</li>");
        out.println("<li><b>Architecture:</b> " + arch + "</li>");
        out.println("<li><b>CPU cores:</b> " + cpuCores + "</li>");
        out.println("<li><b>Total RAM:</b> " + totalRamMb + " MB</li>");
        out.println("<li><b>Free RAM:</b> " + freeRamMb + " MB</li>");
        out.println("</ul>");
    }
}
