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
        HashMap<String, String> response = new HashMap<>();

        try{
            String token =  Database.getDatabase().login(request.getEmail(), request.getPassword());
            return  ResponseEntity.status(HttpStatus.OK).body(new JWTTokenResponse(token,request.getEmail()));

        } catch (WrongPasswordException e) {
            response.put("code", String.valueOf(HttpStatus.FORBIDDEN));
            response.put("message", "Wrong password!");
            return ResponseEntity.status(HttpStatus.OK).body(response);

        } catch (SQLException e) {
            response.put("code", String.valueOf(HttpStatus.NOT_ACCEPTABLE));
            response.put("message", "DataBase Error!");
            return ResponseEntity.status(HttpStatus.OK).body(response);

        } catch (StudentNotFoundException e) {
            response.put("code", String.valueOf(HttpStatus.NOT_FOUND));
            response.put("message", "Student Not Found!");
            return ResponseEntity.status(HttpStatus.OK).body(response);

        } catch (CourseNotFoundException e) {
            response.put("code", String.valueOf(HttpStatus.NOT_FOUND));
            response.put("message", "Course Not Found!");
            return ResponseEntity.status(HttpStatus.OK).body(response);
        }
    }
}
