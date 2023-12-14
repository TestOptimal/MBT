
# These files/folders not needed
#del target\my-rest-2.0.0.war.original
#del target\my-rest-2.0.0

# need following folders/files:
#  - target
#  - .mvn
#  - h2db
#  - mvnw
#  - mvnw.cmd
#  - pom.xml

# application.properties in target/classes/

#starting up the app, jvm args can be added right after 'mvnw'
mvnw spring-boot:run