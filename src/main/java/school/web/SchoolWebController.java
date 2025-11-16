package school.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import school.SchoolManagementService;
import school.adapter.NumericGrade;
import school.decorator.User;
import school.facade.SchoolFacade;
import school.model.Attendance;
import school.model.Grade;
import school.service.AuthenticationService;
import com.sun.net.httpserver.HttpExchange;
import org.json.JSONObject;
import org.json.JSONArray;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Component
public class SchoolWebController {
    @Autowired
    private SchoolFacade schoolFacade;
    
    @Autowired
    private AuthenticationService authenticationService;
    
    private SchoolManagementService schoolService;

    public SchoolWebController() {
    }

    private SchoolManagementService getSchoolService() {
        if (schoolService == null && schoolFacade != null) {
            schoolService = schoolFacade;
        } else if (schoolService == null) {
            schoolService = new SchoolFacade();
        }
        return schoolService;
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
            getSchoolService().completeStudentRegistration(id, name, major, year, trimester);
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
            getSchoolService().completeStaffOnboarding(id, name, dept, position, year, trimester);
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
                getSchoolService().registerGradeObserver("student");
            }
            if (notifyParent) {
                getSchoolService().registerGradeObserver("parent");
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintStream ps = new PrintStream(baos);
            PrintStream oldOut = System.out;
            System.setOut(ps);
            getSchoolService().updateGrade(studentName, new NumericGrade(oldScore), newScore);
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
                double result = getSchoolService().calculateAttendancePercentage(studentName, presentDays, totalDays);
                response.put("result", result);
                response.put("resultType", "percentage");
                response.put("formatted", String.format("%.2f%%", result));
            } else {
                boolean passed = getSchoolService().checkAttendancePassFail(studentName, presentDays, totalDays);
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
            User user = getSchoolService().createUserWithRole("basic", roles);
            getSchoolService().displayUserAccess(user);
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
            getSchoolService().enrollStudent(id, name, major);
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
            getSchoolService().hireStaff(id, name, dept, position);
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
            getSchoolService().notifyGradeChange(studentName, oldScore, newScore);
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
            getSchoolService().createStudentTimetable(studentName, year, trimester);
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
            getSchoolService().createCustomTimetable(year, trimester);
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
            getSchoolService().createPartTimeTimetable(year, trimester);
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
            getSchoolService().demonstrateCompleteSystem();
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

    public void handleLogin(HttpExchange exchange) throws IOException {
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
            String email = json.getString("email");
            String password = json.getString("password");

            Optional<school.model.User> userOpt = authenticationService.login(email, password);
            if (userOpt.isPresent()) {
                school.model.User user = userOpt.get();
                Set<String> roles = authenticationService.getUserRoles(user);
                
                JSONObject response = new JSONObject();
                response.put("success", true);
                response.put("message", "Login successful");
                response.put("userId", user.getUserId());
                response.put("firstName", user.getFirstName());
                response.put("lastName", user.getLastName());
                response.put("name", user.getName());
                response.put("email", user.getEmail());
                response.put("roles", new JSONArray(roles));
                sendResponse(exchange, 200, response.toString());
            } else {
                sendResponse(exchange, 401, createErrorResponse("Invalid email or password"));
            }
        } catch (Exception e) {
            sendResponse(exchange, 400, createErrorResponse(e.getMessage()));
        }
    }

