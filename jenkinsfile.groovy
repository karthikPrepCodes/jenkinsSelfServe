pipeline {
  agent any

  tools {
    nodejs "node18"         // name must match the NodeJS tool name configured in Manage Jenkins -> Global Tool Configuration
  }

  environment {
    // disable browser auto-download if you want to control it in pipeline; leave it unset to download
    PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD = "0"
  }

  stages {
    stage('Checkout') {
      steps {
        checkout scm
      }
    }

    stage('Install Dependencies') {
      steps {
        sh 'npm ci'   // use npm ci for clean install
      }
    }

    stage('Install Playwright Browsers') {
      steps {
        // ensures required browsers are installed
        sh 'npx playwright install --with-deps'
      }
    }

    stage('Run Playwright Tests') {
      steps {
        // run tests; adjust additional CLI flags as needed
        sh 'npx playwright test --reporter=html'
      }
    }

    stage('Publish Report') {
      steps {
        // Playwright default HTML report goes into playwright-report directory
        archiveArtifacts artifacts: 'playwright-report/**', allowEmptyArchive: false
        publishHTML (target: [
          reportDir: 'playwright-report',
          reportFiles: 'index.html',
          reportName: 'Playwright HTML Report',
          keepAll: true
        ])
      }
    }
  }

  post {
    always {
      echo "Pipeline finished. See test report."
    }
    failure {
      mail to: 'you@example.com',
           subject: "Playwright Nightly - Build Failed: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
           body: "Check Jenkins console: ${env.BUILD_URL}"
    }
  }
}
