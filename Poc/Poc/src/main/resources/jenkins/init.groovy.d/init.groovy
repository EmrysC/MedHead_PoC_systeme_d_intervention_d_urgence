import jenkins.model.*
import hudson.model.*
import hudson.plugins.sonar.*
import hudson.plugins.sonar.model.*
import com.cloudbees.plugins.credentials.*
import com.cloudbees.plugins.credentials.domains.*
import com.cloudbees.plugins.credentials.impl.* 
import org.jenkinsci.plugins.plaincredentials.*
import org.jenkinsci.plugins.plaincredentials.impl.*
import hudson.util.Secret
import java.io.ByteArrayInputStream
import java.nio.file.Files
import java.nio.file.Paths
import javax.xml.transform.stream.StreamSource

Thread.start {
    println "--- [INIT] Attente de 30s pour stabilisation ---"
    sleep(30000) 

    def instance = Jenkins.get()
    def store = instance.getExtensionList('com.cloudbees.plugins.credentials.SystemCredentialsProvider')[0].getStore()
    def envPathInContainer = "/tmp/.env"
    def envFileCredId = "medhead-env-file"
    def sonarCredsId = "SONARQUBE_TOKEN"

    if (Files.exists(Paths.get(envPathInContainer))) {
        try {
            // --- PARTIE 1 : SECRET FILE ---
            println "--- [INIT] Création du Credential Fichier '${envFileCredId}' ---"
            def fileContent = Files.readAllBytes(Paths.get(envPathInContainer))
            
            //  Utilisation de SecretBytes et constructeur propre
            def secretFile = new FileCredentialsImpl(
                CredentialsScope.GLOBAL, 
                envFileCredId, 
                "Fichier .env auto-importé", 
                "env", 
                SecretBytes.fromBytes(fileContent)
            )

            def existingFile = store.getCredentials(Domain.global()).find { it.id == envFileCredId }
            if (existingFile) { store.updateCredentials(Domain.global(), existingFile, secretFile) }
            else { store.addCredentials(Domain.global(), secretFile) }

            // --- PARTIE 2 : SONAR TOKEN ---
            def extractedToken = null
            new String(fileContent).eachLine { line ->
                if (line.contains("SONARQUBE_TOKEN=")) {
                    extractedToken = line.split("=")[1].trim()
                }
            }

            if (extractedToken) {
                println "--- [INIT] Création du Secret Text '${sonarCredsId}' ---"
                def sonarTokenCred = new StringCredentialsImpl(
                    CredentialsScope.GLOBAL, sonarCredsId, "Token Sonar auto-extrait", Secret.fromString(extractedToken)
                )

                def existingText = store.getCredentials(Domain.global()).find { it.id == sonarCredsId }
                if (existingText) { store.updateCredentials(Domain.global(), existingText, sonarTokenCred) }
                else { store.addCredentials(Domain.global(), sonarTokenCred) }

                def sonarConfig = instance.getDescriptorByType(SonarGlobalConfiguration.class)

                // On utilise un constructeur stable à 8 paramètres pour la version LTS
                def sonarInst = new SonarInstallation(
                    "sonarqube-poc",              // Name
                    "http://sonarqube-poc:9000",  // Server URL
                    sonarCredsId,                 // Credentials ID
                    null,                         // webhookSecretId (null est OK)
                    new TriggersConfig(),         // triggers
                    "",                           // additionalProperties (utiliser vide au lieu de null)
                    "",                           // additionalAnalysisProperties (utiliser vide au lieu de null)
                    ""                            // serverVersion (utiliser vide au lieu de null)
                )

                // Crucial : Jenkins attend un tableau (Array), même pour un seul serveur
                def sonarInstallations = [sonarInst] as SonarInstallation[]
                sonarConfig.setInstallations(sonarInstallations)
                sonarConfig.save()
            }
            
            instance.save()
            println "--- [SUCCÈS] Configuration terminée ---"

        } catch (Exception e) {
            println "--- [ERREUR] : ${e.toString()} ---"
            e.printStackTrace()
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