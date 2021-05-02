package ie.diyar_moein_ca5.controllers;

import ie.diyar_moein_ca5.Classes.Course;
import ie.diyar_moein_ca5.Classes.Database;
import ie.diyar_moein_ca5.Classes.Student;
import ie.diyar_moein_ca5.Exceptions.*;
import ie.diyar_moein_ca5.controllers.models.CourseModel;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

@RestController
public class CourseController {
    @PutMapping(value = "/course", produces = MediaType.APPLICATION_JSON_VALUE)
    public HashMap<String, String> AddCourse(
            @RequestParam(value = "courseCode") String courseCode,
            @RequestParam(value = "classCode") String classCode,
            @RequestParam(value = "isWaiting", defaultValue = "false") boolean isWaiting) throws SQLException, StudentNotFoundException {

        Database database = Database.getDatabase();
        HashMap<String, String> response = new HashMap<>();
        String message, code = "200";
        try {
            Student student = database.getCurrentStudent();

            Course course = database.getCourse(courseCode, classCode);
            student.addToWeeklySchedule(database.getCourse(courseCode, classCode), isWaiting);
            if (isWaiting)
                message = course.getName() + " Successfully added to waiting list.";

            else
                message = course.getName() + " Successfully added.";

        } catch (CourseNotFoundException e) {
            message = "Course Not Found!";
            code = "404";
            database.setErrorMessage(message);

        } catch (ClassesTimeCollisionException e) {
            message = "Classes Time Collision Error For: " + e.getMessage();
            code = "400";
            database.setErrorMessage(message);

        }catch (ExamsTimeColisionException e) {
            message = "Exams Time Collision Error For: " + e.getMessage();
            code = "400";
            database.setErrorMessage(message);

        } catch (AlreadyAddedCourseToPlanException e) {
            message = "Course " + e.getMessage() + " Already Added To Plan!";
            code = "400";
            database.setErrorMessage(message);
        }
        response.put("code", code);
        response.put("message", message);
        return response;
    }

    @DeleteMapping(value = "/course", produces = MediaType.APPLICATION_JSON_VALUE)
    public HashMap<String, String> RemoveCourse(@RequestParam(value = "courseCode") String courseCode) throws SQLException, StudentNotFoundException, CourseNotFoundException {
        Database database = Database.getDatabase();
        Student student = database.getCurrentStudent();
        Student.AddedOffering offering = student.getAddedOfferings().get(courseCode);
        offering.setToRemove();
        HashMap<String, String> response = new HashMap<>();
        String message;
        response.put("code", "200");
        response.put("message", offering.getCourse().getName() + " successfully removed.");
        return response;
    }

    @GetMapping(value = "/course", produces = MediaType.APPLICATION_JSON_VALUE)
    public CourseModel course() throws SQLException, CourseNotFoundException, StudentNotFoundException {
        Database database = Database.getDatabase();
        return new CourseModel();
    }

    @PostMapping(value = "/course", produces = MediaType.APPLICATION_JSON_VALUE)
    public HashMap<String, String> Submit(@RequestParam(value = "action") String action) throws SQLException, StudentNotFoundException, CourseNotFoundException {
        Database database = Database.getDatabase();
        HashMap<String, String> response = new HashMap<>();
        String message = "Successfully submitted.";
        String code = "200";
        Student student = database.getCurrentStudent();
        try {
            ArrayList<String> forDeleteCourses = new ArrayList<String>();
            if (action.equals("submit")) {
                student.checkFinalizeConditions();
                for (Student.AddedOffering offering : student.getAddedOfferings().values()) {
                    if (offering.isWantsToRemove())
                        forDeleteCourses.add(offering.getCourse().getCode());
                    else if (offering.getFinalized() == Student.Status.finalized)
                        continue;
                    else
                        offering.makeFinalize();
                }

            }
            else {

                for (Student.AddedOffering offering : student.getAddedOfferings().values()) {
                    if (offering.getFinalized() == Student.Status.non_finalized)
                        forDeleteCourses.add(offering.getCourse().getCode());

                    if (offering.isWantsToRemove())
                        offering.cancelRemoving();
                }
                message = "Successfully reset.";
            }
            for (String courseCode : forDeleteCourses)
                student.removeFromWeeklySchedule(courseCode);

        } catch (CourseNotFoundException e) {
            message = "Course Not Found!";
            code = "404";
            database.setErrorMessage(message);

        } catch (PrerequisiteException e) {
            message = "Prerequisite Error For Course: " + e.getMessage();
            code = "400";
            database.setErrorMessage(message);

        } catch (MinimumRequiredUnitsException e) {
            message = "Minimum Required Units Error!";
            code = "400";
            database.setErrorMessage(message);

        } catch (MaximumAllowedUnitsException e) {
            message = "Maximum Allowed Units Error!";
            code = "400";
            database.setErrorMessage(message);


        } catch (CourseCapacityException e) {
            message = "Course Capacity Error For Course: " + e.getMessage();
            code = "400";
            database.setErrorMessage(message);

        } catch (AlreadyPassedCourseException e) {
            message = "Already Passed Course: " + e.getMessage();
            code = "400";
            database.setErrorMessage(message);
        }

        response.put("code", code);
        response.put("message", message);
        return response;
    }

}
