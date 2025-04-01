#!/bin/bash

jmh_test_rexexp=${1}
if [[ $jmh_test_rexexp ]]; then
   optional_test_restriction="-Djmh.tests="${jmh_test_rexexp}""
fi

# build benchmarks
./mvnw clean package

# Configure SDKMAN!
# NOTE: turn off interactive mode, see https://sdkman.io/usage#configuration
. ${HOME}/.sdkman/bin/sdkman-init.sh

#
# Use Eclipse Temurin
#
sdk install java 23.0.1-tem
sdk use java 23.0.1-tem

# Test with default C2 JIT--GraalWasm Interpreter
./mvnw exec:exec ${optional_test_restriction}
