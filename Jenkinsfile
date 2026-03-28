pipeline {
    agent any

    environment {
        DOCKER_IMAGE = "pushpanjay/devops-app"
        IMAGE_TAG = "${BUILD_NUMBER}"
    }

    stages {

        stage('Checkout') {
            steps {
                git branch: 'main', url: 'https://github.com/Pushpanjay/devops-ci-cd-pipeline.git'
            }
        }

        stage('Build + SonarQube') {
            steps {
                withSonarQubeEnv('sonar-server') {
                    sh 'mvn clean verify sonar:sonar'
                }
            }
        }

        stage('Quality Gate') {
            steps {
                timeout(time: 2, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }

        stage('Docker Build') {
            steps {
                sh 'docker build -t $DOCKER_IMAGE:$IMAGE_TAG .'
            }
        }

        stage('Docker Login & Push') {
            steps {
                withCredentials([usernamePassword(
                    credentialsId: 'dockerhub-creds',
                    usernameVariable: 'DOCKER_USER',
                    passwordVariable: 'DOCKER_PASS'
                )]) {
                    sh '''
                    echo "$DOCKER_PASS" | docker login -u "$DOCKER_USER" --password-stdin
                    docker push $DOCKER_IMAGE:$IMAGE_TAG
                    docker logout
                    '''
                }
            }
        }
    }

    post {
        success {
            echo "Pipeline succeeded. Image pushed: ${DOCKER_IMAGE}:${IMAGE_TAG}"
        }
        failure {
            echo 'Pipeline failed. Check logs.'
        }
    }
}
