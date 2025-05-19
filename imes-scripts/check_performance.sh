#!/bin/bash

# Print server performance and health details
echo "======= Server Health Information ======="
echo "CPU Load Averages (1, 5, 15 mins):"
uptime | awk -F'load average:' '{print $2}'
echo "Memory Usage:"
free -h
echo "Disk Usage:"
df -h /
echo "========================================="
echo ""

# Print the header for the process list
echo "PID   ELAPSED   %MEM   %CPU   COMMAND"

# List iMES service processes with details
ps -eo pid,etime,%mem,%cpu,cmd --sort=-%mem | grep 'imes-services' | grep -v grep
