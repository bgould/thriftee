#!/bin/bash
mvn install -DskipTests=true -Dfindbugs.skip=true && (cd examples && mvn tomee:run)

