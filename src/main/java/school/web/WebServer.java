package school.web;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;

import java.io.*;
import java.net.InetSocketAddress;

public class WebServer {
    private HttpServer server;
    private SchoolWebController controller;
    private int port;

    public WebServer(int port) {
        this.port = port;
        this.controller = new SchoolWebController();
    }

    public void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/api/students/register", controller::handleStudentRegistration);
        server.createContext("/api/students/enroll", controller::handleStudentEnrollment);
        server.createContext("/api/staff/onboard", controller::handleStaffOnboarding);
        server.createContext("/api/staff/hire", controller::handleStaffHiring);
        server.createContext("/api/grades/update", controller::handleGradeUpdate);
        server.createContext("/api/grades/notify", controller::handleGradeNotification);
        server.createContext("/api/timetable/student", controller::handleStudentTimetable);
        server.createContext("/api/timetable/custom", controller::handleCustomTimetable);
        server.createContext("/api/timetable/parttime", controller::handlePartTimeTimetable);
        server.createContext("/api/attendance/calculate", controller::handleAttendanceCalculation);
        server.createContext("/api/users/create", controller::handleUserCreation);
        server.createContext("/api/demo/run", controller::handleSystemDemo);
        server.createContext("/", this::handleStaticFiles);
        server.setExecutor(null);
        server.start();
        System.out.println("Web server started on http://localhost:" + port);
        System.out.println("Open your browser and navigate to: http://localhost:" + port);
    }

    public void stop() {
        if (server != null) {
            server.stop(0);
        }
    }

    private void handleStaticFiles(HttpExchange exchange) throws IOException {
        if ("OPTIONS".equals(exchange.getRequestMethod())) {
            exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
            exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
            exchange.sendResponseHeaders(200, -1);
            exchange.close();
            return;
        }

        String path = exchange.getRequestURI().getPath();
        if (path.equals("/") || path.equals("/index.html")) {
            path = "/index.html";
        }

        String filePath = "web" + path;
        InputStream resourceStream = getClass().getClassLoader().getResourceAsStream(filePath);
        if (resourceStream == null) {
            File file = new File("src/main/resources" + path);
            if (file.exists() && !file.isDirectory()) {
                resourceStream = new FileInputStream(file);
            } else {
                send404(exchange);
                return;
            }
        }

        byte[] fileBytes = readAllBytes(resourceStream);
        resourceStream.close();

        String contentType = getContentType(path);
        exchange.getResponseHeaders().set("Content-Type", contentType);
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.sendResponseHeaders(200, fileBytes.length);
        OutputStream os = exchange.getResponseBody();
        os.write(fileBytes);
        os.close();
    }

    private String getContentType(String filePath) {
        if (filePath.endsWith(".html")) return "text/html";
        if (filePath.endsWith(".css")) return "text/css";
        if (filePath.endsWith(".js")) return "application/javascript";
        if (filePath.endsWith(".json")) return "application/json";
        return "text/plain";
    }

    private void send404(HttpExchange exchange) throws IOException {
        String response = "404 - File Not Found";
        exchange.sendResponseHeaders(404, response.length());
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }

    private byte[] readAllBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] data = new byte[8192];
        int nRead;
        while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }
        return buffer.toByteArray();
    }
}

