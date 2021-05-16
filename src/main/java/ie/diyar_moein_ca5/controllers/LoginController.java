package ie.diyar_moein_ca5.controllers;

import ie.diyar_moein_ca5.Classes.Database;
import ie.diyar_moein_ca5.Exceptions.CourseNotFoundException;
import ie.diyar_moein_ca5.Exceptions.StudentNotFoundException;
import ie.diyar_moein_ca5.Exceptions.WrongPasswordException;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLException;
import java.util.HashMap;

@RestController
public class LoginController {

    @PostMapping(value = "/login", produces = MediaType.APPLICATION_JSON_VALUE)
    public HashMap<String, String> login(@RequestParam(value = "email") String email,
                                         @RequestParam(value = "password") String password) throws SQLException {
        Database database = Database.getDatabase();
        HashMap<String, String> response = new HashMap<>();
        try {
            database.setCurrentStudent(email, password);      /// TODO: Fix this
            response.put("code", "200");
            response.put("message", "login successfully");
        } catch (StudentNotFoundException | CourseNotFoundException e) {
            database.setErrorMessage("Student Not Found!");
            response.put("code", "404");
            response.put("message", database.getErrorMessage());
        }  catch (WrongPasswordException e) {
            database.setErrorMessage("Wrong Password!");
            response.put("code", "403");
            response.put("message", database.getErrorMessage());
        }
        return response;
    }

    @DeleteMapping(value ="/login", produces = MediaType.APPLICATION_JSON_VALUE)
    public HashMap<String, String> logout() throws SQLException {
        Database database = Database.getDatabase();
        HashMap<String, String> response = new HashMap<>();
        database.logout();
        response.put("code", "200");
        response.put("message", "logged out successfully");
        return response;
    }
}
