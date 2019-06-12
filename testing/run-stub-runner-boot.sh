#!/bin/bash
java -jar  -Dserver.port=8090 spring-cloud-contract-stub-runner-boot-2.1.1.RELEASE.jar \
  	--stubrunner.workOffline=true  \
	--stubrunner.stubs-mode="local" \
 	--stubrunner.ids=com.example:producer:+:8080 \



