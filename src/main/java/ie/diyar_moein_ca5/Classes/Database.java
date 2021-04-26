package ie.diyar_moein_ca5.Classes;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.io.Resources;
import ie.diyar_moein_ca5.Exceptions.*;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
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

    private Database() {
        students = new ArrayList<Student>();
        courses = new ArrayList<Course>();
        objectMapper = new ObjectMapper();
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String message) {
        this.errorMessage = message;
    }

    public static Database getDatabase() {
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

    public String addStudent(String data) throws JsonProcessingException {
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
        Student student = new Student(studentId, name, secondName, birthDate, field, faculty, level, status, img);
        if (checkStudentIdRepeating(studentId)) {
            this.students.add(student);
            return createJsonOutput("true", "Classes.Student '" + studentId + "' successfully added.");
        }
        return createJsonOutput("false", "This 'studentId' has already been added before!");
    }

    public boolean checkStudentIdRepeating(String studentId)
    {
        for (Student student : this.students)
            if (student.getStudentId().equals(studentId))
                return false;
        return true;
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
