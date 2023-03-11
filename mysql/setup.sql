create user 'domcretan'@'%' identified by 'admin_root_kyr1';
grant all on *.* to 'domcretan'@'%' with grant option;
create database books;
connect books;
create table book(isbn VARCHAR(15) PRIMARY KEY, authors VARCHAR(100) NOT NULL, title VARCHAR(50) NOT NULL, 
publisher VARCHAR(20) NOT NULL, date VARCHAR(20), category VARCHAR(20), summary VARCHAR(100), language VARCHAR(15));
create table `user`(login VARCHAR(20), password VARCHAR(20));
insert into table `user` values('employee','employee1');
exit