#!/bin/bash

set -ev

set LIMIT_PARAMS="| tee jersey-build.log | grep -E '(---)|(Building)|(Tests run)|(T E S T S)'"
set VALIDATION_PARAM="-Ptravis_e2e"

if [ $1 == ${VALIDATION_PARAM} ]; then
    unset $LIMIT_PARAMS
fi

mvn -e -U -B clean install $1 2>&1 ${LIMIT_PARAMS}
echo '------------------------------------------------------------------------'
tail -100 jersey-build.log
#cd tests
#mvn -e -B test -Ptravis_e2e