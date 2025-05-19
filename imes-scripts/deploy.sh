#!/bin/bash

# JVM options
JVM_OPTS="-Xms512m -Xmx1g \
          -XX:+UseG1GC \
          -XX:MaxGCPauseMillis=150 \
          -XX:InitiatingHeapOccupancyPercent=55 \
          -XX:+ExitOnOutOfMemoryError \
          -Xlog:gc*:file=/var/log/gc.log:time,tags,uptime:filecount=5,filesize=10M"

# Environment variables
EUREKA_HOST="192.168.1.64"
DOCKER_KAFKA_HOST="192.168.1.64"
POSTGRES_HOST="192.168.1.64"
DOCKER_HOST_IP="192.168.1.64"
DOCKER_HOST_PORT="8686"
DOCKER_HOST_API_PORT="8080"

# Define the array of JAR file locations in the specific sequence you want
JAR_FILES=(
    "/home/senthil/iMES/imes-services/discovery-server/target/discovery-server-1.0-SNAPSHOT.jar"  # Discovery Service
    "/home/senthil/iMES/imes-services/api-gateway/target/api-gateway-1.0-SNAPSHOT.jar"            # API Gateway
    "/home/senthil/iMES/imes-services/integration-service/target/integration-service-1.0-SNAPSHOT.jar"  # Integration Service
    "/home/senthil/iMES/imes-services/core-service/target/core-service-1.0-SNAPSHOT.jar"          # Core Service
    "/home/senthil/iMES/imes-services/inventory-service/target/inventory-service-1.0-SNAPSHOT.jar"
    "/home/senthil/iMES/imes-services/oee-process-service/target/oee-process-service-1.0-SNAPSHOT.jar"
    "/home/senthil/iMES/imes-services/product-service/target/product-service-1.0-SNAPSHOT.jar"
    "/home/senthil/iMES/imes-services/productdefinition-service/target/productdefinition-service-1.0-SNAPSHOT.jar"
    "/home/senthil/iMES/imes-services/production-service/target/production-service-1.0-SNAPSHOT.jar"
    "/home/senthil/iMES/imes-services/qualitydefinition-service/target/qualitydefinition-service-1.0-SNAPSHOT.jar"
    "/home/senthil/iMES/imes-services/validation-service/target/validation-service-1.0-SNAPSHOT.jar"
)

# Define memory limits for each service (512 MB min, 1 GB max) and enable G1GC
MEMORY_LIMIT="-Xms512m -Xmx1g -XX:+UseG1GC"

# Function to check if the service is already running
is_service_running() {
  JAR_FILE=$1
  if pgrep -f "java -jar $JAR_FILE" > /dev/null; then
    return 0  # Service is running
  else
    return 1  # Service is not running
  fi
}

# Function to start each JAR file
start_microservices() {
  for JAR_FILE in "${JAR_FILES[@]}"; do
    SERVICE_NAME=$(basename "$JAR_FILE" .jar)
    LOG_FILE="/home/senthil/iMES/logs/$SERVICE_NAME.log"

    # Check if the service is already running
    if is_service_running "$JAR_FILE"; then
      echo "$SERVICE_NAME is already running. Skipping..."
      continue
    fi

    echo "Starting $SERVICE_NAME..."

    # Start the microservice with memory limits, G1GC, and all environment variables in the background
    nohup env EUREKA_HOST=$EUREKA_HOST DOCKER_KAFKA_HOST=$DOCKER_KAFKA_HOST POSTGRES_HOST=$POSTGRES_HOST \
    DOCKER_HOST_IP=$DOCKER_HOST_IP DOCKER_HOST_PORT=$DOCKER_HOST_PORT DOCKER_HOST_API_PORT=$DOCKER_HOST_API_PORT \
    java $MEMORY_LIMIT -XX:+ExitOnOutOfMemoryError -jar "$JAR_FILE" > "$LOG_FILE" 2>&1 &

    # Capture the PID and log it
    echo "$SERVICE_NAME started with PID $!" >> "/home/senthil/iMES/microservice_pids.log"

    # Wait for the service to start by checking its logs for the "Started" message
    wait_for_service "$SERVICE_NAME"
  done
}

# Function to check if the service has started by looking for "Started" in the log file
wait_for_service() {
  SERVICE_NAME=$1
  LOG_FILE="/home/senthil/iMES/logs/$SERVICE_NAME.log"

  echo "Waiting for $SERVICE_NAME to be ready..."

  # Continuously check the log for the "Started" message
  while ! grep -q "Started" "$LOG_FILE"; do
    sleep 5  # Wait for 5 seconds before checking again
  done

  echo "$SERVICE_NAME is up and running!"
}

# Create a logs directory if it doesn't exist
mkdir -p /home/senthil/iMES/logs

# Remove any previous PID log
rm -f /home/senthil/iMES/microservice_pids.log

# Start the microservices in the specified order
start_microservices

echo "All microservices started."

