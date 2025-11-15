package school.web;

import school.SchoolManagementService;
import school.adapter.NumericGrade;
import school.decorator.User;
import school.facade.SchoolFacade;
import com.sun.net.httpserver.HttpExchange;
import org.json.JSONObject;
import org.json.JSONArray;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class SchoolWebController {
    private SchoolManagementService schoolService;

    public SchoolWebController() {
        this.schoolService = new SchoolFacade();
    }

    public void handleStudentRegistration(HttpExchange exchange) throws IOException {
        if ("OPTIONS".equals(exchange.getRequestMethod())) {
            handleOptions(exchange);
            return;
        }
        if (!"POST".equals(exchange.getRequestMethod())) {
            sendResponse(exchange, 405, createErrorResponse("Method not allowed"));
            return;
        }
        try {
            String requestBody = readRequestBody(exchange);
            JSONObject json = new JSONObject(requestBody);
            String id = json.getString("id");
            String name = json.getString("name");
            String major = json.getString("major");
            String year = json.optString("year", "2024-2025");
            String trimester = json.optString("trimester", "first");

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintStream ps = new PrintStream(baos);
            PrintStream oldOut = System.out;
            System.setOut(ps);
            schoolService.completeStudentRegistration(id, name, major, year, trimester);
            schoolService.completeStudentRegistration(id, name, major, year, trimester);
            System.setOut(oldOut);
            String output = baos.toString();

            JSONObject response = new JSONObject();
            response.put("success", true);
            response.put("message", "Student registered successfully");
            response.put("output", output);
            response.put("studentId", id);
            response.put("studentName", name);
            sendResponse(exchange, 200, response.toString());
        } catch (Exception e) {
            sendResponse(exchange, 400, createErrorResponse(e.getMessage()));
        }
    }

    public void handleStaffOnboarding(HttpExchange exchange) throws IOException {
        if ("OPTIONS".equals(exchange.getRequestMethod())) {
            handleOptions(exchange);
            return;
        }
        if (!"POST".equals(exchange.getRequestMethod())) {
            sendResponse(exchange, 405, createErrorResponse("Method not allowed"));
            return;
        }
        try {
            String requestBody = readRequestBody(exchange);
            JSONObject json = new JSONObject(requestBody);
            String id = json.getString("id");
            String name = json.getString("name");
            String dept = json.getString("department");
            String position = json.getString("position");
            String year = json.optString("year", "2024-2025");
            String trimester = json.optString("trimester", "first");

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintStream ps = new PrintStream(baos);
            PrintStream oldOut = System.out;
            System.setOut(ps);
            schoolService.completeStaffOnboarding(id, name, dept, position, year, trimester);
            System.setOut(oldOut);
            String output = baos.toString();

            JSONObject response = new JSONObject();
            response.put("success", true);
            response.put("message", "Staff onboarded successfully");
            response.put("output", output);
            response.put("staffId", id);
            response.put("staffName", name);
            sendResponse(exchange, 200, response.toString());
        } catch (Exception e) {
            sendResponse(exchange, 400, createErrorResponse(e.getMessage()));
        }
    }

    public void handleGradeUpdate(HttpExchange exchange) throws IOException {
        if ("OPTIONS".equals(exchange.getRequestMethod())) {
            handleOptions(exchange);
            return;
        }
        if (!"POST".equals(exchange.getRequestMethod())) {
            sendResponse(exchange, 405, createErrorResponse("Method not allowed"));
            return;
        }
        try {
            String requestBody = readRequestBody(exchange);
            JSONObject json = new JSONObject(requestBody);
            String studentName = json.getString("studentName");
            int oldScore = json.getInt("oldGrade");
            int newScore = json.getInt("newGrade");
            boolean notifyStudent = json.optBoolean("notifyStudent", true);
            boolean notifyParent = json.optBoolean("notifyParent", true);

            if (notifyStudent) {
                schoolService.registerGradeObserver("student");
            }
            if (notifyParent) {
                schoolService.registerGradeObserver("parent");
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintStream ps = new PrintStream(baos);
            PrintStream oldOut = System.out;
            System.setOut(ps);
            schoolService.updateGrade(studentName, new NumericGrade(oldScore), newScore);
            System.setOut(oldOut);
            String output = baos.toString();

            JSONObject response = new JSONObject();
            response.put("success", true);
            response.put("message", "Grade updated successfully");
            response.put("output", output);
            sendResponse(exchange, 200, response.toString());
        } catch (Exception e) {
            sendResponse(exchange, 400, createErrorResponse(e.getMessage()));
        }
    }

    public void handleAttendanceCalculation(HttpExchange exchange) throws IOException {
        if ("OPTIONS".equals(exchange.getRequestMethod())) {
            handleOptions(exchange);
            return;
        }
        if (!"POST".equals(exchange.getRequestMethod())) {
            sendResponse(exchange, 405, createErrorResponse("Method not allowed"));
            return;
        }
        try {
            String requestBody = readRequestBody(exchange);
            JSONObject json = new JSONObject(requestBody);
            String studentName = json.getString("studentName");
            int presentDays = json.getInt("presentDays");
            int totalDays = json.getInt("totalDays");
            String strategy = json.optString("strategy", "percentage");

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintStream ps = new PrintStream(baos);
            PrintStream oldOut = System.out;
            System.setOut(ps);

            JSONObject response = new JSONObject();
            if ("percentage".equalsIgnoreCase(strategy)) {
                double result = schoolService.calculateAttendancePercentage(studentName, presentDays, totalDays);
                response.put("result", result);
                response.put("resultType", "percentage");
                response.put("formatted", String.format("%.2f%%", result));
            } else {
                boolean passed = schoolService.checkAttendancePassFail(studentName, presentDays, totalDays);
                response.put("result", passed);
                response.put("resultType", "passFail");
                response.put("formatted", passed ? "PASS" : "FAIL");
            }
            System.setOut(oldOut);
            String output = baos.toString();
            response.put("output", output);
            response.put("success", true);
            sendResponse(exchange, 200, response.toString());
        } catch (Exception e) {
            sendResponse(exchange, 400, createErrorResponse(e.getMessage()));
        }
    }

    public void handleUserCreation(HttpExchange exchange) throws IOException {
        if ("OPTIONS".equals(exchange.getRequestMethod())) {
            handleOptions(exchange);
            return;
        }
        if (!"POST".equals(exchange.getRequestMethod())) {
            sendResponse(exchange, 405, createErrorResponse("Method not allowed"));
            return;
        }
        try {
            String requestBody = readRequestBody(exchange);
            JSONObject json = new JSONObject(requestBody);
            JSONArray rolesArray = json.getJSONArray("roles");
            String[] roles = new String[rolesArray.length()];
            for (int i = 0; i < rolesArray.length(); i++) {
                roles[i] = rolesArray.getString(i);
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintStream ps = new PrintStream(baos);
            PrintStream oldOut = System.out;
            System.setOut(ps);
            User user = schoolService.createUserWithRole("basic", roles);
            schoolService.displayUserAccess(user);
            System.setOut(oldOut);
            String output = baos.toString();

            JSONObject response = new JSONObject();
            response.put("success", true);
            response.put("message", "User created successfully");
            response.put("description", user.getDescription());
            response.put("accessLevel", user.getAccessLevel());
            response.put("output", output);
            sendResponse(exchange, 200, response.toString());
        } catch (Exception e) {
            sendResponse(exchange, 400, createErrorResponse(e.getMessage()));
        }
    }

    public void handleStudentEnrollment(HttpExchange exchange) throws IOException {
        if ("OPTIONS".equals(exchange.getRequestMethod())) {
            handleOptions(exchange);
            return;
        }
        if (!"POST".equals(exchange.getRequestMethod())) {
            sendResponse(exchange, 405, createErrorResponse("Method not allowed"));
            return;
        }
        try {
            String requestBody = readRequestBody(exchange);
            JSONObject json = new JSONObject(requestBody);
            String id = json.getString("id");
            String name = json.getString("name");
            String major = json.getString("major");

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintStream ps = new PrintStream(baos);
            PrintStream oldOut = System.out;
            System.setOut(ps);
            schoolService.enrollStudent(id, name, major);
            System.setOut(oldOut);
            String output = baos.toString();

            JSONObject response = new JSONObject();
            response.put("success", true);
            response.put("message", "Student enrolled successfully");
            response.put("output", output);
            sendResponse(exchange, 200, response.toString());
        } catch (Exception e) {
            sendResponse(exchange, 400, createErrorResponse(e.getMessage()));
        }
    }

    public void handleStaffHiring(HttpExchange exchange) throws IOException {
        if ("OPTIONS".equals(exchange.getRequestMethod())) {
            handleOptions(exchange);
            return;
        }
        if (!"POST".equals(exchange.getRequestMethod())) {
            sendResponse(exchange, 405, createErrorResponse("Method not allowed"));
            return;
        }
        try {
            String requestBody = readRequestBody(exchange);
            JSONObject json = new JSONObject(requestBody);
            String id = json.getString("id");
            String name = json.getString("name");
            String dept = json.getString("department");
            String position = json.getString("position");

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintStream ps = new PrintStream(baos);
            PrintStream oldOut = System.out;
            System.setOut(ps);
            schoolService.hireStaff(id, name, dept, position);
            System.setOut(oldOut);
            String output = baos.toString();

            JSONObject response = new JSONObject();
            response.put("success", true);
            response.put("message", "Staff hired successfully");
            response.put("output", output);
            sendResponse(exchange, 200, response.toString());
        } catch (Exception e) {
            sendResponse(exchange, 400, createErrorResponse(e.getMessage()));
        }
    }

    public void handleGradeNotification(HttpExchange exchange) throws IOException {
        if ("OPTIONS".equals(exchange.getRequestMethod())) {
            handleOptions(exchange);
            return;
        }
        if (!"POST".equals(exchange.getRequestMethod())) {
            sendResponse(exchange, 405, createErrorResponse("Method not allowed"));
            return;
        }
        try {
            String requestBody = readRequestBody(exchange);
            JSONObject json = new JSONObject(requestBody);
            String studentName = json.getString("studentName");
            int oldScore = json.getInt("oldScore");
            int newScore = json.getInt("newScore");

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintStream ps = new PrintStream(baos);
            PrintStream oldOut = System.out;
            System.setOut(ps);
            schoolService.notifyGradeChange(studentName, oldScore, newScore);
            System.setOut(oldOut);
            String output = baos.toString();

            JSONObject response = new JSONObject();
            response.put("success", true);
            response.put("message", "Grade notification sent");
            response.put("output", output);
            sendResponse(exchange, 200, response.toString());
        } catch (Exception e) {
            sendResponse(exchange, 400, createErrorResponse(e.getMessage()));
        }
    }

    public void handleStudentTimetable(HttpExchange exchange) throws IOException {
        if ("OPTIONS".equals(exchange.getRequestMethod())) {
            handleOptions(exchange);
            return;
        }
        if (!"POST".equals(exchange.getRequestMethod())) {
            sendResponse(exchange, 405, createErrorResponse("Method not allowed"));
            return;
        }
        try {
            String requestBody = readRequestBody(exchange);
            JSONObject json = new JSONObject(requestBody);
            String studentName = json.getString("studentName");
            String year = json.optString("year", "2024-2025");
            String trimester = json.optString("trimester", "first");

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintStream ps = new PrintStream(baos);
            PrintStream oldOut = System.out;
            System.setOut(ps);
            schoolService.createStudentTimetable(studentName, year, trimester);
            System.setOut(oldOut);
            String output = baos.toString();

            JSONObject response = new JSONObject();
            response.put("success", true);
            response.put("message", "Student timetable created");
            response.put("output", output);
            sendResponse(exchange, 200, response.toString());
        } catch (Exception e) {
            sendResponse(exchange, 400, createErrorResponse(e.getMessage()));
        }
    }

    public void handleCustomTimetable(HttpExchange exchange) throws IOException {
        if ("OPTIONS".equals(exchange.getRequestMethod())) {
            handleOptions(exchange);
            return;
        }
        if (!"POST".equals(exchange.getRequestMethod())) {
            sendResponse(exchange, 405, createErrorResponse("Method not allowed"));
            return;
        }
        try {
            String requestBody = readRequestBody(exchange);
            JSONObject json = new JSONObject(requestBody);
            String year = json.getString("year");
            String trimester = json.getString("trimester");

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintStream ps = new PrintStream(baos);
            PrintStream oldOut = System.out;
            System.setOut(ps);
            schoolService.createCustomTimetable(year, trimester);
            System.setOut(oldOut);
            String output = baos.toString();

            JSONObject response = new JSONObject();
            response.put("success", true);
            response.put("message", "Custom timetable created");
            response.put("output", output);
            sendResponse(exchange, 200, response.toString());
        } catch (Exception e) {
            sendResponse(exchange, 400, createErrorResponse(e.getMessage()));
        }
    }

    public void handlePartTimeTimetable(HttpExchange exchange) throws IOException {
        if ("OPTIONS".equals(exchange.getRequestMethod())) {
            handleOptions(exchange);
            return;
        }
        if (!"POST".equals(exchange.getRequestMethod())) {
            sendResponse(exchange, 405, createErrorResponse("Method not allowed"));
            return;
        }
        try {
            String requestBody = readRequestBody(exchange);
            JSONObject json = new JSONObject(requestBody);
            String year = json.optString("year", "2024-2025");
            String trimester = json.optString("trimester", "second");

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintStream ps = new PrintStream(baos);
            PrintStream oldOut = System.out;
            System.setOut(ps);
            schoolService.createPartTimeTimetable(year, trimester);
            System.setOut(oldOut);
            String output = baos.toString();

            JSONObject response = new JSONObject();
            response.put("success", true);
            response.put("message", "Part-time timetable created");
            response.put("output", output);
            sendResponse(exchange, 200, response.toString());
        } catch (Exception e) {
            sendResponse(exchange, 400, createErrorResponse(e.getMessage()));
        }
    }

    public void handleSystemDemo(HttpExchange exchange) throws IOException {
        if ("OPTIONS".equals(exchange.getRequestMethod())) {
            handleOptions(exchange);
            return;
        }
        if (!"POST".equals(exchange.getRequestMethod())) {
            sendResponse(exchange, 405, createErrorResponse("Method not allowed"));
            return;
        }
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintStream ps = new PrintStream(baos);
            PrintStream oldOut = System.out;
            System.setOut(ps);
            schoolService.demonstrateCompleteSystem();
            System.setOut(oldOut);
            String output = baos.toString();

            JSONObject response = new JSONObject();
            response.put("success", true);
            response.put("message", "System demo completed");
            response.put("output", output);
            sendResponse(exchange, 200, response.toString());
        } catch (Exception e) {
            sendResponse(exchange, 400, createErrorResponse(e.getMessage()));
        }
    }

    private String readRequestBody(HttpExchange exchange) throws IOException {
        InputStream is = exchange.getRequestBody();
        BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        return sb.toString();
    }

    private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
        byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(statusCode, responseBytes.length);
        OutputStream os = exchange.getResponseBody();
        os.write(responseBytes);
        os.close();
    }

    private void handleOptions(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
        exchange.sendResponseHeaders(200, -1);
        exchange.close();
    }

    private String createErrorResponse(String message) {
        JSONObject error = new JSONObject();
        error.put("success", false);
        error.put("error", message);
        return error.toString();
    }
}

