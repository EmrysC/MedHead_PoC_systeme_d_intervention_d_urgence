package MeadHead.Poc;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
// MODIFICATION : On utilise le profil 'test' pour utiliser H2 au lieu de MariaDB
@ActiveProfiles("test")
// MODIFICATION : On autorise le remplacement par la base de données de test (H2)
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
