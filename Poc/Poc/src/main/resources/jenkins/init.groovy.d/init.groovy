import jenkins.model.*
import hudson.model.*
import java.io.File
import java.io.ByteArrayInputStream
import javax.xml.transform.stream.StreamSource

Thread.start {
    println "--- [INIT] Attente de 15s ---"
    sleep(15000) 
    
    def jobName = "MedHead_Pipeline"
    def instance = Jenkins.getInstance()
    def xmlPath = "/var/jenkins_home/init.groovy.d/job_config.xml"
    def xmlFile = new File(xmlPath)

    try {
        if (xmlFile.exists()) {
            def jobXml = xmlFile.text
            def existingJob = instance.getItem(jobName)
            
            if (existingJob != null) {
                def xmlStream = new ByteArrayInputStream(jobXml.getBytes("UTF-8"))
                existingJob.updateByXml(new StreamSource(xmlStream))
                existingJob.save()
            } else {
                def xmlStream = new ByteArrayInputStream(jobXml.getBytes("UTF-8"))
                instance.createProjectFromXML(jobName, xmlStream)
            }

            instance.save()
            sleep(2000)
            
            def job = instance.getItem(jobName)
            if (job != null) {
                job.scheduleBuild2(0)
                println "--- [SUCCES] Build lance ---"
            }
        }
    } catch (Exception e) {
        e.printStackTrace()
    }
}