import jenkins.model.*
import hudson.model.*
import java.io.File
import java.io.ByteArrayInputStream
import javax.xml.transform.stream.StreamSource
// --- NOUVEAUX IMPORTS POUR SONARQUBE ---
import hudson.plugins.sonar.*
import hudson.plugins.sonar.model.*
import com.cloudbees.plugins.credentials.*
import com.cloudbees.plugins.credentials.domains.*
import org.jenkinsci.plugins.plaincredentials.impl.*
import hudson.util.Secret

Thread.start {
    // Augmenté à 30s pour laisser le temps aux plugins Docker/Git/Sonar de charger
    println "--- [INIT] Attente de 30s pour stabilisation Jenkins ---"
    sleep(30000) 

    def instance = Jenkins.getInstance()
    
    // ========================================================================
    // PARTIE 1 : CONFIGURATION SONARQUBE 
    // ========================================================================
    try {
        println "--- [INIT] Début configuration SonarQube ---"
        
        // Configuration
        def sonarServerName = "sonarqube-poc"  // Le nom utilisé dans le Pipeline
        def sonarServerUrl = "http://sonarqube-poc:9000"
        def sonarTokenId = "sonar-token-id"
        def sonarAuthToken = System.getenv("SONAR_AUTH_TOKEN") // Variable d'env Docker

        if (sonarAuthToken) {
            // A. Création du Credential (Token)
            def credentialsStore = instance.getExtensionList('com.cloudbees.plugins.credentials.SystemCredentialsProvider')[0].getStore()
            def domain = Domain.global()
            def secretBytes = Secret.fromString(sonarAuthToken)
            def sonarCredential = new StringCredentialsImpl(
                CredentialsScope.GLOBAL,
                sonarTokenId,
                "Token Admin SonarQube (Auto)",
                secretBytes
            )

            // On met à jour ou on crée
            def existingCreds = credentialsStore.getCredentials(domain).find { it.id == sonarTokenId }
            if (existingCreds) {
                credentialsStore.updateCredentials(domain, existingCreds, sonarCredential)
            } else {
                credentialsStore.addCredentials(domain, sonarCredential)
            }
            println "--- [INIT] Credentials SonarQube configurés ---"

            // B. Configuration du Serveur dans Jenkins
            def sonarGlobalConfig = instance.getDescriptorByType(SonarGlobalConfiguration.class)
            def sonarInst = new SonarInstallation(
                sonarServerName,
                sonarServerUrl,
                sonarTokenId,
                null, null, null, null, null
            )
            sonarGlobalConfig.setInstallations(sonarInst)
            sonarGlobalConfig.save()
            println "--- [INIT] Serveur SonarQube '${sonarServerName}' activé ---"
            
        } else {
            println "--- [WARN] Pas de SONAR_AUTH_TOKEN trouvé. Configuration Sonar ignorée. ---"
        }

    } catch (Exception e) {
        println "--- [ERREUR] Problème config SonarQube : ${e.message} ---"
        e.printStackTrace()
    }

    // ========================================================================
    // PARTIE 2 : CONFIGURATION DU JOB 
    // ========================================================================
    def jobName = "MedHead_Pipeline"
    def xmlPath = "/var/jenkins_home/init.groovy.d/job_config.xml"
    def xmlFile = new File(xmlPath)

    try {
        if (xmlFile.exists()) {
            println "--- [INIT] Fichier config trouvé, configuration du Job: ${jobName} ---"
            def jobXml = xmlFile.text
            def existingJob = instance.getItem(jobName)
            
            if (existingJob != null) {
                println "--- [INIT] Mise à jour du job existant ---"
                def xmlStream = new ByteArrayInputStream(jobXml.getBytes("UTF-8"))
                existingJob.updateByXml(new StreamSource(xmlStream))
                existingJob.save()
            } else {
                println "--- [INIT] Création du nouveau job ---"
                def xmlStream = new ByteArrayInputStream(jobXml.getBytes("UTF-8"))
                instance.createProjectFromXML(jobName, xmlStream)
            }

            // Sauvegarde finale et Reload
            instance.save()
            instance.reload() 
            
            sleep(5000) 
            
            def job = instance.getItem(jobName)
            if (job != null) {
                if (!job.isBuilding() && !job.isInQueue()) {
                    job.scheduleBuild2(0)
                    println "--- [SUCCES] Job ${jobName} configuré et build lancé ---"
                } else {
                    println "--- [INFO] Job déjà en cours d'exécution ---"
                }
            }
        } else {
            println "--- [ERREUR] Fichier XML ${xmlPath} introuvable ---"
        }
    } catch (Exception e) {
        println "--- [ERREUR] Échec de l'init du job : ${e.message} ---"
        e.printStackTrace()
    }
}