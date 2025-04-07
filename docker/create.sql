create user 'myuser'@'%' identified with mysql_native_password by 'password'; -- Crear usuari
create user 'myuser'@'192.168.65.1' identified with mysql_native_password by 'password'; -- Crear usuari
grant all privileges on *.* to 'myuser'@'%' with grant option; -- Dar todos los privilegios en la BD al nuevo usuario creado
grant all privileges on *.* to 'myuser'@'192.168.65.1' with grant option; -- Dar todos los privilegios en la BD al nuevo usuario creado
flush privileges; -- Aplicar los cambios