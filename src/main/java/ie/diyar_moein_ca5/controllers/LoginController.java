package ie.diyar_moein_ca5.controllers;

import ie.diyar_moein_ca5.Classes.Database;
import ie.diyar_moein_ca5.Exceptions.CourseNotFoundException;
import ie.diyar_moein_ca5.Exceptions.StudentNotFoundException;
import ie.diyar_moein_ca5.Exceptions.WrongPasswordException;
import ie.diyar_moein_ca5.controllers.Requests.JWTLoginRequest;
import ie.diyar_moein_ca5.controllers.Response.JWTTokenResponse;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.util.HashMap;

@RestController
public class LoginController {

    @PostMapping(value = "/login", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> login(@RequestBody JWTLoginRequest request) {
        try{
            String token =  Database.getDatabase().login(request.getEmail(), request.getPassword());
            return  ResponseEntity.status(HttpStatus.OK).body(new JWTTokenResponse(token,request.getEmail()));

            /// TODO: add 403 for these
        } catch (WrongPasswordException | StudentNotFoundException | SQLException | CourseNotFoundException e) {
            return  ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());

        }
    }


    /// TODO: handle logout in frontend
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
