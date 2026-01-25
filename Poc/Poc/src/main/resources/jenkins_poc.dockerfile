FROM jenkins/jenkins:lts
USER root
# Installation de Docker CLI et du plugin Compose
RUN apt-get update && apt-get install -y lsb-release
RUN curl -fsSLo /usr/share/keyrings/docker-archive-keyring.asc \
  https://download.docker.com/linux/debian/gpg
RUN echo "deb [arch=$(dpkg --print-architecture) \
  signed-by=/usr/share/keyrings/docker-archive-keyring.asc] \
  https://download.docker.com/linux/debian $(lsb_release -cs) stable" > /etc/apt/sources.list.d/docker.list
RUN apt-get update && apt-get install -y docker-ce-cli docker-compose-plugin


# Installation automatique des plugins
# blueocean : l'interface "jolie"
# sonar : le plugin SonarQube Scanner
# docker-workflow : pour utiliser Docker dans tes pipelines
RUN jenkins-plugin-cli --plugins \
  blueocean \
  sonar \
  docker-workflow

USER jenkins