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
import java.nio.file.Files
import java.nio.file.Paths
import javax.xml.transform.stream.StreamSource

Thread.start {
    println "--- [INIT] Attente de 30s pour stabilisation ---"
    sleep(30000) 

    def instance = Jenkins.get()
    def store = instance.getExtensionList('com.cloudbees.plugins.credentials.SystemCredentialsProvider')[0].getStore()
    def envPathInContainer = "/tmp/.env"
    def envFileCredId = "medhead-env-file" // ID utilisé dans ton Jenkinsfile
    def sonarCredsId = "SONARQUBE_TOKEN"

    if (Files.exists(Paths.get(envPathInContainer))) {
        try {
            // ========================================================================
            // PARTIE 1 : CRÉATION AUTOMATIQUE DU "SECRET FILE" (.env)
            // ========================================================================
            println "--- [INIT] Création du Credential Fichier '${envFileCredId}' ---"
            def fileContent = Files.readAllBytes(Paths.get(envPathInContainer))
            def secretFile = new FileCredentialsImpl(
                CredentialsScope.GLOBAL, envFileCredId, "Fichier .env auto-importé", "env", SecretBytes.fromBytes(fileContent)
            )

            def existingFile = store.getCredentials(Domain.global()).find { it.id == envFileCredId }
            if (existingFile) { store.updateCredentials(Domain.global(), existingFile, secretFile) }
            else { store.addCredentials(Domain.global(), secretFile) }

            // ========================================================================
            // PARTIE 2 : EXTRACTION DU TOKEN ET CRÉATION DU "SECRET TEXT"
            // ========================================================================
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

                // Configuration du serveur
                def sonarConfig = instance.getDescriptorByType(SonarGlobalConfiguration.class)
                def sonarInst = new SonarInstallation("sonarqube-poc", "http://sonarqube-poc:9000", sonarCredsId, null, null, null, null, null, null)
                sonarConfig.setInstallations([sonarInst] as SonarInstallation[])
                sonarConfig.save()
            }
            
            instance.save()
            println "--- [SUCCÈS] Identifiants configurés à partir du .env ---"

        } catch (Exception e) {
            println "--- [ERREUR] Initialisation Credentials : ${e.message} ---"
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