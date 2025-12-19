
-- Désactiver temporairement les vérifications des clés étrangères
SET FOREIGN_KEY_CHECKS = 0;

-- Vider les tables 

TRUNCATE TABLE users;
TRUNCATE TABLE specialisation;
TRUNCATE TABLE groupe_specialite;
TRUNCATE TABLE hopital;
TRUNCATE TABLE unite_soins;
TRUNCATE TABLE reservation;



INSERT INTO users ( active, email, nom, password, prenom, role) VALUES 
(1,'utilisateur.possede@compte.com', 'NomUtilisateur', '$2a$10$p2CGjRAPVz/8LRWE1egCseRZObf9vVghiKhVnL9hBIovJuk1QWOwG', 'Prenom_Utilisateur', 'ROLE_USER'),
(1,'utilisateur.deja@existant.com', 'NomUtilisateur', '$2a$10$k3TfzxXK8k3ZiJgpJB/i5.5J8XMHyyevcuzZ3RT.U.pF0TPTlVDtS', 'Prenom_Utilisateur', 'ROLE_USER');


INSERT INTO groupe_specialite (id, nom) VALUES 
(1, 'Surgical Specialties'),
(2, 'Medical Specialties'),
(3, 'Mental Health'),
(4, 'Radiology & Pathology'),
(5, 'Obstetrics & Gynaecology');

-- Surgical Specialties (ID 1)
INSERT INTO specialisation (id, nom, groupe_specialite_id) VALUES
(1, 'GENERAL SURGERY', 1),
(2, 'UROLOGY', 1),
(3, 'TRAUMA & ORTHOPAEDICS', 1),
(4, 'ENT', 1),
(5, 'OPHTHALMOLOGY', 1),
(6, 'ORAL SURGERY', 1),
(7, 'NEUROSURGERY', 1),
(8, 'PLASTIC SURGERY', 1),
(9, 'CARDIOTHORACIC SURGERY', 1),
(10, 'PAEDIATRIC SURGERY', 1),
(11, 'ACCIDENT & EMERGENCY', 1);

-- Medical Specialties (ID 2)
INSERT INTO specialisation (id, nom, groupe_specialite_id) VALUES
(12, 'ANAESTHETICS', 2),
(13, 'GENERAL MEDICINE', 2),
(14, 'GASTROENTEROLOGY', 2),
(15, 'ENDOCRINOLOGY', 2),
(16, 'CLINICAL HAEMATOLOGY', 2),
(17, 'AUDIOLOGICAL MEDICINE', 2),
(18, 'CLINICAL GENETICS', 2),
(19, 'CARDIOLOGY', 2),
(20, 'DERMATOLOGY', 2),
(21, 'RESPIRATORY MEDICINE', 2),
(22, 'INFECTIOUS DISEASES', 2),
(23, 'NEPHROLOGY', 2),
(24, 'NEUROLOGY', 2),
(25, 'RHEUMATOLOGY', 2),
(26, 'PAEDIATRICS', 2),
(27, 'GERIATRIC MEDICINE', 2);

-- Mental Health (ID 3)
INSERT INTO specialisation (id, nom, groupe_specialite_id) VALUES
(28, 'LEARNING DISABILITY', 3),
(29, 'ADULT MENTAL ILLNESS', 3),
(30, 'CHILD & ADOLESCENT PSYCHIATRY', 3),
(31, 'FORENSIC PSYCHIATRY', 3),
(32, 'OLD AGE PSYCHIATRY', 3);

-- Radiology & Pathology (ID 4)
INSERT INTO specialisation (id, nom, groupe_specialite_id) VALUES
(33, 'CLINICAL ONCOLOGY', 4),
(34, 'RADIOLOGY', 4),
(35, 'HAEMATOLOGY', 4);

-- Obstetrics & Gynaecology (ID 5)
INSERT INTO specialisation (id, nom, groupe_specialite_id) VALUES
(36, 'OBSTETRICS', 5),
(37, 'GYNAECOLOGY', 5);


INSERT INTO hopital (id, nom) VALUES 
(1, 'Centre Hospitalier Universitaire de Lille'),
(2, 'Hôpital de la Pitié-Salpêtrière'),
(3, 'Hôpital Européen Georges-Pompidou'),
(4, 'Hôpital Purpan'),
(5, 'CHU de Bordeaux'),
(6, 'Hôpital Civil de Strasbourg'),
(7, 'Hôpital de la Timone'),
(8, 'CHU de Nantes'),
(9, 'Hôpital Édouard Herriot'),
(10, 'Hôpital Pellegrin');


