#!/bin/bash
SCRIPTDIR=$(dirname $0)

curdir="$(dirname $(readlink -f ${0}))"
source "${curdir}/../../scripts/logger.sh"

#parameters:
# {1} path to folder with installation scripts, roles,... (usually templates/install)
# {2} url to OpenShift origin client (default value is set to oc version v3.7.0)
ENMASSE_DIR=${1}
OPENSHIFT_CLIENT_URL=${2:-"https://github.com/openshift/origin/releases/download/v3.7.0/openshift-origin-client-tools-v3.7.0-7ed6862-linux-64bit.tar.gz"}
ansible-playbook ${ENMASSE_DIR}/ansible/playbooks/openshift/environment.yml \
    --extra-vars "openshift_client_url=${OPENSHIFT_CLIENT_URL}" -t openshift,kubectl

oc cluster down #for the case that cluster is already running


if oc status; then
    err_and_exit "shutting down of openshift cluster failed, tests won't be executed"
fi

sudo rm -rf /var/lib/origin/openshift.local.pv
sudo rm -rf /var/log/containers/*
sudo rm -rf /var/log/pods/*

DOCKER_STATUS=$(sudo systemctl show --property ActiveState ${DOCKER} | sed -n -e 's/^ActiveState=//p')
if [[ "${DOCKER_STATUS}" != "active" ]]; then
    info "Docker service is not running"
    info "Starting docker service"
    sudo systemctl restart ${DOCKER}
fi

oc cluster up --service-catalog "${OC_CLUSTER_ARGS}"
if [ ! $? -ne 0 ]; then
    warn "OpenShift cluster didn't start properly, wait for 30s and try to restart..."
    sleep 30
    oc cluster down
    oc cluster up --service-catalog "${OC_CLUSTER_ARGS}"
fi
oc login -u system:admin

TIMEOUT=300
NOW=$(date +%s)
END=$(($NOW + $TIMEOUT))
info "Now: $(date -d@${NOW} -u +%F:%H:%M:%S)"
info "Waiting ${TIMEOUT} seconds until: $(date -d@${END} -u +%F:%H:%M:%S)"

oc cluster status
while [ $? -gt 0 ]
do
    NOW=$(date +%s)
    if [ ${NOW} -gt ${END} ]; then
        err_and_exit "ERROR: Timed out waiting for openshift cluster to come up!"
    fi
    sleep 5
    oc cluster status
done
sleep 30
oc get pv
