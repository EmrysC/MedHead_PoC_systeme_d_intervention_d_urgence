import jenkins.model.*
import hudson.model.*
import java.io.ByteArrayInputStream

Thread.start {
    println "--- [INIT] Attente de 1min pour stabilisation des plugins ---"
    sleep(60000) 
    println "--- [INIT] Demarrage de la configuration du Job ---"
    
    def jobName = "MedHead_Pipeline"
    def instance = Jenkins.getInstance()
    def xmlFile = new File("/var/jenkins_home/init.groovy.d/job_config.xml")

    try {
        if (xmlFile.exists()) {
            def jobXml = xmlFile.text
            def xmlStream = new ByteArrayInputStream(jobXml.getBytes("UTF-8"))
            
            // On verifie si le job existe pour ne pas recréer par dessus une erreur
            def existingJob = instance.getItem(jobName)
            if (existingJob != null) {
                println "--- [INFO] Le job existe deja, mise a jour de la config ---"
                existingJob.updateByXml(new javax.xml.transform.stream.StreamSource(xmlStream))
            } else {
                println "--- [INFO] Creation du nouveau job : ${jobName} ---"
                instance.createProjectFromXML(jobName, xmlStream)
            }
            
            // PAUSE CRUCIALE : Laisse à Jenkins le temps de finaliser l'écriture sur disque
            sleep(5000)
            instance.getItem(jobName).scheduleBuild2(0)
            println "--- [SUCCES] Configuration terminee et Build lance ---"
        } else {
            println "--- [ERREUR] Fichier XML introuvable dans le conteneur ---"
        }
    } catch (Exception e) {
        println "--- [ERREUR] Echec critique : ${e.message} ---"
        e.printStackTrace()
    }
}