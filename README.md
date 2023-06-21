Before starting the application locally, you need to start the database
`cd dependency-stub && docker-compose up -d`

build fat jar with command `./mvnw package` on mac, linux or `.\mvnw.cmd package` on windows

start fat jar with command `java -jar ./target/dictionary-api.jar`

development
in order to generate open-api class you need to build fat jar first - this way classes will be generated
and project will compile correctly