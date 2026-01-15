package MeadHead.Poc.repository;

import MeadHead.Poc.entites.UniteSoins;
import MeadHead.Poc.entites.Specialisation;
import MeadHead.Poc.entites.GroupeSpecialite;
import MeadHead.Poc.entites.Hopital;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("preprod")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UniteSoinsRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UniteSoinsRepository uniteSoinsRepository;

    private Specialisation cardio;

    @BeforeEach
    void setUp() {
        // Création de l'Hôpital 
        Hopital hopital = Hopital.builder()
                .nom("Hôpital Central")
                .build();
        entityManager.persist(hopital);

        // Création du Groupe
        GroupeSpecialite groupeMCO = GroupeSpecialite.builder()
                .nom("Médecine Chirurgie Obstétrique")
                .build();
        entityManager.persist(groupeMCO);

        // Création de la spécialisation
        cardio = Specialisation.builder()
                .nom("Cardiologie")
                .groupeSpecialite(groupeMCO)
                .build();
        entityManager.persist(cardio);

        // Création des Unités de Soins
        UniteSoins u1 = UniteSoins.builder()
                .adresse("Unité Coronarienne - Bâtiment A")
                .specialisation(cardio)
                .hopital(hopital)
                .litsDisponibles(12)
                .latitude(new BigDecimal("48.8566000"))
                .longitude(new BigDecimal("2.3522000"))
                .build();
        entityManager.persist(u1);

        UniteSoins u2 = UniteSoins.builder()
                .adresse("Réadaptation Cardiaque - Bâtiment B")
                .specialisation(cardio)
                .hopital(hopital)
                .litsDisponibles(0)
                .latitude(new BigDecimal("45.7640000"))
                .longitude(new BigDecimal("4.8357000"))
                .build();
        entityManager.persist(u2);

        entityManager.flush();
    }

    @Test
    @DisplayName("Trouver par ID de spécialisation avec lits disponibles")
    void findBySpecialisationId_LitsDispo() {
        List<UniteSoins> result = uniteSoinsRepository
                .findBySpecialisationIdAndLitsDisponiblesGreaterThan(cardio.getId(), 0);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getAdresse()).contains("Coronarienne");
    }

    @Test
    @DisplayName("Trouver par Nom de spécialisation avec lits disponibles")
    void findBySpecialisationNom_LitsDispo() {
        List<UniteSoins> result = uniteSoinsRepository
                .findBySpecialisationNomAndLitsDisponiblesGreaterThan("Cardiologie", 5);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getLitsDisponibles()).isEqualTo(12);
    }

    @Test
    @DisplayName("Retourne liste vide si aucun lit n'est disponible")
    void findBySpecialisation_NoLitsAvailable() {
        List<UniteSoins> result = uniteSoinsRepository
                .findBySpecialisationNomAndLitsDisponiblesGreaterThan("Cardiologie", 20);

        assertThat(result).isEmpty();
    }
}
