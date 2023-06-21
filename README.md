# Start application

## Local start
### Requirements:
Docker has to be installed
### Start
1. Before starting the application locally, you need to start the database
`cd dependency-stub && docker-compose up -d`

2. Build fat jar with command `./mvnw package` on mac, linux or `.\mvnw.cmd package` on windows

3. start fat jar with command `java -jar ./target/template.jar`

# Development
## How to get generated classes?
1. Build fat jar with command `./mvnw package` on mac, linux or `.\mvnw.cmd package` on windows
2. After first step classes will be generated inside target folder