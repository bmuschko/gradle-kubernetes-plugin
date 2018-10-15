#!/bin/bash

################################################################################
################################################################################
################################################################################
#
# Start (via up) or Stop (via down) a minikube/kubernetes instance.
#
# If minikube/kubernetes is not installed than an attempt will be made to
# install it using docker containers.
#
# *** Ideas in this script taken from: https://github.com/LiliC/travis-minikube
#
################################################################################
################################################################################
################################################################################

# so we can execute this script from any location
PARENT_PATH=$( cd "$(dirname "${BASH_SOURCE[0]}")" ; pwd -P )
cd "$PARENT_PATH/../"

# create minikube dirs it not already present
mkdir -p $HOME/.kube
mkdir -p $HOME/.minikube
touch $HOME/.kube/config

# check, and potentially create with sensible defaults, the necessary env-vars
export MINIKUBE_WANTUPDATENOTIFICATION=${MINIKUBE_WANTUPDATENOTIFICATION:="false"}
export MINIKUBE_WANTREPORTERRORPROMPT=${MINIKUBE_WANTREPORTERRORPROMPT:="false"}
export MINIKUBE_HOME=${MINIKUBE_HOME:="$HOME"}
export CHANGE_MINIKUBE_NONE_USER=${CHANGE_MINIKUBE_NONE_USER:="true"}
export KUBECONFIG=${KUBECONFIG:="$HOME/.kube/config"}

# stand up minikube/kubernetes instance
up () {

  # install minikube if not already present
  exit_status=`command -v minikube &> /dev/null; echo $?`
  if [ $exit_status -eq 1 ]; then
    curl -Lo minikube https://storage.googleapis.com/minikube/releases/latest/minikube-linux-amd64
    chmod +x minikube
    sudo cp minikube /usr/local/bin/
    rm minikube
  fi

  # install kubectl if not already present
  exit_status=`command -v kubectl &> /dev/null; echo $?`
  if [ $exit_status -eq 1 ]; then
    curl -Lo kubectl https://storage.googleapis.com/kubernetes-release/release/v1.9.0/bin/linux/amd64/kubectl
    chmod +x kubectl
    sudo cp kubectl /usr/local/bin/
    rm kubectl
  fi

  # if not already started attempt to start
  exit_status=`sudo minikube status &> /dev/null; echo $?`
  if [ $exit_status -eq 1 ]; then
    sudo minikube start --vm-driver=none --bootstrapper=localkube --kubernetes-version=v1.10.0
    minikube update-context

    # this for loop waits until kubectl can access the api server that Minikube has created
    for i in {1..150}; do # timeout for 5 minutes
       exit_status=`kubectl get po &> /dev/null; echo $?`
       if [ $exit_status -ne 1 ]; then
          break
      fi
      sleep 2
    done
  else
    echo minikube is already running...
  fi
}

# stop down minkube/kubernetes instance
down () {
  exit_status=`command -v minikube &> /dev/null; echo $?`
  if [ $exit_status -eq 0 ]; then sudo minikube stop
  fi
  exit 0
}

if [ "$1" == "up" ]; then up
elif [ "$1" == "down" ]; then down
else
   echo "Must supply either [up|down] command"
   exit 0
fi
