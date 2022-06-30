# RM Database Addon for Alfresco

To run use `mvn clean install -DskipTests=true alfresco:run` or `./run.sh` and verify that it 

 * Runs the embedded Tomcat + H2 DB 
 * Runs Alfresco Platform (Repository)
 * Runs Alfresco Solr4
 * Packages both as JAR and AMP assembly
 
 Try cloning it, change the port and play with `enableShare`, `enablePlatform` and `enableSolr`. 
 
 Protip: This module will work just fine as a Share module if the files are changed and 
 if the enablePlatform and enableSolr is disabled.
 
# Few things to notice

 * No parent pom
 * WAR assembly is handled by the Alfresco Maven Plugin configuration
 * Standard JAR packaging and layout
 * Works seamlessly with Eclipse and IntelliJ IDEA
 * JRebel for hot reloading, JRebel maven plugin for generating rebel.xml, agent usage: `MAVEN_OPTS=-Xms256m -Xmx1G -agentpath:/home/martin/apps/jrebel/lib/libjrebel64.so`
 * AMP as an assembly
 * [Configurable Run mojo](https://github.com/Alfresco/alfresco-sdk/blob/sdk-3.0/plugins/alfresco-maven-plugin/src/main/java/org/alfresco/maven/plugin/RunMojo.java) in the `alfresco-maven-plugin`
 * No unit testing/functional tests just yet
 * Resources loaded from META-INF
 * Web Fragment (this includes a sample servlet configured via web fragment)
 * Only works with Java Version 8
   * To force Java version, simply delete other versions from system by deleting their folders in `/Library/Java/JavaVirtualMachines/`
   * Download Java Version 8: (https://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)
 
# TODO
 
  * Abstract assembly into a dependency so we don't have to ship the assembly in the archetype
  * Purge, 
  * Functional/remote unit tests

# Architecture

The frontend is an angularjs application that connects with the alfresco backend through the endpoints placed here:
src/alfresco-rm-database-alfresco/src/main/java/dk/magenta/webscripts

The businesslogic have been placed in beans here:
fhp/src/alfresco-rm-database-alfresco/src/main/java/dk/magenta/beans

The Alfresco backend has been installed using the Alfresco installer that was available for version 5.2 The root folder for alfresco is:
/appl/alfresco


# Datamodels

The datamodel for the Observand is found in the file:
src/alfresco-rm-database-alfresco/src/main/resources/alfresco/module/rm-database-alfresco/model/content-model.xml

The datamodel for the Psychologist is found in the file:
src/alfresco-rm-database-alfresco/src/main/resources/alfresco/module/rm-database-alfresco/model/psych_content-model.xml


# Deploying the backend

Build the amp file:
mvn clean package -DskipTests=true

copy the amp file to the server:
sudo scp target/rm-database-alfresco-*.amp adminflmpde@oda-test.rm.dk:~/

stop the server:
sudo systemctl stop alfresco

Apply the amp:

sudo cp rm-database-alfresco-1.3.8.amp /appl/alfresco/amps
sudo cp alfresco-rm-cpr-1.0-SNAPSHOT.amp /appl/alfresco/amps
sudo cp support-tools-repo-1.1.0.0-amp.amp /appl/alfresco/amps


sudo chown alfresco:alfresco /appl/alfresco/amps/rm-database-alfresco-1.3.8.amp
sudo chmod 775 /appl/alfresco/amps/rm-database-alfresco-1.3.8.amp

sudo chown alfresco:alfresco /appl/alfresco/amps/alfresco-rm-cpr-1.0-SNAPSHOT.amp
sudo chmod 775 /appl/alfresco/amps/alfresco-rm-cpr-1.0-SNAPSHOT.amp

sudo chown alfresco:alfresco /appl/alfresco/amps/support-tools-repo-1.1.0.0-amp.amp
sudo chmod 775 /appl/alfresco/amps/support-tools-repo-1.1.0.0-amp.amp


cd /appl/alfresco/bin/
sudo ./apply_amps.sh* -force
sudo chmod 775 /appl/alfresco/tomcat/webapps/alfresco.war
sudo chown alfresco:alfresco /appl/alfresco/tomcat/webapps/alfresco.war

sudo chmod 775 /appl/alfresco/tomcat/webapps/share.war
sudo chown alfresco:alfresco /appl/alfresco/tomcat/webapps/share.war

start the server:
sudo systemctl start alfresco


# Deploying the frontend

The application requires node v11.15.0 and the npm module gulp. Build the application:

gulp build

Now zip the folder and copy it to the server:

tar -zcvf ret.zip alfresco-rm-database-alfresco-ui
scp ret.zip adminflmpde@oda-test.rm.dk:~

To apply the frontend changes:

cp ret.zip tmp/.

cd tmp
tar xvf ret.zip

cd /var/www/html

sudo rm -rf retspsyk

sudo mv /home/onerm.dk/adminflmpde/tmp/alfresco-rm-database-alfresco-ui retspsyk

sudo cp /var/www/html/discovery.xml /var/www/html/retspsyk/discovery.xml

sudo chown alfresco:alfresco -R retspsyk

# edit online

Libreoffice online are deploying here lool.rm.dk

# repositories

https://github.com/magenta-aps/alfresco-rm-database-alfresco-ui

https://github.com/magenta-aps/alfresco-rm-database-alfresco
