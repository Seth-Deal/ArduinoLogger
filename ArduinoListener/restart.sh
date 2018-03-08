nohup /home/sethdeal/dev/arduino/projects/java/home/stop.sh > /dev/null 2>&1 &
echo "logger stopped"
nohup /home/sethdeal/dev/arduino/projects/java/home/start.sh 
#> /dev/null 2>&1 &
echo "logger started"
