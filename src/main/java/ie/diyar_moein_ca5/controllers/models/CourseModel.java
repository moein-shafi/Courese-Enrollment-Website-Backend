package ie.diyar_moein_ca5.controllers.models;

import ie.diyar_moein_ca5.Classes.Course;
import ie.diyar_moein_ca5.Classes.Database;
import ie.diyar_moein_ca5.Classes.Student;
import ie.diyar_moein_ca5.Exceptions.CourseNotFoundException;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

public class CourseModel {
    private Student student;
    private ArrayList<HashMap<String, String>> selectedCourses = new ArrayList<>();
    private HashMap<String, Course> allCourses = new HashMap<>();
    private Integer code;
    private String message;

    public CourseModel() throws SQLException, CourseNotFoundException {
        Database database = Database.getDatabase();
        student = database.getCurrentStudent();
        if (student == null) {
            code = 401;
            message = "login first!";
        } else {
            code = 200;
            message = "courses data is ready.";

            for (Course course : database.getCourses()) {
                allCourses.put(course.getCode() + course.getClassCode(), course);
            }
        }
    }
    public Student getStudent() {
        return student;
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
}
