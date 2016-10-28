#!/bin/bash
mvn install -DskipTests=true -Dfindbugs.skip=true && (cd thriftee-examples-war && mvn tomee:run -Ptomee-with-debug-and-hotswap)

