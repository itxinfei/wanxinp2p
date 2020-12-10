### Docker 安装软件
`
docker run -d -p 3307:3306 --privileged=true -v /docker/mysql/conf/my.cnf:/etc/my.cnf -v /docker/mysql/data:/var/lib/mysql -e MYSQL_ROOT_PASSWORD=root --name mysql57 mysql:5.7 --character-set-server=utf8mb4 --collation-server=utf8mb4_general_ci
`