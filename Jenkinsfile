pipeline {
    agent any

    environment {
        DOCKER_IMAGE = "pushpanjay/devops-app"
        IMAGE_TAG = "${BUILD_NUMBER}"
    }

    stages {

        stage('Checkout') {
            steps {
                git 'https://github.com/Pushpanjay/devops-ci-cd-pipeline.git'
            }
        }

        // 🔴 LINTER STAGE
        stage('Lint Check') {
            steps {
                sh 'mvn checkstyle:check'
            }
        }

        stage('Build & Test') {
            steps {
                sh 'mvn clean install'
            }
        }

        stage('SonarQube Analysis') {
            steps {
                withSonarQubeEnv('sonar-server') {
                    sh 'mvn sonar:sonar'
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

        // 🔵 Nexus Upload
        // stage('Upload to Nexus') {
        //     steps {
        //         withCredentials([usernamePassword(
        //             credentialsId: 'nexus-creds',
        //             usernameVariable: 'NEXUS_USER',
        //             passwordVariable: 'NEXUS_PASS'
        //         )]) {

        //             sh '''
        //             mvn deploy \
        //             -Dnexus.username=$NEXUS_USER \
        //             -Dnexus.password=$NEXUS_PASS
        //             '''
        //         }
        //     }
        // }

        stage('Docker Build') {
            steps {
                sh '''
                docker build -t $DOCKER_IMAGE:$IMAGE_TAG .
                '''
            }
        }

        stage('Trivy Scan') {
            steps {
                sh '''
                trivy image --exit-code 1 --severity CRITICAL,HIGH $DOCKER_IMAGE:$IMAGE_TAG
                '''
            }
        }

        stage('Docker Push') {
            steps {
                withCredentials([usernamePassword(
                    credentialsId: 'docker-creds',
                    usernameVariable: 'USER',
                    passwordVariable: 'PASS'
                )]) {

                    sh '''
                    echo $PASS | docker login -u $USER --password-stdin
                    docker push $DOCKER_IMAGE:$IMAGE_TAG
                    '''
                }
            }
        }

        stage('Deploy (Docker)') {
            steps {
                sh '''
                docker stop devops-app || true
                docker rm devops-app || true
                docker run -d -p 8081:8080 --name devops-app $DOCKER_IMAGE:$IMAGE_TAG
                '''
            }
        }
    }
}
