#!/bin/bash
mvn clean install -DskipTests=true -Dfindbugs.skip=true && (cd thriftee-examples-war && mvn tomee:run)
