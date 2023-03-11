sudo docker network create net
sudo docker run -d -v /home/ubuntu/mysql:/var/lib/mysql -v "$(pwd)"/mysql:/docker-entrypoint-initdb.d -e MYSQL_RANDOM_ROOT_PASSWORD=yes --network net --name mysql mysql
sudo docker run -d -p 8080:30000 --network net --name service service:latest