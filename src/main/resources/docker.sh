#linux version
docker run --network host -p 3306:3306 --name test-mysql -v /home/barcz/mysql:/var/lib/mysql -e MYSQL_ROOT_PASSWORD=password -d mysql