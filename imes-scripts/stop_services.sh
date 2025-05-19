#!/bin/bash

# Stop all microservices by reading the PID file
if [ -f /home/senthil/iMES/microservice_pids.log ]; then
  echo "Stopping all microservices..."

  # Read the PIDs from the log file and kill each process
  while IFS= read -r line; do
    PID=$(echo $line | awk '{print $NF}')
    echo "Stopping process with PID $PID"
    kill -9 $PID
  done < "/home/senthil/iMES/microservice_pids.log"

  # Clean up the PID log file
  rm /home/senthil/iMES/microservice_pids.log
  echo "Removed microservice PID log file."

  # Remove all log files in the logs directory
  LOG_DIR="/home/senthil/iMES/logs"
  if [ -d "$LOG_DIR" ]; then
    echo "Removing all service logs..."
    rm -rf "$LOG_DIR"/*
    echo "All logs removed from $LOG_DIR."
  else
    echo "Log directory does not exist."
  fi

  echo "All microservices stopped and logs cleaned up."
else
  echo "No microservices are currently running."
fi

