#!/bin/sh

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
	sed "s%\${CONTROLLER}%${CONTROLLER}%" / ${subdir}/manifest.yml >${subdir}/expanded-manifest.yml
	(cd ${subdir}; cf push -f expanded-manifest.yml)
done
