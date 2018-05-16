#!/bin/bash

mvn -e -U -B clean install -Ptravis_main -fn 2>&1 | tee jersey-build.log | grep -E '(---)|(Building)|(Tests run)|(T E S T S)'
echo '------------------------------------------------------------------------'
tail -100 jersey-build.log
cd tests
mvn -e -B test -Ptravis_e2e -fn