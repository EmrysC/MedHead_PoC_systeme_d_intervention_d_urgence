-- Adminer 5.4.1 MariaDB 12.1.2-MariaDB-ubu2404 dump

SET NAMES utf8;
SET time_zone = '+00:00';
SET foreign_key_checks = 0;

SET NAMES utf8mb4;

DROP TABLE IF EXISTS `groupe_specialite`;
CREATE TABLE `groupe_specialite` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `nom` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;


DROP TABLE IF EXISTS `hopital`;
CREATE TABLE `hopital` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `nom` varchar(255) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;


DROP TABLE IF EXISTS `reservation`;
CREATE TABLE `reservation` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `date_reservation` datetime(6) DEFAULT NULL,
  `unite_soins_id` bigint(20) NOT NULL,
  `user_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKrea93581tgkq61mdl13hehami` (`user_id`),
  KEY `FK1d20cock7ir4fjqhwbnt9bnqh` (`unite_soins_id`),
  CONSTRAINT `FK1d20cock7ir4fjqhwbnt9bnqh` FOREIGN KEY (`unite_soins_id`) REFERENCES `unite_soins` (`id`),
  CONSTRAINT `FKrea93581tgkq61mdl13hehami` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;


DROP TABLE IF EXISTS `specialisation`;
CREATE TABLE `specialisation` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `nom` varchar(150) NOT NULL,
  `groupe_specialite_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKc3l3waaxc6k62cd96msjkj8w6` (`nom`),
  KEY `FK1omgugnpdbd0gsgxq1l7icot3` (`groupe_specialite_id`),
  CONSTRAINT `FK1omgugnpdbd0gsgxq1l7icot3` FOREIGN KEY (`groupe_specialite_id`) REFERENCES `groupe_specialite` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;


DROP TABLE IF EXISTS `unite_soins`;
CREATE TABLE `unite_soins` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `adresse` varchar(512) DEFAULT NULL,
  `latitude` decimal(10,7) NOT NULL,
  `lits_disponibles` int(11) NOT NULL,
  `longitude` decimal(10,7) NOT NULL,
  `version` bigint(20) DEFAULT NULL,
  `hopital_id` bigint(20) NOT NULL,
  `specialisation_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKpi7hw3cbnh6kgu7f7pk2qdmfu` (`hopital_id`,`specialisation_id`,`latitude`,`longitude`),
  KEY `FKke5buyw80nuwwib0xvtykob4r` (`specialisation_id`),
  CONSTRAINT `FK9hnwdvkyj8l3naxxm137x0smc` FOREIGN KEY (`hopital_id`) REFERENCES `hopital` (`id`),
  CONSTRAINT `FKke5buyw80nuwwib0xvtykob4r` FOREIGN KEY (`specialisation_id`) REFERENCES `specialisation` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;


DROP TABLE IF EXISTS `users`;
CREATE TABLE `users` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `active` bit(1) NOT NULL,
  `email` varchar(255) NOT NULL,
  `nom` varchar(255) NOT NULL,
  `password` varchar(255) NOT NULL,
  `prenom` varchar(255) NOT NULL,
  `role` enum('ROLE_ADMIN','ROLE_HOPITAL','ROLE_USER') NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK6dotkott2kjsp8vw4d0m25fb7` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;


-- 2026-01-20 15:50:24 UTC