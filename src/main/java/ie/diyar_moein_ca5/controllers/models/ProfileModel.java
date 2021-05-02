package ie.diyar_moein_ca5.controllers.models;

import ie.diyar_moein_ca5.Classes.Course;
import ie.diyar_moein_ca5.Classes.Database;
import ie.diyar_moein_ca5.Classes.Student;
import ie.diyar_moein_ca5.Exceptions.CourseNotFoundException;
import ie.diyar_moein_ca5.Exceptions.StudentNotFoundException;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

public class ProfileModel {
    private Student student;
    private HashMap<String, Double> passedCourses = new HashMap<>();
    private HashMap<Integer, ArrayList<Course>> termCourses = new HashMap<>();
    private Integer code;
    private String message;

    public ProfileModel() throws CourseNotFoundException, SQLException, StudentNotFoundException {
        Database database = Database.getDatabase();
        try {
            student = database.getCurrentStudent();
            code = 200;
            message = "profile data is ready.";
        }
        catch (StudentNotFoundException e)
        {
            code = 401;
            message = "login first!";
        }


        for (Integer termNumber : student.getTermGrades().keySet()) {
            ArrayList<Course> courses = new ArrayList<>();
                for (String code : student.getTermGrades().get(termNumber).keySet()) {
                    Double grade = student.getTermGrades().get(termNumber).get(code);
                    courses.add(database.getCourse(code));
                    if (grade < 10)
                        continue;
                    passedCourses.put(code, grade);
                }
            termCourses.put(termNumber, courses);
        }

    }

    public Student getStudent() {
        return student;
    }

    public Integer getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public HashMap<String, Double> getPassedCourses() {
        return passedCourses;
    }

    public HashMap<Integer, ArrayList<Course>> getTermCourses() {
        return termCourses;
    }
}
