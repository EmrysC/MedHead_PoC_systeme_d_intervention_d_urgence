import jenkins.model.*
import hudson.model.*
import hudson.plugins.sonar.*
import hudson.plugins.sonar.model.*
import com.cloudbees.plugins.credentials.*
import com.cloudbees.plugins.credentials.domains.*
import org.jenkinsci.plugins.plaincredentials.*
import org.jenkinsci.plugins.plaincredentials.impl.*
import hudson.util.Secret
import java.io.ByteArrayInputStream
import javax.xml.transform.stream.StreamSource

// Initialisation asynchrone pour laisser le temps aux services de démarrer
Thread.start {
    println "--- [INIT] Attente de 30s pour stabilisation de Jenkins ---"
    sleep(30000) 

    def instance = Jenkins.get()
    def envCredId = ".env"          // L'ID de ton credential fichier existant
    def sonarCredsId = "SONARCUBE_TOKEN" 
    def extractedToken = null

    // ========================================================================
    // PARTIE 1 : EXTRACTION AUTOMATIQUE DEPUIS L'IDENTIFIANT ".env"
    // ========================================================================
    try {
        def credentialsStore = instance.getExtensionList('com.cloudbees.plugins.credentials.SystemCredentialsProvider')[0].getStore()
        def credentials = credentialsStore.getCredentials(Domain.global())
        
        // Recherche de l'identifiant ".env"
        def envCred = credentials.find { it.id == envCredId }

        if (envCred instanceof FileCredentials) {
            println "--- [INIT] Lecture du contenu de l'identifiant '.env' ---"
            def content = envCred.getContent().text
            
            // Extraction de la valeur après SONARCUBE_TOKEN=
            content.eachLine { line ->
                if (line.contains("SONARCUBE_TOKEN=")) {
                    extractedToken = line.split("=")[1].trim()
                }
            }
        } else {
            println "--- [ERREUR] Identifiant '.env' introuvable dans Jenkins ---"
        }
    } catch (Exception e) {
        println "--- [ERREUR] Extraction : ${e.message} ---"
    }

    // ========================================================================
    // PARTIE 2 : CONFIGURATION DU SECRET TEXT ET DU SERVEUR SONAR
    // ========================================================================
    if (extractedToken) {
        try {
            println "--- [INIT] Token trouvé. Configuration du serveur SonarQube ---"
            def credentialsStore = instance.getExtensionList('com.cloudbees.plugins.credentials.SystemCredentialsProvider')[0].getStore()
            
            // Création/Mise à jour du Secret Text "SONARCUBE_TOKEN"
            def sonarTokenCred = new StringCredentialsImpl(
                CredentialsScope.GLOBAL, 
                sonarCredsId, 
                "Token extrait automatiquement du fichier .env", 
                Secret.fromString(extractedToken)
            )

            def existing = credentialsStore.getCredentials(Domain.global()).find { it.id == sonarCredsId }
            if (existing) { 
                credentialsStore.updateCredentials(Domain.global(), existing, sonarTokenCred) 
            } else { 
                credentialsStore.addCredentials(Domain.global(), sonarTokenCred) 
            }

            // Enregistrement de l'installation SonarQube
            def sonarConfig = instance.getDescriptorByType(SonarGlobalConfiguration.class)
            def sonarInst = new SonarInstallation(
                "sonarqube-poc", 
                "http://sonarqube-poc:9000", 
                sonarCredsId, 
                null, null, null, null, null, null
            )
            
            sonarConfig.setInstallations([sonarInst] as SonarInstallation[])
            sonarConfig.save()
            instance.save()
            println "--- [INIT] Serveur 'sonarqube-poc' configuré avec succès ---"

        } catch (Exception e) {
            println "--- [ERREUR] Configuration Sonar : ${e.message} ---"
        }
    }

    // ========================================================================
    // PARTIE 3 : CONFIGURATION ET LANCEMENT DU JOB
    // ========================================================================
    def jobName = "MedHead_Pipeline"
    def xmlPath = "/var/jenkins_home/init.groovy.d/job_config.xml"
    
    try {
        def xmlFile = new File(xmlPath)
        if (xmlFile.exists()) {
            println "--- [INIT] Synchronisation du Job : ${jobName} ---"
            def jobXml = xmlFile.text
            def existingJob = instance.getItem(jobName)
            
            if (existingJob != null) {
                existingJob.updateByXml(new StreamSource(new ByteArrayInputStream(jobXml.getBytes("UTF-8"))))
                existingJob.save()
            } else {
                instance.createProjectFromXML(jobName, new ByteArrayInputStream(jobXml.getBytes("UTF-8")))
            }

            instance.save()
            sleep(5000) 
            
            def job = instance.getItem(jobName)
            if (job != null && !job.isBuilding() && !job.isInQueue()) {
                job.scheduleBuild2(0)
                println "--- [SUCCÈS] Job '${jobName}' lancé avec succès ---"
            }
        }
    } catch (Exception e) {
        println "--- [ERREUR] Job Init : ${e.message} ---"
    }
}