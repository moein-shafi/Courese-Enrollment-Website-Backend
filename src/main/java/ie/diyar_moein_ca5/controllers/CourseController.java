package ie.diyar_moein_ca5.controllers;

import ie.diyar_moein_ca5.Classes.Course;
import ie.diyar_moein_ca5.Classes.Database;
import ie.diyar_moein_ca5.Classes.Student;
import ie.diyar_moein_ca5.Exceptions.*;
import ie.diyar_moein_ca5.controllers.Requests.JWTAddCourseRequest;
import ie.diyar_moein_ca5.controllers.Requests.JWTDeleteCourseRequest;
import ie.diyar_moein_ca5.controllers.Requests.JWTSubmitCoursesRequest;
import ie.diyar_moein_ca5.controllers.models.CourseModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

@RestController
public class CourseController {

        @PutMapping(value = "/course", produces = MediaType.APPLICATION_JSON_VALUE)
        public ResponseEntity<?> AddCourse(@RequestAttribute("id") String email,
                                            @RequestBody final JWTAddCourseRequest request) {

        HashMap<String, String> response = new HashMap<>();
        String message, code = "200";
        try {
            Student student = Database.getDatabase().getStudent(email);
            String courseCode = request.getCourseCode();
            String classCode = request.getClassCode();
            boolean isWaiting = request.isWaiting();
            Course course = Database.getDatabase().getCourse(courseCode, classCode);
            student.addToWeeklySchedule(Database.getDatabase().getCourse(courseCode, classCode), isWaiting);
            if (isWaiting)
                message = course.getName() + " Successfully added to waiting list.";

            else
                message = course.getName() + " Successfully added.";

            response.put("code", code);
            response.put("message", message);
            return ResponseEntity.status(HttpStatus.OK).body(response);

        } catch (CourseNotFoundException e) {
            response.put("code", String.valueOf(HttpStatus.NOT_FOUND));
            response.put("message", "Course Not Found!");
            return ResponseEntity.status(HttpStatus.OK).body(response);

        } catch (ClassesTimeCollisionException e) {
            response.put("code", String.valueOf(HttpStatus.NOT_ACCEPTABLE));
            response.put("message", "Classes Time Collision Error For: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.OK).body(response);

        } catch (ExamsTimeColisionException e) {
            response.put("code", String.valueOf(HttpStatus.NOT_ACCEPTABLE));
            response.put("message", "Exams Time Collision Error For: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.OK).body(response);

        } catch (AlreadyAddedCourseToPlanException e) {
            response.put("code", String.valueOf(HttpStatus.NOT_ACCEPTABLE));
            response.put("message", "Course " + e.getMessage() + " Already Added To Plan!");
            return ResponseEntity.status(HttpStatus.OK).body(response);

        } catch (SQLException e) {
            response.put("code", String.valueOf(HttpStatus.NOT_ACCEPTABLE));
            response.put("message", "DataBase Error!");
            return ResponseEntity.status(HttpStatus.OK).body(response);

        } catch (StudentNotFoundException e) {
            response.put("code", String.valueOf(HttpStatus.NOT_FOUND));
            response.put("message", "Student Not Found!");
            return ResponseEntity.status(HttpStatus.OK).body(response);
        }

    }

    @DeleteMapping(value = "/course", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> RemoveCourse(@RequestAttribute("id") String email,
                                       @RequestBody final JWTDeleteCourseRequest request) {
        Student student = null;
        HashMap<String, String> response = new HashMap<>();

        try {
            student = Database.getDatabase().getStudent(email);
            String courseCode = request.getCourseCode();
            Student.AddedOffering offering = student.getAddedOfferings().get(courseCode);
            offering.setToRemove();
            String message;
            response.put("code", "200");
            response.put("message", offering.getCourse().getName() + " successfully removed.");
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

    @GetMapping(value = "/course", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> course(@RequestAttribute("id") String email) {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(new CourseModel(email));
        } catch (StudentNotFoundException | SQLException | CourseNotFoundException e){
            return  ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @PostMapping(value = "/course", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> course(@RequestAttribute("id") String email,
                                    @RequestBody final JWTSubmitCoursesRequest request) {
        HashMap<String, String> response = new HashMap<>();
        String message = "Successfully submitted.";
        String code = "200";
        String action = request.getAction();
        try {
            Student student = Database.getDatabase().getStudent(email);
            ArrayList<String> forDeleteCourses = new ArrayList<String>();
            if (action.equals("submit")) {
                student.checkFinalizeConditions();
                for (Student.AddedOffering offering : student.getAddedOfferings().values()) {
                    if (offering.isWantsToRemove())
                        forDeleteCourses.add(offering.getCourse().getCode());
                    else if (offering.getFinalized() == "finalized")
                        continue;
                    else
                    {
                        offering.makeFinalize();

                    }
                }
            }
            else {

                for (Student.AddedOffering offering : student.getAddedOfferings().values()) {
                    if (offering.getFinalized() == "non_finalized")
                        forDeleteCourses.add(offering.getCourse().getCode());

                    if (offering.isWantsToRemove())
                        offering.cancelRemoving();
                }
                message = "Successfully reset.";
            }
            for (String courseCode : forDeleteCourses)
                student.removeFromWeeklySchedule(courseCode);
            response.put("code", code);
            response.put("message", message);
            return ResponseEntity.status(HttpStatus.OK).body(response);


        } catch (CourseNotFoundException e) {
            response.put("code", String.valueOf(HttpStatus.NOT_FOUND));
            response.put("message", "Course Not Found!");
            return ResponseEntity.status(HttpStatus.OK).body(response);

        } catch (PrerequisiteException e) {
            response.put("code", String.valueOf(HttpStatus.NOT_ACCEPTABLE));
            response.put("message", "Prerequisite Error For Course: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.OK).body(response);

        } catch (MinimumRequiredUnitsException e) {
            response.put("code", String.valueOf(HttpStatus.NOT_ACCEPTABLE));
            response.put("message", "Minimum Required Units Error!");
            return ResponseEntity.status(HttpStatus.OK).body(response);

        } catch (MaximumAllowedUnitsException e) {
            response.put("code", String.valueOf(HttpStatus.NOT_ACCEPTABLE));
            response.put("message", "Maximum Allowed Units Error!");
            return ResponseEntity.status(HttpStatus.OK).body(response);

        } catch (CourseCapacityException e) {
            response.put("code", String.valueOf(HttpStatus.NOT_ACCEPTABLE));
            response.put("message", "Course Capacity Error For Course: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.OK).body(response);

        } catch (AlreadyPassedCourseException e) {
            response.put("code", String.valueOf(HttpStatus.NOT_ACCEPTABLE));
            response.put("message", "Already Passed Course: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.OK).body(response);

        } catch (SQLException e) {
            response.put("code", String.valueOf(HttpStatus.NOT_ACCEPTABLE));
            response.put("message", "DataBase Error!");
            return ResponseEntity.status(HttpStatus.OK).body(response);

        } catch (StudentNotFoundException e) {
            response.put("code", String.valueOf(HttpStatus.NOT_FOUND));
            response.put("message", "Student Not Found!");
            return ResponseEntity.status(HttpStatus.OK).body(response);
        }
    }
}
