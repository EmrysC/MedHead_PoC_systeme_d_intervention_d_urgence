import jenkins.model.*
import hudson.model.*
import java.io.File
import java.io.ByteArrayInputStream
import javax.xml.transform.stream.StreamSource

Thread.start {
    // Augmenté à 30s pour laisser le temps aux plugins Docker/Git de charger
    println "--- [INIT] Attente de 30s pour stabilisation Jenkins ---"
    sleep(30000) 
    
    def jobName = "MedHead_Pipeline"
    def instance = Jenkins.getInstance()
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

            // Forcer Jenkins à sauvegarder l'état global
            instance.save()
            // Recharger la configuration pour être sûr que le XML est bien pris en compte
            instance.reload() 
            
            sleep(5000) // Petit délai après reload
            
            def job = instance.getItem(jobName)
            if (job != null) {
                // On vérifie si un build n'est pas déjà en cours avant d'en lancer un
                if (!job.isBuilding() && !job.isInQueue()) {
                    job.scheduleBuild2(0)
                    println "--- [SUCCES] Job ${jobName} configuré et build lancé ---"
                } else {
                    println "--- [INFO] Job déjà en cours d'exécution, pas de nouveau build lancé ---"
                }
            }
        } else {
            println "--- [ERREUR] Fichier ${xmlPath} introuvable ---"
        }
    } catch (Exception e) {
        println "--- [ERREUR] Échec de l'init du job : ${e.message} ---"
        e.printStackTrace()
    }
}