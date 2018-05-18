#!/bin/bash

set -ev

VALIDATION_PARAM="-Ptravis_e2e"

if [[ $1 == "$VALIDATION_PARAM" ]]; then
    mvn -e -U -B clean install $1 2>&1
else
    mvn -e -U -B clean install $1 2>&1 | tee jersey-build.log | grep -E '(---)|(Building)|(Tests run)|(T E S T S)'
fi

echo '------------------------------------------------------------------------'
tail -100 jersey-build.log
#cd tests
#mvn -e -B test -Ptravis_e2e