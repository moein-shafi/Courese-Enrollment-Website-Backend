package ie.diyar_moein_ca5.controllers;

import ie.diyar_moein_ca5.Classes.Database;
import ie.diyar_moein_ca5.Exceptions.CourseNotFoundException;
import ie.diyar_moein_ca5.Exceptions.StudentAlreadySignedUpException;
import ie.diyar_moein_ca5.Exceptions.StudentNotFoundException;
import ie.diyar_moein_ca5.Exceptions.WrongPasswordException;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLException;
import java.util.HashMap;

@RestController
public class SignupController {

    @PostMapping(value = "/signup", produces = MediaType.APPLICATION_JSON_VALUE)
    public HashMap<String, String> signup(@RequestParam(value = "studentId") String studentId,
                                          @RequestParam(value = "firstName") String firstName,
                                          @RequestParam(value = "secondName") String secondName,
                                          @RequestParam(value = "birthDate") String birthDate,
                                          @RequestParam(value = "field") String field,
                                          @RequestParam(value = "faculty") String faculty,
                                          @RequestParam(value = "level") String level,
                                          @RequestParam(value = "email") String email,
                                          @RequestParam(value = "password") String password
                                          ) throws SQLException {
        Database database = Database.getDatabase();
        HashMap<String, String> response = new HashMap<>();
        try {
            /// TODO: check email repeated.
            /// TODO: make email as 'UNIQUE' in students table.
            database.signup(studentId, firstName, secondName, birthDate, field, faculty, level, email, password);
            response.put("code", "200");
            response.put("message", "signup successfully");
        } catch (StudentAlreadySignedUpException e) {
            database.setErrorMessage("Student " + studentId + "has already signed up.!");
            response.put("code", "404");
            response.put("message", database.getErrorMessage());
        }
        return response;
    }
}
