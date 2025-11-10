// seedjob.groovy
// Job DSL script (no external YAML dependency) to create a Playwright nightly pipeline job

pipelineJob('Playwright_Nightly_Run') {
    description('Auto-created Playwright nightly job (from seed-job)')
    triggers {
        cron('H 2 * * *') // nightly around 2 AM
    }
    definition {
        cpsScm {
            scm {
                git {
                    remote {
                        url('https://github.com/karthikPrepCodes/VoltMoney-Assesment-UI-Automation.git')
                    }
                    branch('main')
                }
            }
            scriptPath('Jenkinsfile') // ensure Jenkinsfile exists in the test repo root
        }
    }
}
