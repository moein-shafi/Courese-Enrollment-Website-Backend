package ie.diyar_moein_ca5.Classes;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.io.Resources;
import ie.diyar_moein_ca5.Exceptions.*;
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
    ArrayList<Student> students;
    ArrayList<Course> courses;
    ObjectMapper objectMapper;
    Student currentStudent = null;
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
        students = new ArrayList<Student>();
        courses = new ArrayList<Course>();
        objectMapper = new ObjectMapper();
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
        command += String.format("\nimg CHAR(225), \nPRIMARY KEY(studentId));");
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
        command += String.format("\nPRIMARY KEY (courseCode, termNumber),");
        command += String.format("\nFOREIGN KEY (courseCode) REFERENCES %s(courseCode));", CourseTableName);

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

    public void setCurrentStudent(String studentId) throws StudentNotFoundException {
        this.currentStudent = database.getStudent(studentId);
    }

    public Student getCurrentStudent() {
        return this.currentStudent;
    }

    public void logout() {
        this.currentStudent = null;
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
        String response = sendGetRequestToURL("http://138.197.181.131:5100/api/students");
        JsonNode jsonNode = objectMapper.readTree(response);
        for (int i = 0; i < jsonNode.size(); i++)
        {
            addStudent(jsonNode.get(i).toString());
        }
    }

    public void getGradesFromAPI() throws Exception {
        for (Student student:students){
            String response = sendGetRequestToURL("http://138.197.181.131:5100/api/grades/"+student.getStudentId());
            JsonNode jsonNode = objectMapper.readTree(response);
            HashMap<Integer, HashMap<String, Double>> termGrades = new HashMap<>();
            for (int i = 0; i < jsonNode.size(); i++)
            {
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
            student.addTermGrade(termGrades);
            student.calculateGpa();
        }
    }

    public void getCoursesFromAPI() throws Exception {
        String response = sendGetRequestToURL("http://138.197.181.131:5100/api/courses");
        JsonNode jsonNode = objectMapper.readTree(response);

        for (int i = 0; i < jsonNode.size(); i++)
        {
            addOffering(jsonNode.get(i).toString());
        }
    }

    public String addStudent(String data) throws JsonProcessingException, SQLException {
        String studentId, name, secondName, birthDate, field, faculty, level, status, img;
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

        } catch (Exception e) {
            return createJsonOutput("false", "Your input was in wrong format!");
        }
        if (checkStudentIdRepeating(studentId)) {
            this.addStudentToDB(studentId, name, secondName, birthDate, field, faculty, level, status, img);
            return createJsonOutput("true", "Classes.Student '" + studentId + "' successfully added.");
        }
        return createJsonOutput("false", "This 'studentId' has already been added before!");
    }

    private void addStudentToDB(String studentId, String name, String secondName,
                                String birthDate, String field, String faculty,
                                String level, String status, String img) throws SQLException {

        Connection connection = ConnectionPool.getConnection();
        PreparedStatement statement = connection.prepareStatement(String.format(
                "insert into %s (studentId, name, secondName, birthDate, field, faculty, level, status, img)"
                        + "values (?, ?, ?, ?, ?, ?, ?, ?, ?);", StudentTableName));
        statement.setString(1, studentId);
        statement.setString(2, name);
        statement.setString(3, secondName);
        statement.setString(4, birthDate);
        statement.setString(5, field);
        statement.setString(6, faculty);
        statement.setString(7, level);
        statement.setString(8, status);
        statement.setString(9, img);

        statement.executeUpdate();
        statement.close();
        connection.close();
    }

    public boolean checkStudentIdRepeating(String studentId) throws SQLException{
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

    public String addOffering(String data) throws JsonProcessingException {
        String code, name, instructor, classTime, classCode, type, examTimeStartString, examTimeEndString;
        Integer units, capacity;
        HashMap<String, LocalDateTime> examTime = new HashMap<String, LocalDateTime>();
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
            examTimeStartString = jsonNode.at("/examTime/start").asText();
            examTimeEndString = jsonNode.at("/examTime/end").asText();
            examTime.put("start", LocalDateTime.parse(examTimeStartString));
            examTime.put("end", LocalDateTime.parse(examTimeEndString));
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

        Course course = new Course(code, classCode, name, instructor, type, units, classDays, classTime, examTime, capacity,
                prerequisites);
        if (checkCourseIdRepeating(code)) {
            this.courses.add(course);
            return createJsonOutput("true", "Classes.Course '" + code + "' successfully added.");
        }
        return createJsonOutput("false", "This 'code' has already been added before!");
    }

    public boolean checkCourseIdRepeating(String code)
    {
        for (Course course : this.courses)
            if (course.getCode().equals(code))
                return false;
        return true;
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

    public Student getStudent(String studentId) throws StudentNotFoundException {
        for (Student s: this.students)
        {
            if (s.getStudentId().equals(studentId))
                return s;
        }
        throw new StudentNotFoundException();
    }

    public Course getCourse(String code, String classCode) throws CourseNotFoundException {
        for (Course c: this.courses)
        {
            if (c.getCode().equals(code) && c.getClassCode().equals(classCode))
                return c;
        }
        throw new CourseNotFoundException();
    }

    public Course getCourse(String code) throws CourseNotFoundException {
        for (Course c: this.courses)
        {
            if (c.getCode().equals(code))
                return c;
        }
        throw new CourseNotFoundException();
    }

    public ArrayList<Course> getCourses() { return courses; }


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
    public void checkWaitingLists() throws AlreadyAddedCourseToPlanException, ExamsTimeColisionException, ClassesTimeCollisionException {
        for (Course course : this.courses) {
            course.checkWaitingList();
        }
    }
}
