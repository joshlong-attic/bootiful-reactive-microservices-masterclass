find . -iname pom.xml | xargs -I pom mvn -DskipTests=true -f pom install
