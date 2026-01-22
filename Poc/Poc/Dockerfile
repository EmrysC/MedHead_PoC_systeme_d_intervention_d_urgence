# utilise une image Java légère
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
# copie le JAR généré par Maven
COPY target/*.jar app.jar
#  expose le port de l'API
EXPOSE 8080
#  lance l'application avec le profil dev
ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=dev"]