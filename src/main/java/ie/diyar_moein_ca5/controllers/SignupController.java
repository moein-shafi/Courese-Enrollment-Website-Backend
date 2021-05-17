package ie.diyar_moein_ca5.controllers;

import ie.diyar_moein_ca5.Classes.Database;
import ie.diyar_moein_ca5.Exceptions.CourseNotFoundException;
import ie.diyar_moein_ca5.Exceptions.StudentAlreadySignedUpException;
import ie.diyar_moein_ca5.Exceptions.StudentNotFoundException;
import ie.diyar_moein_ca5.Exceptions.WrongPasswordException;
import ie.diyar_moein_ca5.controllers.Requests.JWTSignupRequest;
import ie.diyar_moein_ca5.controllers.Response.JWTTokenResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLException;
import java.util.HashMap;

@RestController
public class SignupController {

    @PostMapping(value = "/signup", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> signup(@RequestBody final JWTSignupRequest request) {
        try {
            Database.getDatabase().signup(
                    request.getStudentId(),
                    request.getFirstName(),
                    request.getSecondName(),
                    request.getBirthDate(),
                    request.getField(),
                    request.getFaculty(),
                    request.getLevel(),
                    request.getEmail(),
                    request.getPassword());

            String token = Database.getDatabase().login(request.getEmail(), request.getPassword());
            return ResponseEntity.status(HttpStatus.OK).body(new JWTTokenResponse(token, request.getEmail()));
        } catch (StudentAlreadySignedUpException | SQLException | StudentNotFoundException |
                CourseNotFoundException | WrongPasswordException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }

    }
}
