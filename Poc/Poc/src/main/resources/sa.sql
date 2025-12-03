


-- CRÉATION DE LA TABLE RÉFÉRENCÉE (GroupeSpecialite)
DROP TABLE IF EXISTS GroupeSpecialite;
CREATE TABLE GroupeSpecialite (
    id SERIAL PRIMARY KEY, 
    nom VARCHAR(150) NOT NULL UNIQUE

) ENGINE=InnoDB;



-- CRÉATION DE LA TABLE RÉFÉRENÇANTE (Specialisation)
DROP TABLE IF EXISTS Specialisation;
CREATE TABLE Specialisation (
    id SERIAL PRIMARY KEY,
    nom VARCHAR(150) NOT NULL UNIQUE,
    
    -- Utilisation de BIGINT UNSIGNED pour correspondre à GroupeSpecialite.id
    groupe_specialite_id BIGINT UNSIGNED NOT NULL, 
    CONSTRAINT fk_groupe_specialite
        FOREIGN KEY (groupe_specialite_id) 
        REFERENCES GroupeSpecialite(id),

) ENGINE=InnoDB;



DROP TABLE IF EXISTS Hopital ;
CREATE TABLE Hopital (
    -- Clé primaire
    id SERIAL PRIMARY KEY,
    
    -- Nom de l'hôpital (ex: 'Hôpital Fred Brooks')
    nom VARCHAR(255) NOT NULL

)ENGINE=InnoDB;




DROP TABLE IF EXISTS UniteSoins;
CREATE TABLE UniteSoins (
    -- Clé primaire pour identifier l'unité spécifique
    id SERIAL PRIMARY KEY,
    
    -- Clé étrangère vers la table Hopital
    hopital_id BIGINT UNSIGNED NOT NULL,
    CONSTRAINT fk_hopital_id
        FOREIGN KEY (hopital_id) 
        REFERENCES Hopital(id),
    
    -- Clé étrangère vers la table Specialisation
    specialisation_id BIGINT UNSIGNED NOT NULL,
    CONSTRAINT fk_specialisation_id
        FOREIGN KEY (specialisation_id) 
        REFERENCES Specialisation(id),
    
    -- Colonne pour l'adresse 
    adresse VARCHAR(512) , -- Champ pour l'adresse complète (ex: 1 Rue Victore Hugo, 38000 Paris)
    
    -- Nombre de lits disponibles dans cette unité/spécialisation
    lits_disponibles INTEGER NOT NULL 
        CHECK (lits_disponibles >= 0),
    
    -- Coordonnée de latitude de cette unité
    latitude NUMERIC(10, 7) NOT NULL,
    
    -- Coordonnée de longitude de cette unité
    longitude NUMERIC(10, 7) NOT NULL,

    -- Contrainte d'unicité on ne veut pas de doublons
    UNIQUE (hopital_id, specialisation_id, latitude, longitude)

)ENGINE=InnoDB;