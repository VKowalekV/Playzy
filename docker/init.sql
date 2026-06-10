CREATE USER playzy_api WITH PASSWORD 'naprawdeTajneHasloAPi';

GRANT ALL PRIVILEGES ON DATABASE playzy TO playzy_api;

\c playzy
GRANT ALL ON SCHEMA public TO playzy_api;