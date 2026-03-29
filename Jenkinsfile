pipeline {
    agent any

    environment {
        DOCKER_IMAGE = "pushpanjay/devops-app"
        IMAGE_TAG = "${BUILD_NUMBER}"
    }

    stages {

        // CHECKOUT
        stage('Checkout') {
            steps {
                git branch: 'main',
                    url: 'https://github.com/Pushpanjay/devops-ci-cd-pipeline.git'
            }
        }

        // LINT
        stage('Lint Check') {
            steps {
                sh 'mvn checkstyle:check'
            }
        }

        // BUILD + TEST + SONAR
        stage('Build, Test & SonarQube Analysis') {
            steps {
                withSonarQubeEnv('sonar-server') {
                    sh 'mvn clean verify sonar:sonar'
                }
            }
        }

        // QUALITY GATE
        stage('Quality Gate') {
            steps {
                timeout(time: 5, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }

        // DOCKER BUILD
        stage('Docker Build') {
            steps {
                sh 'docker build -t $DOCKER_IMAGE:$IMAGE_TAG .'
            }
        }

        // TRIVY SCAN
        stage('Trivy Scan') {
            steps {
                sh 'trivy image --exit-code 1 --severity CRITICAL,HIGH $DOCKER_IMAGE:$IMAGE_TAG'
            }
        }

        // DOCKER PUSH
        stage('Docker Push') {
            steps {
                withCredentials([usernamePassword(
                    credentialsId: 'dockerhub-creds',
                    usernameVariable: 'DOCKER_USER',
                    passwordVariable: 'DOCKER_PASS'
                )]) {
                    sh '''
                    echo $DOCKER_PASS | docker login -u $DOCKER_USER --password-stdin
                    docker push $DOCKER_IMAGE:$IMAGE_TAG
                    docker logout
                    '''
                }
            }
        }

        //  FIXED: DEPLOY TO NEXUS
        stage('Deploy to Nexus') {
            steps {
                configFileProvider([configFile(
                    fileId: 'nexus-settings',
                    variable: 'MAVEN_SETTINGS'
                )]) {
                    sh 'mvn clean deploy --settings $MAVEN_SETTINGS'
                }
            }
        }
    }
}
