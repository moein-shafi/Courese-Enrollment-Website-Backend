package ie.diyar_moein_ca5.controllers.models;

import ie.diyar_moein_ca5.Classes.Database;
import ie.diyar_moein_ca5.Classes.Student;

import java.util.HashMap;

public class ProfileModel {
    private Student student;
    private HashMap<String, Double> passedCourses = new HashMap<>();
    private Integer code;
    private String message;

    public ProfileModel() {
        Database database = Database.getDatabase();
        student = database.getCurrentStudent();

        if (student == null)
        {
            code = 404;
            message = "login first!";
        }

        else {
            code = 200;
            message = "profile data is ready.";
            for (HashMap<String, Double> grades : student.getTermGrades().values()) {
                for (String code : grades.keySet()) {
                    if (grades.get(code) < 10)
                        continue;
                    passedCourses.put(code, grades.get(code));
                }
            }
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
}
