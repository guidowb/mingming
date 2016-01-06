#!/bin/sh

DEPLOYMENT=$1
if [ -z "${DEPLOYMENT}" ]; then
	echo "Must specify DEPLOYMENT name"
	exit 1
fi
if [ -z "${CONTROLLER}" ]; then
	echo "Must set CONTROLLER variable to the URL of your mingming controller"
	exit 1
fi

for subdir in *
do
	if [ ! -d ${subdir} ]; then
		continue
	fi
	echo Pushing ${subdir}
	sed "s%\${CONTROLLER}%${CONTROLLER}%;s%\${DEPLOYMENT}%${DEPLOYMENT}%" / ${subdir}/manifest.yml >${subdir}/expanded-manifest.yml
	(cd ${subdir}; cf push -f expanded-manifest.yml)
done
