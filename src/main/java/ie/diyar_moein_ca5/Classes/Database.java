package ie.diyar_moein_ca5.Classes;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.io.Resources;
import com.google.common.hash.Hashing;

import ie.diyar_moein_ca5.Exceptions.*;
import ie.diyar_moein_ca5.Services.JWTAuthentication;
import ie.diyar_moein_ca5.repository.ConnectionPool;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class Database {
    ArrayList<Course> courses;
    ObjectMapper objectMapper;
    String currentStudent = "";
    String searchKey = "";
    String errorMessage = "";
    private static Database database;
    String CourseTableName = "Courses";
    String StudentTableName = "Students";
    String OfferingTableName = "Offerings";
    String AddedOfferingTableName = "AddedOfferings";
    String PrerequisitTableName = "Prerequisites";
    String TermTableName = "Terms";

    private Database() throws SQLException {
        this.initializeTablesInDB();
        objectMapper = new ObjectMapper();
    }

    public void signup(String studentId, String firstName, String secondName,
                       String birthDate, String field, String faculty,
                       String level, String email, String password) throws StudentAlreadySignedUpException, SQLException {
        if (checkStudentIdRepeating(studentId)) {
            String status = "مشغول به تحصیل";
            String img = "http://138.197.181.131:5200/img/default.jpg";
            this.addStudentToDB(studentId, firstName, secondName, birthDate, field, faculty, level, status, img, email, password);
        }
        else
            throw new StudentAlreadySignedUpException();
    }

    public String getAddedOfferingTableName() {
        return AddedOfferingTableName;
    }

    private void initializeTablesInDB() throws SQLException {
        Connection connection = ConnectionPool.getConnection();
        this.initializeStudentTable(connection);
        this.initializeCourseTable(connection);
        this.initializeOfferingTable(connection);
        this.initializeAddedOfferingTable(connection);
        this.initializeTermTable(connection);
        this.initializePrerequisitesTable(connection);
        connection.close();
    }

    private void initializeStudentTable(Connection connection) throws SQLException {
        String command = String.format("CREATE TABLE IF NOT EXISTS %s", StudentTableName);
        command += String.format("(studentId CHAR(50),\nname CHAR(225),\nsecondName CHAR(225),\nbirthDate CHAR(100),");
        command += String.format("\nfield CHAR(100),\nfaculty CHAR(100),\nlevel CHAR(100),\nstatus CHAR(100),");
        command += String.format("\nimg CHAR(225), \nemail CHAR(100), \npassword CHAR(255), ");
        command += String.format("\nPRIMARY KEY(studentId));");
        PreparedStatement createStudentTableStatement = connection.prepareStatement(command);
        createStudentTableStatement.executeUpdate();
        createStudentTableStatement.close();
    }

    private void initializeCourseTable(Connection connection) throws SQLException {
        String command = String.format("CREATE TABLE IF NOT EXISTS %s", CourseTableName);
        command += String.format("(courseCode CHAR(50),\nname CHAR(225),\ntype CHAR(100),\nunits INT,");
        command += String.format("\nexamTimeStart CHAR(100),\nexamTimeEnd CHAR(100),");
        command += String.format("\nPRIMARY KEY(courseCode));");

        PreparedStatement createCourseTableStatement = connection.prepareStatement(command);
        createCourseTableStatement.executeUpdate();
        createCourseTableStatement.close();
    }

    private void initializeOfferingTable(Connection connection) throws SQLException {
        String command = String.format("CREATE TABLE IF NOT EXISTS %s", OfferingTableName);
        command += String.format("(courseCode CHAR(50), \nclassCode CHAR(50),\ninstructor CHAR(225),");
        command += String.format("\nfirstClassDay CHAR(100),\nsecondClassDay CHAR(100),\nclassTime CHAR(100),");
        command += String.format("\ncapacity INT, \nPRIMARY KEY(courseCode, classCode),");
        command += String.format("\nFOREIGN KEY (courseCode) REFERENCES %s(courseCode));", CourseTableName);

        PreparedStatement createCourseTableStatement = connection.prepareStatement(command);
        createCourseTableStatement.executeUpdate();
        createCourseTableStatement.close();
    }

    private void initializeTermTable(Connection connection) throws SQLException {
        String command = String.format("CREATE TABLE IF NOT EXISTS %s", TermTableName);
        command += String.format("(courseCode CHAR(50),\ntermNumber INT,\ngrade FLOAT,");
        command += String.format("\nstudentId CHAR(50),\nPRIMARY KEY (courseCode, termNumber, studentId),");
        command += String.format("\nFOREIGN KEY (courseCode) REFERENCES %s(courseCode),", CourseTableName);
        command += String.format("\nFOREIGN KEY (studentId) REFERENCES %s(studentId));", StudentTableName);

        PreparedStatement createTermTableStatement = connection.prepareStatement(command);
        createTermTableStatement.executeUpdate();
        createTermTableStatement.close();
    }

    private void initializeAddedOfferingTable(Connection connection) throws SQLException {
        String command = String.format("CREATE TABLE IF NOT EXISTS %s", AddedOfferingTableName);
        command += String.format("(courseCode CHAR(50), \nclassCode CHAR(50),\nstudentId CHAR(50),");
        command += String.format("\nstatus CHAR(100),\nwantsToRemove BOOLEAN,\nisWaiting BOOLEAN,");
        command += String.format("\nPRIMARY KEY(studentId, courseCode, classCode),");
        command += String.format("\nFOREIGN KEY (courseCode, classCode) REFERENCES %s(courseCode, classCode),", OfferingTableName);
        command += String.format("\nFOREIGN KEY (studentId) REFERENCES %s(studentId));", StudentTableName);

        PreparedStatement createAddedOfferingTableStatement = connection.prepareStatement(command);
        createAddedOfferingTableStatement.executeUpdate();
        createAddedOfferingTableStatement.close();
    }

    private void initializePrerequisitesTable(Connection connection) throws SQLException {
        String command = String.format("CREATE TABLE IF NOT EXISTS %s", PrerequisitTableName);
        command += String.format("(mainCourseCode CHAR(50),\nid MEDIUMINT NOT NULL AUTO_INCREMENT,");
        command += String.format("\nprerequisitCourseCode CHAR(50),");
        command += String.format("\nPRIMARY KEY(id, mainCourseCode),");
        command += String.format("\nFOREIGN KEY (mainCourseCode) REFERENCES %s(courseCode),", CourseTableName);
        command += String.format("\nFOREIGN KEY (prerequisitCourseCode) REFERENCES %s(courseCode));", CourseTableName);

        PreparedStatement createPrerequisitesTableStatement = connection.prepareStatement(command);
        createPrerequisitesTableStatement.executeUpdate();
        createPrerequisitesTableStatement.close();
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String message) {
        this.errorMessage = message;
    }

    public static Database getDatabase() throws SQLException {
        if (database == null) {
            database = new Database();
            try {
                database.getStudentsFromAPI();
                database.getCoursesFromAPI();
                database.getGradesFromAPI();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return database;
    }

    public String getSearchKey() {
        return searchKey;
    }

    public void setSearchKey(String searchKey) {
        this.searchKey = searchKey;
    }

    private boolean PasswordIsCorrect(Student student, String password) throws SQLException {
        String userPassword = student.getHashedPassword();
        password = Hashing.sha256().hashString(password, StandardCharsets.UTF_8).toString();
        return password.equals(userPassword);
    }

    public String login(String email, String password) throws StudentNotFoundException, SQLException, CourseNotFoundException, WrongPasswordException {
        Student student = database.getStudent(email);
        if (student != null) {
            if (!PasswordIsCorrect(student, password))
                throw new WrongPasswordException();
            this.currentStudent = email;
            return JWTAuthentication.createAndSignToken(email);

        }
        else
            throw new StudentNotFoundException();
    }

    public Student getCurrentStudent() throws SQLException, StudentNotFoundException, CourseNotFoundException {
        return getStudent(this.currentStudent);
    }

    public void logout() {
        this.currentStudent = "";
    }

    public String sendGetRequestToURL(String url) throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = null;
        HttpRequest request;
        try {
            request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .build();
            response = client.send(request,
                    HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            throw new Exception("Can't get data from " + url);
        }
        return response.body();
    }

    public void getStudentsFromAPI() throws Exception {
        String response = sendGetRequestToURL("http://138.197.181.131:5200/api/students");
        JsonNode jsonNode = objectMapper.readTree(response);
        for (int i = 0; i < jsonNode.size(); i++)
        {
            addStudent(jsonNode.get(i).toString());
        }
    }

    public void getGradesFromAPI() throws Exception {
        for (String studentId : getStudentFromDB()) {
            String response = sendGetRequestToURL("http://138.197.181.131:5200/api/grades/" + studentId);
            JsonNode jsonNode = objectMapper.readTree(response);

            HashMap<Integer, HashMap<String, Double>> termGrades = new HashMap<>();
            for (int i = 0; i < jsonNode.size(); i++)
            {
                if (jsonNode.get(i) == null) {
                    break;
                }

                Integer termNumber = jsonNode.get(i).get("term").asInt();
                String courseCode = jsonNode.get(i).get("code").asText();
                Double grade = jsonNode.get(i).get("grade").asDouble();
                if (termGrades.containsKey(termNumber)) {
                    termGrades.get(termNumber).put(courseCode, grade);
                }
                else {
                    HashMap<String, Double> grades = new HashMap<>();
                    grades.put(courseCode, grade);
                    termGrades.put(termNumber, grades);
                }
            }
            this.addTermToDB(termGrades, studentId);
        }
    }

    private ArrayList<String> getStudentFromDB() throws SQLException {
        Connection connection = ConnectionPool.getConnection();
        PreparedStatement statement = connection.prepareStatement(
                String.format("select studentId from %s;", StudentTableName));
        ResultSet result = statement.executeQuery();
        ArrayList<String> studentsId = new ArrayList<>();
        while (result.next())
            studentsId.add(result.getString("studentId"));

        result.close();
        statement.close();
        connection.close();
        return studentsId;
    }

    private void addTermToDB(HashMap<Integer, HashMap<String, Double>> termGrades, String studentId) throws SQLException {
        Connection connection = ConnectionPool.getConnection();
        for (int termNumber : termGrades.keySet()) {
            for (String courseCode : termGrades.get(termNumber).keySet()) {

                if (!checkTermRepeating(courseCode, termNumber, studentId))
                    continue;

                PreparedStatement statement = connection.prepareStatement(String.format(
                        "insert into %s (courseCode, termNumber, grade, studentId)"
                                + " values (?, ?, ?, ?);", TermTableName));

                statement.setString(1, courseCode);
                statement.setInt(2, termNumber);
                statement.setDouble(3, termGrades.get(termNumber).get(courseCode));
                statement.setString(4, studentId);

                statement.executeUpdate();
                statement.close();
            }
        }
        connection.close();
    }

    public boolean checkTermRepeating(String courseCode, int termNumber, String studentId) throws SQLException {
        Connection connection = ConnectionPool.getConnection();

        PreparedStatement statement = connection.prepareStatement(
                String.format("select courseCode from %s t where t.courseCode = ? and t.termNumber = ? and t.studentId = ?;", TermTableName));
        statement.setString(1, courseCode);
        statement.setInt(2, termNumber);
        statement.setString(3, studentId);
        ResultSet result = statement.executeQuery();
        boolean exist = result.next();
        result.close();
        statement.close();
        connection.close();
        return !exist;
    }


    public void getCoursesFromAPI() throws Exception {
        String response = sendGetRequestToURL("http://138.197.181.131:5200/api/courses");
        JsonNode jsonNode = objectMapper.readTree(response);

        for (int i = 0; i < jsonNode.size(); i++)
        {
            addOffering(jsonNode.get(i).toString(), false);
        }

        for (int i = 0; i < jsonNode.size(); i++)
        {
            addOffering(jsonNode.get(i).toString(), true);
        }

    }

    public String addStudent(String data) throws JsonProcessingException, SQLException {
        String studentId, name, secondName, birthDate, field, faculty, level, status, img, email, password;
        try {
            JsonNode jsonNode = objectMapper.readTree(data);
            studentId = jsonNode.get("id").asText();
            name = jsonNode.get("name").asText();
            secondName = jsonNode.get("secondName").asText();
            birthDate = jsonNode.get("birthDate").asText();
            field = jsonNode.get("field").asText();;
            faculty = jsonNode.get("faculty").asText();
            level = jsonNode.get("level").asText();
            status = jsonNode.get("status").asText();
            img = jsonNode.get("img").asText();
            email = jsonNode.get("email").asText();
            password = jsonNode.get("password").asText();

        } catch (Exception e) {
            return createJsonOutput("false", "Your input was in wrong format!");
        }
        if (checkStudentIdRepeating(studentId)) {
            this.addStudentToDB(studentId, name, secondName, birthDate, field, faculty, level, status, img, email, password);
            return createJsonOutput("true", "Classes.Student '" + studentId + "' successfully added.");
        }
        return createJsonOutput("false", "This 'studentId' has already been added before!");
    }

    private void addStudentToDB(String studentId, String name, String secondName,
                                String birthDate, String field, String faculty,
                                String level, String status, String img,
                                String email, String password) throws SQLException {

        Connection connection = ConnectionPool.getConnection();
        PreparedStatement statement = connection.prepareStatement(String.format(
                "insert into %s (studentId, name, secondName, birthDate, field, faculty, level, status, img, email, password)"
                        + "values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);", StudentTableName));
        statement.setString(1, studentId);
        statement.setString(2, name);
        statement.setString(3, secondName);
        statement.setString(4, birthDate);
        statement.setString(5, field);
        statement.setString(6, faculty);
        statement.setString(7, level);
        statement.setString(8, status);
        statement.setString(9, img);
        statement.setString(10, email);

        String passwordHash = Hashing.sha256().hashString(password, StandardCharsets.UTF_8).toString();
        statement.setString(11, passwordHash);

        statement.executeUpdate();
        statement.close();
        connection.close();
    }

    public boolean checkStudentIdRepeating(String studentId) throws SQLException {
        Connection connection = ConnectionPool.getConnection();
        PreparedStatement statement = connection.prepareStatement(
                String.format("select studentId from %s s where s.studentId = ?;", StudentTableName));
        statement.setString(1, studentId);
        ResultSet result = statement.executeQuery();
        boolean exist = result.next();
        result.close();
        statement.close();
        connection.close();
        return !exist;
    }

    public String addOffering(String data, boolean addPrerequisites) throws JsonProcessingException, SQLException {
        String code, name, instructor, classTime, classCode, type, examTimeStart, examTimeEnd;
        Integer units, capacity;
        ArrayList<String> classDays = new ArrayList<String>();
        ArrayList<String> prerequisites = new ArrayList<String>();

        try {
            JsonNode jsonNode = objectMapper.readTree(data);
            code = jsonNode.get("code").asText();
            classCode = jsonNode.get("classCode").asText();
            name = jsonNode.get("name").asText();
            instructor = jsonNode.get("instructor").asText();
            type = jsonNode.get("type").asText();
            units = jsonNode.get("units").asInt();
            capacity = jsonNode.get("capacity").asInt();
            classTime = jsonNode.at("/classTime/time").asText();
            examTimeStart = jsonNode.at("/examTime/start").asText();
            examTimeEnd = jsonNode.at("/examTime/end").asText();
            JsonNode classDaysNode = jsonNode.at("/classTime/days");
            for (JsonNode day : classDaysNode) {
                classDays.add(day.asText());
            }
            JsonNode prerequisitesNode = jsonNode.at("/prerequisites");
            for (JsonNode prerequisite : prerequisitesNode) {
                prerequisites.add(prerequisite.asText());
            }
        } catch (Exception e) {
            return createJsonOutput("false", "Your input was in wrong format!");
        }
        if (addPrerequisites) {
            if (checkPrerequisitesRepeating(code)) {
                this.addPrerequisitesToDB(code, prerequisites);
            }
        }

        if (checkCourseIdRepeating(code)) {
            this.addCourseToDB(code, name, type, units, examTimeStart, examTimeEnd);
            if (checkOfferingRepeating(code, classCode))
                this.addOfferingToDB(code, classCode, instructor, capacity, classDays, classTime);
            return createJsonOutput("true", "Classes.Course '" + code + "' successfully added.");
        }
        return createJsonOutput("false", "This 'code' has already been added before!");
    }

    private void addOfferingToDB(String courseCode, String classCode, String instructor, int capacity,
                                 ArrayList<String> classDays, String classTime) throws SQLException {
        Connection connection = ConnectionPool.getConnection();
        String firstClassDay = classDays.get(0);
        String secondClassDay = classDays.get(0);

        if (classDays.size() > 1)
            secondClassDay = classDays.get(1);

        PreparedStatement statement = connection.prepareStatement(String.format(
                "insert into %s (courseCode, classCode, instructor, firstClassDay, secondClassDay, classTime, capacity)"
                        + " values (?, ?, ?, ?, ?, ?, ?);", OfferingTableName));

        statement.setString(1, courseCode);
        statement.setString(2, classCode);
        statement.setString(3, instructor);
        statement.setString(4, firstClassDay);
        statement.setString(5, secondClassDay);
        statement.setString(6, classTime);
        statement.setInt(7, capacity);

        statement.executeUpdate();
        statement.close();
        connection.close();
    }

    private void addPrerequisitesToDB(String courseCode, ArrayList<String> prerequisites) throws SQLException {
        Connection connection = ConnectionPool.getConnection();
        for (String prerequisit : prerequisites) {
            PreparedStatement statement = connection.prepareStatement(String.format(
                    "insert into %s (mainCourseCode, id, prerequisitCourseCode)"
                            + " values (?, 0, ?);", PrerequisitTableName));
            statement.setString(1, courseCode);
            statement.setString(2, prerequisit);

            statement.executeUpdate();
            statement.close();
        }
        connection.close();
    }

    private void addCourseToDB(String courseCode, String name, String type, int units,
                               String examTimeStart, String examTimeEnd) throws SQLException {

        Connection connection = ConnectionPool.getConnection();
        PreparedStatement statement = connection.prepareStatement(String.format(
                "insert into %s (courseCode, name, type, units, examTimeStart, examTimeEnd)"
                        + " values (?, ?, ?, ?, ?, ?);", CourseTableName));
        statement.setString(1, courseCode);
        statement.setString(2, name);
        statement.setString(3, type);
        statement.setInt(4, units);
        statement.setString(5, examTimeStart);
        statement.setString(6, examTimeEnd);

        statement.executeUpdate();
        statement.close();
        connection.close();
    }

    public boolean checkPrerequisitesRepeating(String code) throws SQLException {
        Connection connection = ConnectionPool.getConnection();
        PreparedStatement statement = connection.prepareStatement(
                String.format("select mainCourseCode from %s p where p.mainCourseCode = ?;", PrerequisitTableName));
        statement.setString(1, code);
        ResultSet result = statement.executeQuery();
        boolean exist = result.next();
        result.close();
        statement.close();
        connection.close();
        return !exist;
    }

    public boolean checkOfferingRepeating(String code, String classCode) throws SQLException {
        Connection connection = ConnectionPool.getConnection();
        PreparedStatement statement = connection.prepareStatement(
                String.format("select courseCode from %s o where o.courseCode = ? and o.classCode = ?;", OfferingTableName));
        statement.setString(1, code);
        statement.setString(2, classCode);
        ResultSet result = statement.executeQuery();
        boolean exist = result.next();
        result.close();
        statement.close();
        connection.close();
        return !exist;
    }

    public boolean checkCourseIdRepeating(String code) throws SQLException {
        Connection connection = ConnectionPool.getConnection();
        PreparedStatement statement = connection.prepareStatement(
                String.format("select courseCode from %s c where c.courseCode = ?;", CourseTableName));
        statement.setString(1, code);
        ResultSet result = statement.executeQuery();
        boolean exist = result.next();
        result.close();
        statement.close();
        connection.close();
        return !exist;
    }

    public String createJsonOutput(String status, String message) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode rootNode = mapper.createObjectNode();
        rootNode.put("success", status);
        rootNode.put("data", message);
        String jsonOutput = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(rootNode);
        return jsonOutput;
    }

    public void addToWeeklySchedule(Student student, Course course) throws Exception {
            student.addToWeeklySchedule(course, false);
    }

    public Student getStudent(String email) throws StudentNotFoundException, SQLException, CourseNotFoundException {
        Connection connection = ConnectionPool.getConnection();
        PreparedStatement statement = connection.prepareStatement(
                String.format("select * from %s s where s.email = ?;", StudentTableName));
        statement.setString(1, email);
        ResultSet result = statement.executeQuery();
        Student student = null;
        boolean exist = result.next();
        if (exist)
            student = new Student(result.getString("studentId"),
                               result.getString("name"),
                               result.getString("secondName"),
                               result.getString("birthDate"),
                               result.getString("field"),
                               result.getString("faculty"),
                               result.getString("level"),
                               result.getString("status"),
                               result.getString("img"),
                               result.getString("email"),
                               result.getString("password"));

        String studentId = result.getString("studentId");
        result.close();
        PreparedStatement grades_statement = connection.prepareStatement(
                String.format("select * from %s where studentId = ? order by termNumber;", TermTableName));
        grades_statement.setString(1, studentId);
        ResultSet grades_result = grades_statement.executeQuery();

        HashMap<Integer, HashMap<String, Double>> termGrades = new HashMap<>();
        Integer termNumber = 0;
        while (grades_result.next()){
            Integer newTermNumber = grades_result.getInt("termNumber");
            if (termNumber != newTermNumber){
                termNumber = newTermNumber;
                termGrades.put(termNumber, new HashMap<String, Double>());
            }
            String code = grades_result.getString("courseCode");
            Double grade = grades_result.getDouble("grade");
            termGrades.get(termNumber).put(code, grade);
        }

        if (exist) {
            student.addTermGrade(termGrades);
            student.calculateGpa();
        }
        statement.close();
        connection.close();
        return student;
    }

    public Course getCourse(String courseCode, String classCode) throws CourseNotFoundException, SQLException {
        Connection connection = ConnectionPool.getConnection();
        PreparedStatement statement = connection.prepareStatement(
                String.format("select * from %s c where c.courseCode = ?;", CourseTableName));
        statement.setString(1, courseCode);
        ResultSet result = statement.executeQuery();

        Course course = null;
        boolean exist = result.next();

        PreparedStatement prerequisitesStatement = connection.prepareStatement(
                String.format("select prerequisitCourseCode from %s c where c.mainCourseCode = ?;", PrerequisitTableName));
        prerequisitesStatement.setString(1, courseCode);
        ResultSet prerequisitesResult = prerequisitesStatement.executeQuery();
        ArrayList<String> prerequisites = new ArrayList<String>();

        while (prerequisitesResult.next())
            prerequisites.add(prerequisitesResult.getString("prerequisitCourseCode"));

        PreparedStatement offeringStatement = connection.prepareStatement(
                String.format("select * from %s o where o.courseCode = ? and o.classCode = ?;", OfferingTableName));
        offeringStatement.setString(1, courseCode);
        offeringStatement.setString(2, classCode);
        ResultSet offeringResult = offeringStatement.executeQuery();
        boolean offeringExist = offeringResult.next();

        ArrayList<String> classDays = new ArrayList<String>();
        classDays.add(offeringResult.getString("firstClassDay"));
        classDays.add(offeringResult.getString("secondClassDay"));
        HashMap<String, LocalDateTime> examTime = new HashMap<>();
        examTime.put("start", LocalDateTime.parse(result.getString("examTimeStart")));
        examTime.put("end", LocalDateTime.parse(result.getString("examTimeEnd")));

        if (exist && offeringExist)
            course = new Course(result.getString("courseCode"),
                    classCode,
                    result.getString("name"),
                    offeringResult.getString("instructor"),
                    result.getString("type"),
                    result.getInt("units"),
                    classDays,
                    offeringResult.getString("classTime"),
                    examTime,
                    offeringResult.getInt("capacity"),
                    prerequisites);

        result.close();
        prerequisitesResult.close();
        offeringResult.close();
        offeringStatement.close();
        statement.close();
        prerequisitesStatement.close();
        connection.close();
        if (exist && offeringExist)
            return course;
        throw new CourseNotFoundException();
    }

    public Course getCourse(String courseCode) throws CourseNotFoundException, SQLException {
        Connection connection = ConnectionPool.getConnection();
        PreparedStatement offeringStatement = connection.prepareStatement(
                String.format("select * from %s o where o.courseCode = ?;", OfferingTableName));
        offeringStatement.setString(1, courseCode);
        ResultSet offeringResult = offeringStatement.executeQuery();
        boolean offeringExist = offeringResult.next();
        String classCode = "";
        if (offeringExist)
            classCode = offeringResult.getString("classCode");

        offeringResult.close();
        offeringStatement.close();
        connection.close();
        return getCourse(courseCode, classCode);
    }

    public ArrayList<Course> getCourses() throws SQLException, CourseNotFoundException {
        ArrayList<Course> allCourses = new ArrayList<>();
        Connection connection = ConnectionPool.getConnection();
        PreparedStatement statement = connection.prepareStatement(
                String.format("select * from %s;", OfferingTableName));
        ResultSet result = statement.executeQuery();

        while (result.next()) {
            String courseCode = result.getString("courseCode");
            String classCode = result.getString("classCode");
            allCourses.add(getCourse(courseCode, classCode));

        }

        result.close();
        statement.close();
        connection.close();

        return allCourses;
    }


    public String FillTemplates(String address, HashMap<String, String> content) throws IOException, URISyntaxException {
        File htmlTemplateFile = new File(Resources.getResource(address).toURI());
        String htmlString = FileUtils.readFileToString(htmlTemplateFile, StandardCharsets.UTF_8);
        for(Map.Entry<String, String> entry : content.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            htmlString = htmlString.replace(key, value);
        }
        return htmlString;
    }
    public void checkWaitingLists() throws AlreadyAddedCourseToPlanException, ExamsTimeColisionException, ClassesTimeCollisionException, SQLException, CourseNotFoundException {
        /// TODO: this
        for (Course course : this.courses) {
            course.checkWaitingList();
        }
    }
}
