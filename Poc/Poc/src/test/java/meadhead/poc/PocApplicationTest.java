package meadhead.poc;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
// Force les propriétés H2 directement pour écraser toute config MariaDB
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MODE=MySQL",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class PocApplicationTest {

    @Test
    void contextLoads() {
        // Vérifie que le contexte se charge avec le profil test et H2
    }

    @Test
    void main() {
        // On force le type d'application à 'none' pour éviter de lancer Tomcat,
        // tout en exécutant le code de la méthode main pour la couverture
        PocApplication.main(new String[]{
            "--spring.main.web-application-type=none",
            "--spring.profiles.active=test"
        });
    }
}
