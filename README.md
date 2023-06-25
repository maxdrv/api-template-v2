# Start application

## Local start
### Requirements:
Docker has to be installed
### Start
1. Before starting the application locally, you need to start the database
`cd dependency-stub && docker-compose up -d`

2. Build fat jar with command `./mvnw package` on mac, linux or `.\mvnw.cmd package` on windows

3. start fat jar with command `java -jar ./target/template.jar`


## Server start
1. Положить application.properties с конфигом сервера в папку, откуда будет выполнен запуск
2. Запускаем `java -jar ./template.jar`