import jenkins.model.*
import hudson.model.*
import java.io.File
import java.io.ByteArrayInputStream
import javax.xml.transform.stream.StreamSource

// On execute dans un thread pour ne pas bloquer le demarrage de l'interface UI
Thread.start {
    // pause de 15s pour que Jenkins charge ses modules de base
    println "--- [INIT] Attente de 15s pour stabilisation ---"
    sleep(15000) 
    
    def jobName = "MedHead_Pipeline"
    def instance = Jenkins.getInstance()
    // Chemin interne au conteneur (mappé via ton volume docker-compose)
    def xmlPath = "/var/jenkins_home/init.groovy.d/job_config.xml"
    def xmlFile = new File(xmlPath)

    println "--- [INIT] Verification de la configuration du Job ---"

    try {
        if (xmlFile.exists()) {
            def jobXml = xmlFile.text
            
            // On verifie si le job existe déjà
            def existingJob = instance.getItem(jobName)
            
            if (existingJob != null) {
                println "--- [INFO] Le job existe deja : Mise a jour de la configuration ---"
                def xmlStream = new ByteArrayInputStream(jobXml.getBytes("UTF-8"))
                existingJob.updateByXml(new StreamSource(xmlStream))
                existingJob.save()
            } else {
                println "--- [INFO] Creation du nouveau job : ${jobName} ---"
                def xmlStream = new ByteArrayInputStream(jobXml.getBytes("UTF-8"))
                instance.createProjectFromXML(jobName, xmlStream)
            }

            // SAUVEGARDE GLOBALE : Empeche le crash au redemarrage