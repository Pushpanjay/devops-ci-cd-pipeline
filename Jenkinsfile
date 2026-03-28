pipeline {
    agent any

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

        // ✅ LINT
        stage('Lint Check') {
            steps {
                sh 'mvn checkstyle:check'
            }
        }

        // ✅ BUILD + TEST
        stage('Build & Test') {
            steps {
                sh 'mvn clean verify'
            }
        }

        // ✅ SONAR
        // stage('SonarQube Analysis') {
        //     steps {
        //         withSonarQubeEnv('sonar-server') {
        //             sh 'mvn sonar:sonar'
        //         }
        //     }
        // }

        // // ✅ QUALITY GATE
        // stage('Quality Gate') {
        //     steps {
        //         timeout(time: 2, unit: 'MINUTES') {
        //             waitForQualityGate abortPipeline: true
        //         }
        //     }
        // }

        // ✅ DEPLOY TO NEXUS
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

        // 🐳 DOCKER BUILD
        stage('Docker Build') {
            steps {
                sh '''
                docker build -t $DOCKER_IMAGE:$IMAGE_TAG .
                '''
            }
        }

        // 🔐 TRIVY
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
                    docker push $DOCKER_IMAGE:$IMAGE_TAG
                    '''
                }
            }
        }
    }
}
