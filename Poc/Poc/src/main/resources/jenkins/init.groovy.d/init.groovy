import jenkins.model.*
import hudson.model.*
import hudson.plugins.sonar.*
import hudson.plugins.sonar.model.*
import hudson.plugins.sonar.utils.*
import com.cloudbees.plugins.credentials.*
import com.cloudbees.plugins.credentials.domains.*
import com.cloudbees.plugins.credentials.impl.* import org.jenkinsci.plugins.plaincredentials.impl.*
import hudson.util.Secret
import java.io.ByteArrayInputStream
import javax.xml.transform.stream.StreamSource

Thread.start {
    println "--- [INIT] Attente de 30s pour stabilisation Jenkins ---"
    sleep(30000) 

    def instance = Jenkins.getInstance()
    
    // ========================================================================
    // PARTIE 1 : CONFIGURATION SONARQUBE
    // ========================================================================
    try {
        println "--- [INIT] Début configuration SonarQube (Admin/Admin) ---"
        
        def sonarServerName = "sonarqube-poc"
        def sonarServerUrl = "http://sonarqube-poc:9000"
        def sonarCredsId = "sonar-admin-creds"
        
        // 1. Création des identifiants (Username / Password)
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
            println "--- [INIT] Credentials mis à jour ---"
        } else {
            credentialsStore.addCredentials(domain, sonarCredential)
            println "--- [INIT] Credentials créés ---"
        }

        // 2. Configuration du Serveur 
        def sonarGlobalConfig = instance.getDescriptorByType(SonarGlobalConfiguration.class)
        
        // Utilisation du constructeur compatible avec les versions récentes
        // Nom, Url, TokenId
        def sonarInst = new SonarInstallation(
            sonarServerName,
            sonarServerUrl,
            sonarCredsId,
            null, // mojoVersion
            null, // additionalProperties
            null, // additionalAnalysisProperties
            null, // triggers
            null  // sonarRunnerName
        )
        
        sonarGlobalConfig.setInstallations(sonarInst)
        sonarGlobalConfig.save()
        println "--- [INIT] Serveur SonarQube '${sonarServerName}' activé ---"

    } catch (Exception e) {
        println "--- [ERREUR] Problème config SonarQube : ${e.message} ---"
        // Tentative de fallback (plan B) pour les versions très récentes
        try {
             println "--- [INFO] Tentative Plan B pour SonarQube ---"
             def desc = Jenkins.instance.getDescriptorByType(SonarGlobalConfiguration.class)
             def inst = new SonarInstallation(
                "sonarqube-poc",
                "http://sonarqube-poc:9000",
                "sonar-admin-creds"
             )
             desc.setInstallations(inst)
             desc.save()
             println "--- [SUCCES] Plan B réussi ---"
        } catch (Exception e2) {
             println "--- [ERREUR FATALE] Impossible de configurer Sonar : ${e2.message} ---"
        }
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