    public void handleRegister(HttpExchange exchange) throws IOException {
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
            String userId = json.getString("userId");
            String firstName = json.getString("firstName");
            String lastName = json.getString("lastName");
            String email = json.getString("email");
            String password = json.getString("password");
            String userType = json.getString("userType");
            String major = json.optString("major", null);
            String department = json.optString("department", null);
            String position = json.optString("position", null);

            school.model.User user = authenticationService.register(userId, firstName, lastName, email, password, userType, major, department, position);
            Set<String> roles = authenticationService.getUserRoles(user);

            JSONObject response = new JSONObject();
            response.put("success", true);
            response.put("message", "Registration successful");
            response.put("userId", user.getUserId());
            response.put("firstName", user.getFirstName());
            response.put("lastName", user.getLastName());
            response.put("name", user.getName());
            response.put("email", user.getEmail());
            response.put("roles", new JSONArray(roles));
            sendResponse(exchange, 200, response.toString());
        } catch (Exception e) {
            sendResponse(exchange, 400, createErrorResponse(e.getMessage()));
        }
    }

    public void handleUserGrades(HttpExchange exchange) throws IOException {
        if ("OPTIONS".equals(exchange.getRequestMethod())) {
            handleOptions(exchange);
            return;
        }
        if (!"GET".equals(exchange.getRequestMethod())) {
            sendResponse(exchange, 405, createErrorResponse("Method not allowed"));
            return;
        }
        try {
            String query = exchange.getRequestURI().getQuery();
            String userId = null;
            if (query != null) {
                for (String param : query.split("&")) {
                    String[] pair = param.split("=");
                    if (pair.length == 2 && "userId".equals(pair[0])) {
                        userId = java.net.URLDecoder.decode(pair[1], StandardCharsets.UTF_8);
                    }
                }
            }
            if (userId == null) {
                sendResponse(exchange, 400, createErrorResponse("userId parameter required"));
                return;
            }

            List<Grade> grades = schoolFacade.getUserGrades(userId);
            JSONArray gradesArray = new JSONArray();
            for (Grade grade : grades) {
                JSONObject gradeJson = new JSONObject();
                gradeJson.put("id", grade.getId());
                gradeJson.put("subject", grade.getSubject());
                gradeJson.put("score", grade.getScore());
                gradeJson.put("createdAt", grade.getCreatedAt().toString());
                gradeJson.put("updatedAt", grade.getUpdatedAt().toString());
                gradesArray.put(gradeJson);
            }

            JSONObject response = new JSONObject();
            response.put("success", true);
            response.put("grades", gradesArray);
            sendResponse(exchange, 200, response.toString());
        } catch (Exception e) {
            sendResponse(exchange, 400, createErrorResponse(e.getMessage()));
        }
    }

    public void handleUserAttendance(HttpExchange exchange) throws IOException {
        if ("OPTIONS".equals(exchange.getRequestMethod())) {
            handleOptions(exchange);
            return;
        }
        if (!"GET".equals(exchange.getRequestMethod())) {
            sendResponse(exchange, 405, createErrorResponse("Method not allowed"));
            return;
        }
        try {
            String query = exchange.getRequestURI().getQuery();
            String userId = null;
            if (query != null) {
                for (String param : query.split("&")) {
                    String[] pair = param.split("=");
                    if (pair.length == 2 && "userId".equals(pair[0])) {
                        userId = java.net.URLDecoder.decode(pair[1], StandardCharsets.UTF_8);
                    }
                }
            }
            if (userId == null) {
                sendResponse(exchange, 400, createErrorResponse("userId parameter required"));
                return;
            }

            List<Attendance> attendanceList = schoolFacade.getUserAttendance(userId);
            JSONArray attendanceArray = new JSONArray();
            int presentCount = 0;
            for (Attendance att : attendanceList) {
                JSONObject attJson = new JSONObject();
                attJson.put("id", att.getId());
                attJson.put("date", att.getDate().toString());
                attJson.put("present", att.getPresent());
                attJson.put("subject", att.getSubject());
                attendanceArray.put(attJson);
                if (att.getPresent()) presentCount++;
            }

            JSONObject response = new JSONObject();
            response.put("success", true);
            response.put("attendance", attendanceArray);
            response.put("totalDays", attendanceList.size());
            response.put("presentDays", presentCount);
            response.put("percentage", attendanceList.size() > 0 ? (presentCount * 100.0 / attendanceList.size()) : 0);
            sendResponse(exchange, 200, response.toString());
        } catch (Exception e) {
            sendResponse(exchange, 400, createErrorResponse(e.getMessage()));
        }
    }

    public void handleRecordAttendance(HttpExchange exchange) throws IOException {
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
            String studentId = json.getString("studentId");
            String studentName = json.getString("studentName");
            String subject = json.getString("subject");
            String dateStr = json.getString("date");
            boolean present = json.getBoolean("present");

            LocalDate date = LocalDate.parse(dateStr);
            schoolFacade.recordAttendance(studentId, studentName, date, present, subject);

            JSONObject response = new JSONObject();
            response.put("success", true);
            response.put("message", "Attendance recorded successfully");
            sendResponse(exchange, 200, response.toString());
        } catch (Exception e) {
            sendResponse(exchange, 400, createErrorResponse(e.getMessage()));
        }
    }

    public void handleGetAllStudents(HttpExchange exchange) throws IOException {
        if ("OPTIONS".equals(exchange.getRequestMethod())) {
            handleOptions(exchange);
            return;
        }
        if (!"GET".equals(exchange.getRequestMethod())) {
            sendResponse(exchange, 405, createErrorResponse("Method not allowed"));
            return;
        }
        try {
            List<school.model.User> students = schoolFacade.getAllStudents();
            JSONArray studentsArray = new JSONArray();
            for (school.model.User student : students) {
                JSONObject studentJson = new JSONObject();
                studentJson.put("userId", student.getUserId());
                studentJson.put("firstName", student.getFirstName());
                studentJson.put("lastName", student.getLastName());
                studentJson.put("name", student.getName());
                studentJson.put("email", student.getEmail());
                studentsArray.put(studentJson);
            }

            JSONObject response = new JSONObject();
            response.put("success", true);
            response.put("students", studentsArray);
            sendResponse(exchange, 200, response.toString());
        } catch (Exception e) {
            sendResponse(exchange, 400, createErrorResponse(e.getMessage()));
        }
    }

    public void handleGetAllUsers(HttpExchange exchange) throws IOException {
        if ("OPTIONS".equals(exchange.getRequestMethod())) {
            handleOptions(exchange);
            return;
        }
        if (!"GET".equals(exchange.getRequestMethod())) {
            sendResponse(exchange, 405, createErrorResponse("Method not allowed"));
            return;
        }
        try {
            List<school.model.User> users = schoolFacade.getAllUsers();
            JSONArray usersArray = new JSONArray();
            for (school.model.User user : users) {
                JSONObject userJson = new JSONObject();
                userJson.put("id", user.getId());
                userJson.put("userId", user.getUserId());
                userJson.put("firstName", user.getFirstName());
                userJson.put("lastName", user.getLastName());
                userJson.put("name", user.getName());
                userJson.put("email", user.getEmail());
                userJson.put("major", user.getMajor());
                userJson.put("department", user.getDepartment());
                userJson.put("position", user.getPosition());
                JSONArray rolesArray = new JSONArray();
                for (school.model.Role role : user.getRoles()) {
                    rolesArray.put(role.getName());
                }
                userJson.put("roles", rolesArray);
                usersArray.put(userJson);
            }

            JSONObject response = new JSONObject();
            response.put("success", true);
            response.put("users", usersArray);
            sendResponse(exchange, 200, response.toString());
        } catch (Exception e) {
            sendResponse(exchange, 400, createErrorResponse(e.getMessage()));
        }
    }

    public void handleGetAllStaff(HttpExchange exchange) throws IOException {
        if ("OPTIONS".equals(exchange.getRequestMethod())) {
            handleOptions(exchange);
            return;
        }
        if (!"GET".equals(exchange.getRequestMethod())) {
            sendResponse(exchange, 405, createErrorResponse("Method not allowed"));
            return;
        }
        try {
            List<school.model.User> staff = schoolFacade.getAllStaff();
            JSONArray staffArray = new JSONArray();
            for (school.model.User user : staff) {
                JSONObject userJson = new JSONObject();
                userJson.put("id", user.getId());
                userJson.put("userId", user.getUserId());
                userJson.put("firstName", user.getFirstName());
                userJson.put("lastName", user.getLastName());
                userJson.put("name", user.getName());
                userJson.put("email", user.getEmail());
                userJson.put("department", user.getDepartment());
                userJson.put("position", user.getPosition());
                JSONArray rolesArray = new JSONArray();
                for (school.model.Role role : user.getRoles()) {
                    rolesArray.put(role.getName());
                }
                userJson.put("roles", rolesArray);
                staffArray.put(userJson);
            }

            JSONObject response = new JSONObject();
            response.put("success", true);
            response.put("staff", staffArray);
            sendResponse(exchange, 200, response.toString());
        } catch (Exception e) {
            sendResponse(exchange, 400, createErrorResponse(e.getMessage()));
        }
    }

    public void handleUpdateUser(HttpExchange exchange) throws IOException {
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
            String userId = json.getString("userId");
            String firstName = json.optString("firstName", null);
            String lastName = json.optString("lastName", null);
            String email = json.optString("email", null);
            String major = json.optString("major", null);
            String department = json.optString("department", null);
            String position = json.optString("position", null);

            school.model.User updatedUser = schoolFacade.updateUser(userId, firstName, lastName, email, major, department, position);
            if (updatedUser != null) {
                JSONObject response = new JSONObject();
                response.put("success", true);
                response.put("message", "User updated successfully");
                response.put("userId", updatedUser.getUserId());
                sendResponse(exchange, 200, response.toString());
            } else {
                sendResponse(exchange, 404, createErrorResponse("User not found"));
            }
        } catch (Exception e) {
            sendResponse(exchange, 400, createErrorResponse(e.getMessage()));
        }
    }

    public void handleDeleteUser(HttpExchange exchange) throws IOException {
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
            String userId = json.getString("userId");

            schoolFacade.deleteUser(userId);

            JSONObject response = new JSONObject();
            response.put("success", true);
            response.put("message", "User deleted successfully");
            sendResponse(exchange, 200, response.toString());
        } catch (Exception e) {
            sendResponse(exchange, 400, createErrorResponse(e.getMessage()));
        }
    }

    public void handleGetNotifications(HttpExchange exchange) throws IOException {
        if ("OPTIONS".equals(exchange.getRequestMethod())) {
            handleOptions(exchange);
            return;
        }
        if (!"GET".equals(exchange.getRequestMethod())) {
            sendResponse(exchange, 405, createErrorResponse("Method not allowed"));
            return;
        }
        try {
            String query = exchange.getRequestURI().getQuery();
            String userId = null;
            if (query != null) {
                for (String param : query.split("&")) {
                    String[] pair = param.split("=");
                    if (pair.length == 2 && "userId".equals(pair[0])) {
                        userId = java.net.URLDecoder.decode(pair[1], StandardCharsets.UTF_8);
                    }
                }
            }
            if (userId == null) {
                sendResponse(exchange, 400, createErrorResponse("userId parameter required"));
                return;
            }

            List<school.model.Notification> notifications = schoolFacade.getUserNotifications(userId);
            JSONArray notifArray = new JSONArray();
            for (school.model.Notification notif : notifications) {
                JSONObject notifJson = new JSONObject();
                notifJson.put("id", notif.getId());
                notifJson.put("message", notif.getMessage());
                notifJson.put("type", notif.getType());
                notifJson.put("isRead", notif.getIsRead());
                notifJson.put("createdAt", notif.getCreatedAt().toString());
                notifArray.put(notifJson);
            }

            JSONObject response = new JSONObject();
            response.put("success", true);
            response.put("notifications", notifArray);
            sendResponse(exchange, 200, response.toString());
        } catch (Exception e) {
            sendResponse(exchange, 400, createErrorResponse(e.getMessage()));
        }
    }

    public void handleMarkNotificationRead(HttpExchange exchange) throws IOException {
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
            Long notificationId = json.getLong("notificationId");

            schoolFacade.markNotificationAsRead(notificationId);

            JSONObject response = new JSONObject();
            response.put("success", true);
            response.put("message", "Notification marked as read");
            sendResponse(exchange, 200, response.toString());
        } catch (Exception e) {
            sendResponse(exchange, 400, createErrorResponse(e.getMessage()));
        }
    }

    public void handleLinkParentToStudent(HttpExchange exchange) throws IOException {
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
            String parentUserId = json.getString("parentUserId");
            String studentUserId = json.getString("studentUserId");

            schoolFacade.linkParentToStudent(parentUserId, studentUserId);

            JSONObject response = new JSONObject();
            response.put("success", true);
            response.put("message", "Parent linked to student successfully");
            sendResponse(exchange, 200, response.toString());
        } catch (Exception e) {
            sendResponse(exchange, 400, createErrorResponse(e.getMessage()));
        }
    }

    public void handleGetParentStudents(HttpExchange exchange) throws IOException {
        if ("OPTIONS".equals(exchange.getRequestMethod())) {
            handleOptions(exchange);
            return;
        }
        if (!"GET".equals(exchange.getRequestMethod())) {
            sendResponse(exchange, 405, createErrorResponse("Method not allowed"));
            return;
        }
        try {
            String query = exchange.getRequestURI().getQuery();
            String parentUserId = null;
            if (query != null) {
                for (String param : query.split("&")) {
                    String[] pair = param.split("=");
                    if (pair.length == 2 && "parentUserId".equals(pair[0])) {
                        parentUserId = java.net.URLDecoder.decode(pair[1], StandardCharsets.UTF_8);
                    }
                }
            }
            if (parentUserId == null) {
                sendResponse(exchange, 400, createErrorResponse("parentUserId parameter required"));
                return;
            }

            List<String> studentIds = schoolFacade.getStudentIdsForParent(parentUserId);
            JSONObject response = new JSONObject();
            response.put("success", true);
            response.put("studentIds", new JSONArray(studentIds));
            sendResponse(exchange, 200, response.toString());
        } catch (Exception e) {
            sendResponse(exchange, 400, createErrorResponse(e.getMessage()));
        }
    }
}

