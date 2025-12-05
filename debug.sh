#!/bin/bash

# mvn spring-boot:run

mvn -DskipTests spring-boot:run -Dspring-boot.run.arguments="--trade.summary.mock.enabled=true"