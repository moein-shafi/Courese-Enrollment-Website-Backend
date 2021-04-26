package ie.diyar_moein_ca5.controllers;

import ie.diyar_moein_ca5.Classes.Database;
import ie.diyar_moein_ca5.controllers.models.ProfileModel;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProfileController {

    @GetMapping(value = "/profile", produces = MediaType.APPLICATION_JSON_VALUE)
    public ProfileModel profile() {
        Database database = Database.getDatabase();
        return new ProfileModel();
    }
}
