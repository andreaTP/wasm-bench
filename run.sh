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
# Run tests on Oracle GraalVM with Oracle Graal JIT
#
sdk install java 23.0.1-graal
sdk use java 23.0.1-graal
./mvnw exec:exec ${optional_test_restriction}

#
# Run tests on GraalVM CE with Graal CE JIT
#
# sdk install java 23.0.1-graalce
# sdk use java 23.0.1-graalce
# ./mvnw exec:exec ${optional_test_restriction}

#
# Use Eclipse Temurin
#
# sdk install java 23.0.1-tem
# sdk use java 23.0.1-tem

# Test with default C2 JIT--GraalWasm Interpreter
# ./mvnw exec:exec ${optional_test_restriction}

# Test with Truffle Compilation enabled
# ./mvnw exec:exec -Ptruffle-jit ${optional_test_restriction}
