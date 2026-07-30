-- Conectar no banco Oracle como SYSTEM (ex: sqlplus system/Mudar123@10.0.0.200:1521/XEPDB1)

-- Alterar a sessao para permitir a criacao de usuarios sem prefixo C## no Oracle 12c+
ALTER SESSION SET "_ORACLE_SCRIPT"=true;

-- Criar o usuario/schema para a aplicacao
CREATE USER meubolso IDENTIFIED BY "MeuBolso@2026";

-- Conceder os privilegios necessarios
GRANT CONNECT, RESOURCE, DBA TO meubolso;
GRANT CREATE SESSION TO meubolso;
GRANT CREATE TABLE TO meubolso;
GRANT CREATE SEQUENCE TO meubolso;
GRANT UNLIMITED TABLESPACE TO meubolso;

-- A partir daqui, o Spring Boot cuidara da criacao das tabelas automaticamente
-- gracas ao parametro spring.jpa.hibernate.ddl-auto=update
