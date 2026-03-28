pipeline {
    agent any

    // tools {
    //     maven 'maven3'   // Ensure configured in Jenkins
    //     jdk 'jdk17'
    // }

    environment {
        DOCKER_IMAGE = "pushpanjay/devops-app"
        IMAGE_TAG = "${BUILD_NUMBER}"
    }

    stages {

        // ✅ CHECKOUT
        stage('Checkout') {
            steps {
                git branch: 'main',
                    url: 'https://github.com/Pushpanjay/devops-ci-cd-pipeline.git'
            }
        }

        // 🔴 LINT (runs ONLY here, not in build lifecycle)
        stage('Lint Check') {
            steps {
                sh 'mvn checkstyle:check'
            }
        }

        // ✅ BUILD (no duplicate lint execution)
        stage('Build') {
            steps {
                sh 'mvn clean install -DskipTests'
            }
        }

        // ✅ TEST
        stage('Test') {
            steps {
                sh 'mvn test'
            }
        }

        // ✅ SONAR ANALYSIS
        stage('SonarQube Analysis') {
            steps {
                withSonarQubeEnv('sonar-server') {
                    sh 'mvn sonar:sonar'
                }
            }
        }

        // ✅ QUALITY GATE
        stage('Quality Gate') {
            steps {
                timeout(time: 2, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }

        // 🐳 DOCKER BUILD
        stage('Docker Build') {
            steps {
                sh '''
                docker build -t $DOCKER_IMAGE:$IMAGE_TAG .
                '''
            }
        }

        // 🔐 TRIVY SCAN
        stage('Trivy Scan') {
            steps {
                sh '''
                trivy image --exit-code 1 --severity CRITICAL,HIGH $DOCKER_IMAGE:$IMAGE_TAG
                '''
            }
        }

        // 🚀 DOCKER PUSH
        stage('Docker Push') {
            steps {
                withCredentials([usernamePassword(
                    credentialsId: 'dockerhub-creds',
                    usernameVariable: 'DOCKER_USER',
                    passwordVariable: 'DOCKER_PASS'
                )]) {
                    sh '''
                    echo $DOCKER_PASS | docker login -u $DOCKER_USER --password-stdin
                    docker push pushpanjay/devops-app:${BUILD_NUMBER}
                    '''
                }
            }
        }

        // 🚀 DEPLOY
        // stage('Deploy') {
        //     steps {
        //         sh '''
        //         docker stop devops-app || true
        //         docker rm devops-app || true
        //         docker run -d -p 8081:8080 --name devops-app $DOCKER_IMAGE:$IMAGE_TAG
        //         '''
        //     }
        // }
    }
}
