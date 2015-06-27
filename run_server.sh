#!/bin/bash
mvn clean install
(cd thriftee-examples-war && mvn tomee:run)
