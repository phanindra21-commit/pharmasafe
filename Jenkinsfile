// PharmaSafe CI/CD pipeline
// GitHub -> Jenkins -> Docker (build + JUnit tests) -> Kubernetes
// Written for a Jenkins agent on Windows (uses "bat"). On Linux, replace bat with sh
// and %BUILD_NUMBER% with ${BUILD_NUMBER}.
pipeline {
    agent any

    environment {
        IMAGE = 'pharmasafe'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build & Test (Docker)') {
            steps {
                // The Dockerfile runs "mvn package", which runs all JUnit tests.
                // A failing test fails this stage and stops the pipeline.
                bat 'docker build -t %IMAGE%:%BUILD_NUMBER% -t %IMAGE%:latest .'
            }
        }

        stage('Deploy to Kubernetes') {
            steps {
                bat 'kubectl apply -f k8s/postgres.yaml'
                bat 'kubectl apply -f k8s/app.yaml'
                bat 'kubectl set image deployment/pharmasafe app=%IMAGE%:%BUILD_NUMBER%'
                bat 'kubectl rollout status deployment/pharmasafe --timeout=180s'
            }
        }

        stage('Smoke Test') {
            steps {
                bat 'kubectl get pods -o wide'
                bat 'curl -fsS http://localhost:30080/api/dashboard/summary'
            }
        }
    }

    post {
        success { echo "Deployed ${env.IMAGE}:${env.BUILD_NUMBER} - open http://localhost:30080" }
        failure { echo 'Pipeline failed - check the stage logs above.' }
    }
}
