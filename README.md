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
1. Положить application.properties с конфигом бд доступной на сервере в папку, откуда будет выполнен запуск
2. Запускаем `java -jar ./template.jar`


## Copy files to server
scp -i ~/.ssh/public_key application.properties template.jar username@{host}:/home/username/
scp -i ~/.ssh/cloud1 HelloVirtualMachine.class maxdrv@158.160.34.208:/home/maxdrv/HelloVirtualMachine1.class
scp -i ~/.ssh/cloud1 target/template.jar maxdrv@158.160.114.165:/home/maxdrv/template-slru-2.jar

## Start in a background
java -jar template.jar > out.log 2>&1 &

### Find and kill java background process
ps aux | grep java
kill -15 5101
kill -9 5101

### curl metrics
curl -X GET --location "http://localhost:8081/actuator/prometheus"