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
    def sonarCredsId = "sonarqube-admin" 
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
        
        def sonarInst = new SonarInstallation(
            sonarServerName,    // Nom utilisé dans le Jenkinsfile
            sonarServerUrl,     // URL interne Docker
            sonarCredsId,       // Utilisation de l'ID "sonarqube-admin"
            null,               // Token (vide car login/pass utilisés)
            null,               // Webhook Secret
            null,               // Extra Properties
            null,               // Triggers
            null,               // Mojo Version
            null                // Additional Analysis Properties
        )
        
        sonarGlobalConfig.setInstallations([sonarInst] as SonarInstallation[])
        sonarGlobalConfig.save()
        instance.save()
        
        println "--- [INIT] Serveur SonarQube '${sonarServerName}' activé avec succès ---"

    } catch (Exception e) {
        println "--- [ERREUR] Configuration Serveur Sonar : ${e.message} ---"
    }

    // ========================================================================
    // PARTIE 3 : CONFIGURATION DU JOB (Pipeline)
    // ========================================================================
    def jobName = "MedHead_Pipeline"
    def xmlPath = "/var/jenkins_home/init.groovy.d/job_config.xml"
    def xmlFile = new File(xmlPath)

    try {
        if (xmlFile.exists()) {
            println "--- [INIT] Configuration du Job : ${jobName} ---"
            def jobXml = xmlFile.text
            def existingJob = instance.getItem(jobName)
            
            if (existingJob != null) {
                def xmlStream = new ByteArrayInputStream(jobXml.getBytes("UTF-8"))
                existingJob.updateByXml(new StreamSource(xmlStream))
                existingJob.save()
            } else {
                def xmlStream = new ByteArrayInputStream(jobXml.getBytes("UTF-8"))
                instance.createProjectFromXML(jobName, xmlStream)
            }

            instance.save()
            sleep(5000) 
            
            def job = instance.getItem(jobName)
            if (job != null && !job.isBuilding() && !job.isInQueue()) {
                job.scheduleBuild2(0)
                println "--- [SUCCÈS] Job '${jobName}' prêt et build lancé ---"
            }
        }
    } catch (Exception e) {
        println "--- [ERREUR] Initialisation du Job : ${e.message} ---"
    }
}