INSERT INTO unite_soins (id, adresse, latitude, longitude, lits_disponibles, hopital_id, specialisation_id) VALUES
(1, '12 Rue de la Paix, Paris (Bâtiment 4)', 43.240528, 5.634805, 34, 3, 10),
(2, '22 Boulevard Haussmann, Marseille (Bâtiment 2)', 43.119622, 1.902859, 21, 10, 11),
(3, '33 Rue de la Liberté, Bordeaux (Bâtiment 5)', 44.796966, 2.968757, 6, 9, 27),
(4, '18 Rue Sainte-Catherine, Strasbourg (Bâtiment 4)', 44.242844, 1.842708, 16, 8, 3),
(5, '12 Rue de la Paix, Paris (Bâtiment 2)', 43.636875, 4.565347, 2, 2, 6),
(6, '8 Impasse des Lilas, Lyon (Bâtiment 4)', 47.811554, 1.938117, 2, 2, 16),
(7, '33 Rue de la Liberté, Bordeaux (Bâtiment 1)', 49.391526, 5.063939, 17, 7, 19),
(8, '45 Avenue de la République, Lille (Bâtiment 1)', 45.150724, 3.777739, 1, 1, 29),
(9, '5 Quai des Orfèvres, Nantes (Bâtiment 3)', 44.18018, 6.096924, 5, 10, 24),
(10, '22 Boulevard Haussmann, Marseille (Bâtiment 3)', 47.281022, 2.30339, 9, 9, 20),
(11, '14 Rue de Rivoli, Nice (Bâtiment 4)', 48.451249, 4.47266, 18, 1, 14),
(12, '12 Rue de la Paix, Paris (Bâtiment 5)', 49.896405, -0.489051, 31, 8, 25),
(13, '14 Rue de Rivoli, Nice (Bâtiment 2)', 48.714684, 3.549497, 14, 10, 18),
(14, '33 Rue de la Liberté, Bordeaux (Bâtiment 5)', 45.375032, 6.061962, 8, 2, 5),
(15, '10 Place du Capitole, Toulouse (Bâtiment 1)', 49.402355, -0.5901, 35, 8, 29),
(16, '7 Avenue Jean Jaurès, Montpellier (Bâtiment 4)', 46.043617, 3.694597, 40, 1, 12),
(17, '10 Place du Capitole, Toulouse (Bâtiment 4)', 43.10038, 3.582106, 0, 7, 19),
(18, '7 Avenue Jean Jaurès, Montpellier (Bâtiment 5)', 48.943116, 0.605708, 6, 5, 14),
(19, '45 Avenue de la République, Lille (Bâtiment 3)', 49.523108, 3.618623, 26, 6, 17),
(20, '12 Rue de la Paix, Paris (Bâtiment 5)', 49.193685, 1.375026, 33, 6, 32),
(21, '12 Rue de la Paix, Paris (Bâtiment 3)', 48.394023, 6.957861, 36, 6, 20),
(22, '45 Avenue de la République, Lille (Bâtiment 2)', 47.133618, 3.309661, 34, 2, 6),
(23, '5 Quai des Orfèvres, Nantes (Bâtiment 5)', 47.650939, 1.321254, 18, 2, 23),
(24, '10 Place du Capitole, Toulouse (Bâtiment 1)', 49.634087, 2.538431, 30, 3, 2),
(25, '8 Impasse des Lilas, Lyon (Bâtiment 3)', 49.400639, 5.120334, 1, 9, 22),
(26, '7 Avenue Jean Jaurès, Montpellier (Bâtiment 2)', 45.228378, 0.332319, 34, 8, 23),
(27, '18 Rue Sainte-Catherine, Strasbourg (Bâtiment 2)', 45.452606, 4.115514, 12, 4, 11),
(28, '12 Rue de la Paix, Paris (Bâtiment 2)', 48.306776, 2.530225, 35, 6, 8),
(29, '33 Rue de la Liberté, Bordeaux (Bâtiment 1)', 47.01603, 1.587245, 30, 10, 26),
(30, '8 Impasse des Lilas, Lyon (Bâtiment 3)', 44.727721, 6.238654, 4, 4, 5),
(31, '10 Place du Capitole, Toulouse (Bâtiment 2)', 47.190254, -0.918427, 3, 9, 7),
(32, '5 Quai des Orfèvres, Nantes (Bâtiment 2)', 49.45108, 6.900991, 37, 4, 22),
(33, '33 Rue de la Liberté, Bordeaux (Bâtiment 1)', 43.510992, 2.194243, 24, 2, 5),
(34, '5 Quai des Orfèvres, Nantes (Bâtiment 5)', 47.598076, 3.029496, 13, 9, 6),
(35, '18 Rue Sainte-Catherine, Strasbourg (Bâtiment 5)', 45.728231, 0.145232, 36, 4, 15),
(36, '14 Rue de Rivoli, Nice (Bâtiment 3)', 47.664004, -0.9309, 12, 9, 19),
(37, '8 Impasse des Lilas, Lyon (Bâtiment 4)', 49.447879, 3.976719, 14, 10, 28),
(38, '45 Avenue de la République, Lille (Bâtiment 4)', 47.658077, -0.778011, 39, 10, 19),
(39, '33 Rue de la Liberté, Bordeaux (Bâtiment 4)', 49.851937, 2.952052, 33, 9, 8),
(40, '45 Avenue de la République, Lille (Bâtiment 3)', 44.538092, -0.978653, 0, 2, 22),
(41, '18 Rue Sainte-Catherine, Strasbourg (Bâtiment 2)', 45.069969, 6.329402, 15, 8, 9),
(42, '14 Rue de Rivoli, Nice (Bâtiment 3)', 44.12934, 1.132896, 34, 9, 36),
(43, '45 Avenue de la République, Lille (Bâtiment 5)', 49.079604, 6.932219, 12, 9, 37),
(44, '8 Impasse des Lilas, Lyon (Bâtiment 3)', 49.878226, 6.165429, 6, 7, 12),
(45, '5 Quai des Orfèvres, Nantes (Bâtiment 4)', 44.531574, 2.137678, 22, 4, 8),
(46, '33 Rue de la Liberté, Bordeaux (Bâtiment 2)', 43.690974, 0.648075, 8, 10, 24),
(47, '18 Rue Sainte-Catherine, Strasbourg (Bâtiment 5)', 45.39985, 0.827647, 22, 3, 27),
(48, '22 Boulevard Haussmann, Marseille (Bâtiment 5)', 49.651539, 5.101216, 26, 9, 17),
(49, '18 Rue Sainte-Catherine, Strasbourg (Bâtiment 1)', 48.750885, 2.448104, 34, 4, 17),
(50, '10 Place du Capitole, Toulouse (Bâtiment 2)', 43.074271, 0.948819, 24, 9, 30);


-- Réactiver les vérifications
SET FOREIGN_KEY_CHECKS = 1;