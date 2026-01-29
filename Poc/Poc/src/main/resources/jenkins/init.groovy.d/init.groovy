import jenkins.model.*
import hudson.model.*
import hudson.plugins.sonar.*
import hudson.plugins.sonar.model.*
import com.cloudbees.plugins.credentials.*
import com.cloudbees.plugins.credentials.domains.*
import com.cloudbees.plugins.credentials.impl.*
import hudson.util.Secret
import java.io.ByteArrayInputStream
import javax.xml.transform.stream.StreamSource

// On lance l'initialisation dans un thread séparé pour ne pas bloquer le démarrage de Jenkins
Thread.start {
    println "--- [INIT] Attente de 30s pour stabilisation de Jenkins et des plugins ---"
    sleep(30000) 

    def instance = Jenkins.get()
    
    // ========================================================================
    // PARTIE 1 : CONFIGURATION DES IDENTIFIANTS (Credentials)
    // ========================================================================
    def sonarCredsId = "sonarqube-admin" // Identifiant utilisé par le plugin Sonar
    try {
        println "--- [INIT] Configuration des credentials 'sonarqube-admin' ---"
        
        def credentialsStore = instance.getExtensionList('com.cloudbees.plugins.credentials.SystemCredentialsProvider')[0].getStore()
        def domain = Domain.global()
        
        def sonarCredential = new UsernamePasswordCredentialsImpl(
            CredentialsScope.GLOBAL,
            sonarCredsId,
            "Authentification automatique admin/admin pour SonarQube",
            "admin",
            "admin"
        )

        def existingCreds = credentialsStore.getCredentials(domain).find { it.id == sonarCredsId }
        if (existingCreds) {
            credentialsStore.updateCredentials(domain, existingCreds, sonarCredential)
            println "--- [INIT] Credentials existants mis à jour ---"
        } else {
            credentialsStore.addCredentials(domain, sonarCredential)
            println "--- [INIT] Nouveaux credentials créés ---"
        }

    } catch (Exception e) {
        println "--- [ERREUR] Partie Credentials : ${e.message} ---"
    }

    // ========================================================================
    // PARTIE 2 : ENREGISTREMENT DU SERVEUR SONARQUBE
    // ========================================================================
    try {
        println "--- [INIT] Enregistrement du serveur SonarQube 'sonarqube-poc' ---"
        
        def sonarServerName = "sonarqube-poc"
        def sonarServerUrl = "http://sonarqube-poc:9000"
        
        def sonarGlobalConfig = instance.getDescriptorByType(SonarGlobalConfiguration.class)
        
        // Création de l'installation liée aux credentials créés en Partie 1
        def sonarInst = new SonarInstallation(
            sonarServerName,    // Nom (doit correspondre à withSonarQubeEnv dans le Jenkinsfile)
            sonarServerUrl,     // URL interne Docker
            sonarCredsId,       // Utilisation de l'ID "sonarqube-admin"
            null,               // Token (null car on utilise login/pass)
            null,               // Webhook Secret
            null,               // Extra Properties
            null,               // Triggers
            null,               // Mojo Version
            null                // Additional Analysis Properties
        )
        
        // Forçage de la configuration dans le plugin
        sonarGlobalConfig.setInstallations([sonarInst] as SonarInstallation[])
        sonarGlobalConfig.save()
        instance.save()
        
        println "--- [INIT] Serveur SonarQube '${sonarServerName