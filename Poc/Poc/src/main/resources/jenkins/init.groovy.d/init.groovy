import jenkins.model.*
import hudson.model.*
import hudson.plugins.sonar.*
import hudson.plugins.sonar.model.*
import hudson.plugins.sonar.utils.*
import com.cloudbees.plugins.credentials.*
import com.cloudbees.plugins.credentials.domains.*
import com.cloudbees.plugins.credentials.impl.*
import org.jenkinsci.plugins.plaincredentials.impl.*
import hudson.util.Secret
import java.io.ByteArrayInputStream
import javax.xml.transform.stream.StreamSource

Thread.start {
    println "--- [INIT] Attente de 30s pour stabilisation Jenkins ---"
    sleep(30000) 

    def instance = Jenkins.getInstance()
    
    // ========================================================================
    // PARTIE 1 : CONFIGURATION DES CREDENTIALS (Admin/Admin)
    // ========================================================================
    try {
        println "--- [INIT] Création des identifiants... ---"
        
        def sonarCredsId = "sonar-admin-creds"
        def credentialsStore = instance.getExtensionList('com.cloudbees.plugins.credentials.SystemCredentialsProvider')[0].getStore()
        def domain = Domain.global()
        
        def sonarCredential = new UsernamePasswordCredentialsImpl(
            CredentialsScope.GLOBAL,
            sonarCredsId,
            "Identifiants Admin SonarQube (Auto)",
            "admin",
            "admin"
        )

        def existingCreds = credentialsStore.getCredentials(domain).find { it.id == sonarCredsId }
        if (existingCreds) {
            credentialsStore.updateCredentials(domain, existingCreds, sonarCredential)
        } else {
            credentialsStore.addCredentials(domain, sonarCredential)
        }
        println "--- [INIT] Credentials OK ---"

    } catch (Exception e) {
        println "--- [ERREUR] Credentials : ${e.message} ---"
    }

    // ========================================================================
    // PARTIE 2 : CONFIGURATION SONARQUBE 
    // ========================================================================
    try {
        println "--- [INIT] Configuration Serveur SonarQube... ---"
        
        def sonarServerName = "sonarqube-poc"
        def sonarServerUrl = "http://sonarqube-poc:9000"
        def sonarCredsId = "sonar-admin-creds"
        
        def sonarGlobalConfig = instance.getDescriptorByType(SonarGlobalConfiguration.class)
        

        def sonarInst = new SonarInstallation(
            sonarServerName,    // 1. Name
            sonarServerUrl,     // 2. Server Url
            sonarCredsId,       // 3. Credentials ID
            null,               // 4. Server Auth Token (obsolète/vide ici)
            null,               // 5. Webhook Secret
            null,               // 6. Extra Properties
            null,               // 7. Triggers
            null,               // 8. Mojo Version / Env Vars
            null                // 9. Additional Analysis Properties
        )
        
        // IMPORTANT : On passe un TABLEAU ([...]) à setInstallations
        sonarGlobalConfig.setInstallations([sonarInst] as SonarInstallation[])
        sonarGlobalConfig.save()
        
        println "--- [INIT] Serveur SonarQube '${sonarServerName}' activé avec succès ---"

    } catch (Exception e) {
        println "--- [ERREUR FATALE] Config Sonar : ${e.message} ---"
        e.printStackTrace()
    }

    // ========================================================================
    // PARTIE 3 : CONFIGURATION DU JOB 
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