import jenkins.model.*
import hudson.model.*
import java.io.File
import java.io.ByteArrayInputStream
import javax.xml.transform.stream.StreamSource
import hudson.plugins.sonar.*
import hudson.plugins.sonar.model.*
import com.cloudbees.plugins.credentials.*
import com.cloudbees.plugins.credentials.domains.*
import com.cloudbees.plugins.credentials.impl.* 
import org.jenkinsci.plugins.plaincredentials.impl.*
import hudson.util.Secret

Thread.start {
    // Augmenté à 30s pour laisser le temps aux plugins Docker/Git/Sonar de charger
    println "--- [INIT] Attente de 30s pour stabilisation Jenkins ---"
    sleep(30000) 

    def instance = Jenkins.getInstance()
    
    // ========================================================================
    // PARTIE 1 : CONFIGURATION SONARQUBE (Mode Login/Password)
    // ========================================================================
    try {
        println "--- [INIT] Début configuration SonarQube (Admin/Admin) ---"
        
        def sonarServerName = "sonarqube-poc"
        def sonarServerUrl = "http://sonarqube-poc:9000"
        def sonarCredsId = "sonar-admin-creds" // ID interne dans Jenkins
        
        // A. Création des identifiants (Username / Password)
        def credentialsStore = instance.getExtensionList('com.cloudbees.plugins.credentials.SystemCredentialsProvider')[0].getStore()
        def domain = Domain.global()
        
        // On crée l'objet "Username with Password" avec admin/admin
        def sonarCredential = new UsernamePasswordCredentialsImpl(
            CredentialsScope.GLOBAL,
            sonarCredsId,
            "Identifiants Admin SonarQube (Auto)",
            "admin", // Login
            "admin"  // Mot de passe
        )

        // On met à jour ou on crée
        def existingCreds = credentialsStore.getCredentials(domain).find { it.id == sonarCredsId }
        if (existingCreds) {
            credentialsStore.updateCredentials(domain, existingCreds, sonarCredential)
            println "--- [INIT] Credentials 'admin/admin' mis à jour ---"
        } else {
            credentialsStore.addCredentials(domain, sonarCredential)
            println "--- [INIT] Credentials 'admin/admin' créés ---"
        }

        // B. Configuration du Serveur dans Jenkins
        def sonarGlobalConfig = instance.getDescriptorByType(SonarGlobalConfiguration.class)
        def sonarInst = new SonarInstallation(
            sonarServerName,
            sonarServerUrl,
            sonarCredsId, // On lie l'ID créé juste avant
            null, null, null, null, null
        )
        sonarGlobalConfig.setInstallations(sonarInst)
        sonarGlobalConfig.save()
        println "--- [INIT] Serveur SonarQube '${sonarServerName}' activé avec admin/admin ---"

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