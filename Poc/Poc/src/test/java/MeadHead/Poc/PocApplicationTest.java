package MeadHead.Poc;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("pre_prod")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class PocApplicationTest {

    @Test
    void contextLoads() {
        // Vérifie que le contexte se charge avec le profil pre_prod et MariaDB
    }

    @Test
    void main() {
        // On force le type d'application à 'none' pour éviter de lancer Tomcat,
        // tout en exécutant le code de la méthode main pour la couverture
        PocApplication.main(new String[]{
            "--spring.main.web-application-type=none"
        });
    }
}
