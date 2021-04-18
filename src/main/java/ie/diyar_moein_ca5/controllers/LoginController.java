package ie.diyar_moein_ca5.controllers;

import ie.diyar_moein_ca5.Classes.Database;
import ie.diyar_moein_ca5.Exceptions.StudentNotFoundException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;

@RestController
public class LoginController {

    @PostMapping("/login")
    public HashMap<String, String> hello(@RequestParam(value = "studentId") String studentId) {
        Database database = Database.getDatabase();
        HashMap<String, String> response = new HashMap<>();
        try {
            database.setCurrentStudent(studentId);
            response.put("code", "200");
            response.put("message", "login successful");
        } catch (StudentNotFoundException e) {
            database.setErrorMessage("Student Not Found!");
            response.put("code", "404");
            response.put("message", database.getErrorMessage());
        }
        return response;
    }
}
