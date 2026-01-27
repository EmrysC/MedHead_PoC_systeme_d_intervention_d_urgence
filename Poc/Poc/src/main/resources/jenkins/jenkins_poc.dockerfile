FROM jenkins/jenkins:lts

USER root

# 1. Installation des outils de base
RUN apt-get update && apt-get install -y \
    lsb-release \
    python3 \
    php-cli \
    curl \
    unzip \
    wget \
    gnupg2 \
    git \
    maven \
    && rm -rf /var/lib/apt/lists/*

# 2. Installation de Docker CLI
RUN curl -fsSL https://download.docker.com/linux/static/stable/x86_64/docker-24.0.7.tgz | tar zxvf - --strip-components=1 -C /usr/local/bin docker/docker

# 3. Installation de Docker Compose 
RUN curl -L "https://github.com/docker/compose/releases/download/v2.24.5/docker-compose-linux-x86_64" -o /usr/local/bin/docker-compose && \
    chmod +x /usr/local/bin/docker-compose

# 4. Installation de Node.js & Newman
RUN curl -fsSL https://deb.nodesource.com/setup_18.x | bash - && \
    apt-get install -y nodejs && \
    npm install -g newman newman-reporter-htmlextra

# 5. Installation du Sonar-Scanner CLI
ENV SONAR_SCANNER_VERSION=5.0.1.3006
RUN curl -o /tmp/sonar-scanner.zip -L https://binaries.sonarsource.com/Distribution/sonar-scanner-cli/sonar-scanner-cli-${SONAR_SCANNER_VERSION}-linux.zip && \
    unzip /tmp/sonar-scanner.zip -d /opt && \
    ln -s /opt/sonar-scanner-${SONAR_SCANNER_VERSION}-linux/bin/sonar-scanner /usr/local/bin/sonar-scanner && \
    rm /tmp/sonar-scanner.zip

# 6. Installation des plugins Jenkins 
# Correction étape 6 et 7 : Une seule commande propre
RUN jenkins-plugin-cli --plugins "workflow-aggregator docker-workflow git sonar blueocean blueocean-pipeline-editor credentials-binding"

# 7. Retour à l'utilisateur Jenkins
USER jenkins