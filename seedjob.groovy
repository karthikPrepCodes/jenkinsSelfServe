// seedJob.groovy
import org.yaml.snakeyaml.Yaml
import jenkins.model.*
import hudson.model.*
import javaposse.jobdsl.plugin.ExecuteDslScripts
import java.nio.file.*

def workspace = new File(build.workspace.toString()) // if using system groovy, adjust getting workspace accordingly
def jobsDir = new File(workspace, "jobs")

if (!jobsDir.exists()) {
    println "No jobs directory found at: ${jobsDir}"
    return
}

def yaml = new Yaml()

jobsDir.listFiles().findAll{ it.name.endsWith('.yaml') || it.name.endsWith('.yml') }.each { file ->
    println "Processing job definition: ${file.name}"
    def config = yaml.load(file.text)

    if (config.disabled == true) {
        println "Skipping disabled job: ${config.name}"
        return
    }

    def jobName = config.name
    println "Creating/updating pipeline job: ${jobName}"

    pipelineJob(jobName) {
        description(config.description ?: "Auto-created pipeline from YAML: ${file.name}")

        if (config.cron) {
            triggers {
                cron(config.cron)
            }
        }

        definition {
            cpsScm {
                scm {
                    git {
                        remote {
                            url(config.repo)
                            if (config.credentialsId) {
                                credentials(config.credentialsId)
                            }
                        }
                        branch(config.branch ?: 'main')
                    }
                }
                scriptPath(config.jenkinsfilePath ?: 'Jenkinsfile')
            }
        }
    }

    println "Job ${jobName} processed."
}
