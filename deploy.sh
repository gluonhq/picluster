#!/bin/bash

rm -rf deploy/IotMonitorServer/*
rm -rf deploy/DisplayApp/*
rm -rf deploy/PiMonitor/*
rm -rf deploy/Server/*

./gradlew IoTMonitorServer:clean IoTMonitorServer:jlink

cp IoTMonitorServer/src/script/runiotserver.sh IoTMonitorServer/build/image/iotserver-linux
mv IoTMonitorServer/build/image/iotserver-linux iotmonitorserver-linux
zip -r deploy/IotMonitorServer/iotmonitorserver-linux.zip iotmonitorserver-linux
rm -rf iotmonitorserver-linux

cp IoTMonitorServer/src/script/runiotserver.sh IoTMonitorServer/build/image/iotserver-mac
mv IoTMonitorServer/build/image/iotserver-mac iotmonitorserver-mac
zip -r deploy/IotMonitorServer/iotmonitorserver-mac.zip iotmonitorserver-mac
rm -rf iotmonitorserver-mac

cp IoTMonitorServer/src/script/runiotserver.sh IoTMonitorServer/build/image/iotserver-win
mv IoTMonitorServer/build/image/iotserver-win iotmonitorserver-win
zip -r deploy/IotMonitorServer/iotmonitorserver-win.zip iotmonitorserver-win
rm -rf iotmonitorserver-win

./gradlew DisplayApp:clean DisplayApp:jlink

cp DisplayApp/src/script/rundisplayapp.sh DisplayApp/build/image/display-linux
mv DisplayApp/build/image/display-linux displayapp-linux
zip -r deploy/DisplayApp/displayapp-linux.zip displayapp-linux
rm -rf displayapp-linux

cp DisplayApp/src/script/rundisplayapp.sh DisplayApp/build/image/display-mac
mv DisplayApp/build/image/display-mac displayapp-mac
zip -r deploy/DisplayApp/displayapp-mac.zip displayapp-mac
rm -rf displayapp-mac

cp DisplayApp/src/script/rundisplayapp.sh DisplayApp/build/image/display-win
mv DisplayApp/build/image/display-win displayapp-win
zip -r deploy/DisplayApp/displayapp-win.zip displayapp-win
rm -rf displayapp-win

./gradlew IoTMonitorClient:clean IoTMonitorClient:build
./gradlew IoTWorker:clean IoTWorker:build

mkdir -p pimonitorbundle/class
cp -r IoTMonitorClient/build/classes/java/main/com pimonitorbundle/class
cp -r IoTWorker/build/classes/java/main/com pimonitorbundle/class
sed -e "s/IP_MON_SERVER/$1/g" -e "s/IP_SERVER/$2/g" -e "s/IP_DISPLAY/$3/g" IoTMonitorClient/src/scripts/startclientmonitor.sh > pimonitorbundle/startclientmonitor.sh
chmod +x pimonitorbundle/startclientmonitor.sh
zip -r deploy/PiMonitor/pimonitorbundle.zip pimonitorbundle
rm -rf pimonitorbundle

./gradlew Server:clean Server:build

mkdir -p serverbundle
cp -r Server/build/libs/Server.jar serverbundle
sed -e "s/DB_USER/$4/g" -e "s/DB_PASS/$5/g" Server/src/scripts/startserver.sh > serverbundle/startserver.sh
chmod +x serverbundle/startserver.sh
zip -r deploy/Server/serverbundle.zip serverbundle
rm -rf serverbundle

