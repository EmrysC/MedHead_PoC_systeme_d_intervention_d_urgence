-- TEST ---

-- On met tous les lits disponibles à 0 pour la spécilitée 'LEARNING DISABILITY' ID 28
-- testé dans postman:  trajet -> 404 Pas de lit disponible
UPDATE unite_soins 
SET lits_disponibles = 0 
WHERE specialisation_id = (
    SELECT id 
    FROM specialisation 
    WHERE nom = 'LEARNING DISABILITY'
);


-- On met les lits disponibles à 0 pour l'unité de soins d'id 519
-- testé dans postman: reservation -> 409 pas de lit disponible
UPDATE unite_soins 
SET lits_disponibles = 0 
WHERE id = 519;

-- On supprime l'utilisateur postman pour pouvoir tester la création d'un utilisateur
-- et des reservation pour la contrainte des clées étrangères
DELETE FROM reservation 
WHERE user_id = (SELECT id FROM users WHERE email = 'postmanTestUtilisateur.non@existant.com');
DELETE FROM users 
WHERE email = 'postmanTestUtilisateur.non@existant.com';