package MeadHead.Poc.controler.view;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import MeadHead.Poc.controller.view.LoginViewController;

@WebMvcTest(LoginViewController.class)
// On désactive les filtres pour éviter les 401 sur les pages de login/dashboard
@AutoConfigureMockMvc(addFilters = false)
class LoginViewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("GET /login - Doit retourner la vue login")
    void testLoginPage() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"));
    }

    @Test
    @DisplayName("GET /dashboard - Doit retourner la vue dashboard")
    void testDashboardPage() throws Exception {
        mockMvc.perform(get("/dashboard"))
                .andExpect(status().isOk())
                .andExpect(view().name("dashboard"));
    }

    @Test
    @DisplayName("GET /search-hospital - Doit retourner la vue search-hospital")
    void testSearchHospitalPage() throws Exception {
        mockMvc.perform(get("/search-hospital"))
                .andExpect(status().isOk())
                .andExpect(view().name("search-hospital"));
    }
}
