package ie.diyar_moein_ca5.controllers.models;

import ie.diyar_moein_ca5.Classes.Student;

import java.util.HashMap;

public class ProfileModel {
    private Student student;
    private HashMap<String, Double> passedCourses = new HashMap<>();
    private Integer code;
    private String message;

    public ProfileModel(Student student) {
        if (student == null)
        {
            code = 404;
            message = "login first!";
        }

        else {
            code = 200;
            message = "profile data is ready.";
            this.student = student;
            for (String code : student.getGrades().keySet()) {
                if (student.getGrades().get(code) < 10)
                    continue;
                passedCourses.put(code, student.getGrades().get(code));
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
