package ie.diyar_moein_ca5.controllers.models;

import ie.diyar_moein_ca5.Classes.Course;
import ie.diyar_moein_ca5.Classes.Database;
import ie.diyar_moein_ca5.Classes.Student;
import ie.diyar_moein_ca5.Exceptions.CourseNotFoundException;
import ie.diyar_moein_ca5.Exceptions.StudentNotFoundException;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

public class CourseModel {
    private ArrayList<HashMap<String, String>> selectedCourses = new ArrayList<>();
    private HashMap<String, Course> allCourses = new HashMap<>();
    private Integer code;
    private String message;
    private Student student;

    public CourseModel(String email) throws SQLException, CourseNotFoundException, StudentNotFoundException {
        Database database = Database.getDatabase();
        student = database.getStudent(email);
        code = 200;
        message = "courses data is ready.";

        for (Course course : database.getCourses()) {
            allCourses.put(course.getCode() + course.getClassCode(), course);
        }

    }

    public String getMessage() {
        return message;
    }

    public HashMap<String, Course> getAllCourses() {
        return allCourses;
    }

    public Integer getCode() {
        return code;
    }

    public Student getStudent() {
        return student;
    }
}
