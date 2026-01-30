package meadhead.poc.controller.view;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginViewController {

    @GetMapping("/login")
    //@ResponseBody // test du controler
    public String loginPage() {
        return "login";
    }

    @GetMapping("/dashboard")
    public String dashboardPage() {
        return "dashboard";
    }

    @GetMapping("/search-hospital")
    public String searchHospitalPage() {
        return "search-hospital"; // Cherchera le fichier search-hospital.html dans src/main/resources/templates
    }
}
