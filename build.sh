find . -iname pom.xml | xargs -I pom mvn -f pom install
