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

        //  REPLACED BUILD STAGE
        stage('Build, Test & SonarQube Analysis') {
            steps {
                withSonarQubeEnv('sonar-server') {
                    sh 'mvn clean verify sonar:sonar'
                }
            }
        }

        //  MUST come immediately after sonar
        stage('Quality Gate') {
            steps {
                timeout(time: 5, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }

        stage('Docker Build') {
            steps {
                sh '''
                docker build -t $DOCKER_IMAGE:$IMAGE_TAG .
                '''
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
                withCredentials([[
                    $class: 'AmazonWebServicesCredentialsBinding',
                    credentialsId: 'aws-creds'
                ]]) {
                    sh '''
                    set -e

                    echo "Updating kubeconfig..."
                    aws eks --region $AWS_REGION update-kubeconfig --name $CLUSTER_NAME

                    echo "Applying Kubernetes manifests..."
                    kubectl apply -f k8s/namespace.yaml
                    kubectl apply -f k8s/configmap.yaml || true
                    kubectl apply -f k8s/secret.yaml || true

                    kubectl apply -f k8s/deployment.yaml
                    kubectl apply -f k8s/service.yaml

                    echo "Updating image..."
                    kubectl set image deployment/devops-app \
                    devops-app=$DOCKER_IMAGE:$IMAGE_TAG -n devops

                    echo "Checking rollout..."
                    kubectl rollout status deployment/devops-app -n devops
                    '''
                }
            }
        }
    }

    post {
        always {
            cleanWs()
        }
    }
}
