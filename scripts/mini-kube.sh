#!/usr/bin/env bash

export CHANGE_MINIKUBE_NONE_USER=true
KUBE_VERSION="v1.9.4"

# Download kubectl, which is a requirement for using minikube.
curl -Lo kubectl https://storage.googleapis.com/kubernetes-release/release/$KUBE_VERSION/bin/linux/amd64/kubectl && chmod +x kubectl && sudo mv kubectl /usr/local/bin/

# Download minikube and start.
curl -Lo minikube https://storage.googleapis.com/minikube/releases/latest/minikube-linux-amd64 && chmod +x minikube && sudo mv minikube /usr/local/bin/
sudo minikube start --vm-driver=none --bootstrapper=localkube --kubernetes-version=$KUBE_VERSION

# Fix the kubectl context, as it's often stale.
sudo minikube update-context

# Wait for Kubernetes to be up and ready.
JSONPATH='{range .items[*]}{@.metadata.name}:{range @.status.conditions[*]}{@.type}={@.status};{end}{end}'; until kubectl get nodes -o jsonpath="$JSONPATH" 2>&1 | grep -q "Ready=True"; do sleep 1; done
sudo kubectl cluster-info
