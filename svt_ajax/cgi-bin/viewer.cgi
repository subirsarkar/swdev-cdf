#!/bin/sh
set -o nounset

umask 002
echo Access at $(date) >> /tmp/viewer.log 
./viewer.exe -b -n -l -q 2>> /tmp/viewer.log

exit $?
