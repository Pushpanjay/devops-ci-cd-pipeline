pipeline {
    agent any

    environment {
        DOCKER_IMAGE = "pushpanjay/devops-app"
        IMAGE_TAG = "${BUILD_NUMBER}"
        AWS_REGION = "us-east-1"
        CLUSTER_NAME = "devops-cluster"
    }

    stages {

        stage('Checkout') {
            steps {
                git branch: 'main',
                url: 'https://github.com/Pushpanjay/devops-ci-cd-pipeline.git'
            }
        }

        stage('Lint Check') {
            steps {
                sh 'mvn checkstyle:check || true'
            }
        }

        stage('Build') {
            steps {
                sh 'mvn clean package -DskipTests'
            }
        }

        stage('Test Jar') {
            steps {
                sh '''
                timeout 15s java -jar target/*.jar || exit 1
                '''
            }
        }

        stage('Docker Build') {
            steps {
                sh 'docker build --no-cache -t $DOCKER_IMAGE:$IMAGE_TAG .'
            }
        }

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

        stage('Deploy to EKS') {
            steps {
                sh '''
                aws eks --region $AWS_REGION update-kubeconfig --name $CLUSTER_NAME

                kubectl apply -f k8s/deployment.yaml
                kubectl apply -f k8s/service.yaml

                kubectl set image deployment/devops-app devops-app=$DOCKER_IMAGE:$IMAGE_TAG

                kubectl rollout status deployment/devops-app
                '''
            }
        }

    }

    post {
        always {
            cleanWs()
        }
    }
}
