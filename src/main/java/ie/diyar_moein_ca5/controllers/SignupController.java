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
        HashMap<String, String> response = new HashMap<>();

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
            var jwtResponse = new JWTTokenResponse(token, request.getEmail());
            jwtResponse.setCode(200);
            jwtResponse.setMessgae("Signed Up!");
            return ResponseEntity.status(HttpStatus.OK).body(jwtResponse);

        } catch (StudentAlreadySignedUpException e) {
            response.put("code", String.valueOf(HttpStatus.NOT_ACCEPTABLE));
            response.put("message", "Student Already Signed Up!");
            return ResponseEntity.status(HttpStatus.OK).body(response);

        } catch (WrongPasswordException e) {
            response.put("code", String.valueOf(HttpStatus.FORBIDDEN));
            response.put("message", "Wrong password!");
            return ResponseEntity.status(HttpStatus.OK).body(response);

        } catch (SQLException e) {

            response.put("code", String.valueOf(HttpStatus.NOT_ACCEPTABLE));
            response.put("message", "DataBase Error!" + e);
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
