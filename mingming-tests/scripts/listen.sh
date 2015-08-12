#!/bin/sh

export timestamp="1"

while (true)
do
	output=`curl -s "$1/canaries/events?since=$timestamp" | python ~/bin/pp.py 2>&1`
	timestamp=`echo "$output" | grep '\s*"timestamp":' | sed 's/^.*"timestamp": //'`
	echo "$output"
	echo $timestamp
	sleep 5
done
