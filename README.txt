mvn clean package

jar tf target\app.jar | Select-String META-INF

docker compose down
docker compose up -d
docker compose up --build -d