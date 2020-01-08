# 256-day

## how to start 
to start service inside a docker container
```scala
gradle startService
```

to start service without docker
```scala
gradle startServiceWithoutDocker
```

## how to request microservice

### days between next programmer's day and current date
```
curl http://localhost:8080?currentDate=12092017
{"errorCode":200,"dataMessage":"1"}%

curl http://localhost:8080?currentDate=10092017
{"errorCode":200,"dataMessage":"3"}%
```

### 256'th day of the year

```
curl http://localhost:8080?year=2017
{"errorCode":200,"dataMessage":"13/09/17"}%

curl http://localhost:8080?year=2020
{"errorCode":200,"dataMessage":"12/09/20"}%
```
