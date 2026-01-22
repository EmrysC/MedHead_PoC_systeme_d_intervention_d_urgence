# Utilisation de l'image officielle Puppeteer
FROM ghcr.io/puppeteer/puppeteer:23.6.0

# On repasse en utilisateur root pour l'installation
USER root

# Définir le répertoire de travail
WORKDIR /app

# On force l'initialisation et l'installation de Puppeteer dans /app
RUN npm init -y && npm install puppeteer

# On copie tous les script
COPY *.js .

# On s'assure que les permissions sont bonnes
RUN chmod -R 777 /app

# Commande de lancement
CMD ["node", "E2E_connexion_clicSpecialite_Selection_SaisieAdresse_reservation.js"]

