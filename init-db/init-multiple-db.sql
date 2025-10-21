-- ===========================================================
-- Inicialización múltiple para entorno Docker Compose
-- Crea las bases y usuarios con permisos adecuados
-- ===========================================================
-- Bases de datos
DROP DATABASE IF EXISTS accounts;
DROP DATABASE IF EXISTS movements;

-- Bases de datos
CREATE DATABASE IF NOT EXISTS accounts;
CREATE DATABASE IF NOT EXISTS movements;

-- ===========================================================
-- LIMPIEZA DE USUARIOS EXISTENTES
-- ===========================================================
DROP USER IF EXISTS 'cobre'@'localhost';
DROP USER IF EXISTS 'cobre'@'%';
DROP USER IF EXISTS 'cobre'@'192.168.65.1';

-- ===========================================================
-- CREACIÓN DE USUARIOS
-- ===========================================================
CREATE USER 'cobre'@'%' IDENTIFIED BY 'cobre';
CREATE USER 'cobre'@'192.168.65.1' IDENTIFIED BY 'cobre';

-- ===========================================================
-- ASIGNACIÓN DE PRIVILEGIOS
-- ===========================================================
GRANT ALL PRIVILEGES ON *.* TO 'cobre'@'%' WITH GRANT OPTION;
GRANT ALL PRIVILEGES ON *.* TO 'cobre'@'192.168.65.1' WITH GRANT OPTION;
GRANT ALL PRIVILEGES ON accounts.* TO 'cobre'@'%' WITH GRANT OPTION;
GRANT ALL PRIVILEGES ON movements.* TO 'cobre'@'%' WITH GRANT OPTION;
GRANT ALL PRIVILEGES ON accounts.* TO 'cobre'@'192.168.65.1' WITH GRANT OPTION;
GRANT ALL PRIVILEGES ON movements.* TO 'cobre'@'192.168.65.1' WITH GRANT OPTION;

-- Aplicar cambios
FLUSH PRIVILEGES;
