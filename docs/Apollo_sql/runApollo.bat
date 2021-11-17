echo

set url="172.168.20.144:3306"
set username="root"
set password="root"

start "configService" java -Xms128m -Xmx128m -Dapollo_profile=github -Dspring.datasource.url=jdbc:mysql://%url%/apolloconfigdb?characterEncoding=utf8 -Dspring.datasource.username=%username% -Dspring.datasource.password=%password% -Dlogging.file=.\logs\apollo-configservice.log -jar .\apollo-configservice-1.3.0.jar
start "adminService" java -Xms128m -Xmx128m -Dapollo_profile=github -Dspring.datasource.url=jdbc:mysql://%url%/apolloconfigdb?characterEncoding=utf8 -Dspring.datasource.username=%username% -Dspring.datasource.password=%password% -Dlogging.file=.\logs\apollo-adminservice.log -jar .\apollo-adminservice-1.3.0.jar
start "ApolloPortal" java -Xms128m -Xmx128m -Dapollo_profile=github,auth -Ddev_meta=http://localhost:8080/ -Dserver.port=8070 -Dspring.datasource.url=jdbc:mysql://%url%/apolloportaldb?characterEncoding=utf8 -Dspring.datasource.username=%username% -Dspring.datasource.password=%password% -Dlogging.file=.\logs\apollo-portal.log -jar .\apollo-portal-1.3.0.jar
