#!/bin/sh

for subdir in *
do
	if [ ! -d ${subdir} ]; then
		continue
	fi
	echo Building ${subdir}
	(cd ${subdir}; ./build.sh)
done
