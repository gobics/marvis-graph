#!/bin/sh
if [ -z "$(which java)" ]; then 
	echo "ERROR: can not find java virtual machine" >&2;
fi

if [ $(java -version 2>&1 | head -n 1 | cut -d\" -f 2 | cut -d. -f2) -lt 7 ]; then
	echo "ERROR: java virtual machine 1.7 required, but $(java -version 2>&1 | head -n 1 | cut -d\" -f 2) available" >&2
fi

exec java -jar marvis-graph.jar $*
