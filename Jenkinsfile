pipeline {
    agent any

    environment {
        DOCKER_IMAGE = "pushpanjay/devops-app"
        IMAGE_TAG = "${BUILD_NUMBER}"
    }

    stages {

        //  CHECKOUT
        stage('Checkout') {
            steps {
                git branch: 'main',
                    url: 'https://github.com/Pushpanjay/devops-ci-cd-pipeline.git'
            }
        }

        //  LINT
        stage('Lint Check') {
            steps {
                sh 'mvn checkstyle:check'
            }
        }

        //  BUILD + TEST + SONAR (merged for efficiency)
        stage('Build, Test & SonarQube Analysis') {
            steps {
                withCredentials([string(credentialsId: 'sonar-token', variable: 'SONAR_TOKEN')]) {
                    withSonarQubeEnv('SonarQube') {
                        sh """
                        mvn clean verify sonar:sonar \
                        -Dsonar.login=$SONAR_TOKEN
                        """
                    }
                }
            }
        }

        //  QUALITY GATE
        stage('Quality Gate') {
            steps {
                timeout(time: 5, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }

        //  DOCKER BUILD
        stage('Docker Build') {
            steps {
                sh '''
                docker build -t $DOCKER_IMAGE:$IMAGE_TAG .
                '''
            }
        }

        //  TRIVY SCAN
        stage('Trivy Scan') {
            steps {
                sh '''
                trivy image --exit-code 1 --severity CRITICAL,HIGH $DOCKER_IMAGE:$IMAGE_TAG
                '''
            }
        }

        //  DOCKER PUSH
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
                    '''
                }
            }
        }

        //  DEPLOY TO NEXUS (optional - uncomment when ready)
        /*
        stage('Deploy to Nexus') {
            steps {
                withCredentials([usernamePassword(
                    credentialsId: 'nexus-creds',
                    usernameVariable: 'NEXUS_USER',
                    passwordVariable: 'NEXUS_PASS'
                )]) {
                    sh '''
                    mvn deploy -Dnexus.username=$NEXUS_USER -Dnexus.password=$NEXUS_PASS
                    '''
                }
            }
        }
        */
    }
}
