package ie.diyar_moein_ca5.controllers;

import ie.diyar_moein_ca5.Classes.Database;
import ie.diyar_moein_ca5.Classes.Student;
import ie.diyar_moein_ca5.Exceptions.CourseNotFoundException;
import ie.diyar_moein_ca5.Exceptions.StudentNotFoundException;
import ie.diyar_moein_ca5.controllers.models.ProfileModel;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

@RestController
public class PlanController {

    @PostMapping(value = "/plan", produces = MediaType.APPLICATION_JSON_VALUE)
    public static HashMap<String, HashMap<String, String>> plan() throws SQLException, StudentNotFoundException, CourseNotFoundException {
        HashMap<String, HashMap<String, String>> weeklyPlan = new HashMap<>();
        Database database = Database.getDatabase();
        Student student = database.getCurrentStudent();

        ArrayList<String> days = new ArrayList<String>();
        days.add("Saturday");
        days.add("Sunday");
        days.add("Monday");
        days.add("Tuesday");
        days.add("Wednesday");
        for (String day : days) {
            HashMap<String, String> dayPlan = new HashMap<>();
            for (Student.AddedOffering offering : student.getAddedOfferings().values()) {
                if (offering.getFinalized() == Student.Status.non_finalized)
                    continue;
                if (offering.getCourse().getClassDays().contains(day)) {
                    dayPlan.put(offering.getCourse().getClassTime(), offering.getCourse().getName());
                }
            }
            weeklyPlan.put(day, dayPlan);
        }
        return weeklyPlan;
    }
}
