import jenkins.model.*
import hudson.model.*
import hudson.plugins.sonar.*
import hudson.plugins.sonar.model.*
import com.cloudbees.plugins.credentials.*
import com.cloudbees.plugins.credentials.domains.*
import hudson.util.Secret
import java.io.ByteArrayInputStream
import javax.xml.transform.stream.StreamSource

// Initialisation asynchrone pour ne pas bloquer le démarrage de Jenkins
Thread.start {
    println "--- [INIT] Attente de 30s pour stabilisation de Jenkins ---"
    sleep(30000) 

    def instance = Jenkins.get()
    
    // ========================================================================
    // PARTIE 1 : ENREGISTREMENT DU SERVEUR SONARQUBE
    // ========================================================================
    try {
        println "--- [INIT] Liaison du serveur 'sonarqube-poc' au token 'SONARCUBE_TOKEN' ---"
        
        def sonarServerName = "sonarqube-poc"
        def sonarServerUrl = "http://sonarqube-poc:9000"
        def sonarCredsId = "SONARCUBE_TOKEN" // Utilisation de votre ID existant
        
        def sonarGlobalConfig = instance.getDescriptorByType(SonarGlobalConfiguration.class)
        
        // On crée l'installation en pointant sur votre credential "Secret Text"
        def sonarInst = new SonarInstallation(
            sonarServerName,    // Name
            sonarServerUrl,     // Server URL
            sonarCredsId,       // Credentials ID (votre token Jenkins)
            null,               // Webhook Secret ID
            null,               // Extra Properties
            null,               // Triggers
            null,               // Mojo Version
            null,               // Additional Analysis Properties
            null                // Additional Analysis Properties (versioning plugin)
        )
        
        // Application de la configuration (en tableau pour la compatibilité)
        sonarGlobalConfig.setInstallations([sonarInst] as SonarInstallation[])
        sonarGlobalConfig.save()
        instance.save()
        
        println "--- [INIT] Serveur '${sonarServerName}' configuré avec succès ---"

    } catch (Exception e) {
        println "--- [ERREUR] Configuration Serveur Sonar : ${e.message} ---"
    }

    // ========================================================================
    // PARTIE 2 : CONFIGURATION DU JOB (Pipeline)
    // ========================================================================
    def jobName = "MedHead_Pipeline"
    def xmlPath = "/var/jenkins_home/init.groovy.d/job_config.xml"
    def xmlFile = new File(xmlPath)

    try {
        if (xmlFile.exists()) {
            println "--- [INIT] Synchronisation du Job : ${jobName} ---"
            def jobXml = xmlFile.text
            def existingJob = instance.getItem(jobName)
            
            if (existingJob != null) {
                def xmlStream = new ByteArrayInputStream(jobXml.getBytes("UTF-8"))
                existingJob.updateByXml(new StreamSource(xmlStream))
                existingJob.save()
                println "--- [INIT] Job mis à jour ---"
            } else {
                def xmlStream = new ByteArrayInputStream(jobXml.getBytes("UTF-8"))
                instance.createProjectFromXML(jobName, xmlStream)
                println "--- [INIT] Job créé ---"
            }

            instance.save()
            sleep(5000) // Petit délai avant le premier build
            
            def job = instance.getItem(jobName)
            if (job != null && !job.isBuilding() && !job.isInQueue()) {
                job.scheduleBuild2(0)
                println "--- [SUCCÈS] Job '${jobName}' prêt et build lancé ---"
            }
        } else {
            println "--- [ALERTE] Fichier ${xmlPath} introuvable, job non configuré ---"
        }
    } catch (Exception e) {
        println "--- [ERREUR] Initialisation du Job : ${e.message} ---"
    }
}