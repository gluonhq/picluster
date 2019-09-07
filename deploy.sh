#!/bin/bash

rm -rf deploy/IotMonitorServer/*
rm -rf deploy/DisplayApp/*
rm -rf deploy/PiMonitor/*
rm -rf deploy/IoTWorker/*
rm -rf deploy/Server/*

echo "Running IoTMonitorServer::jlink..."
./gradlew IoTMonitorServer:clean IoTMonitorServer:jlink

cp IoTMonitorServer/src/scripts/runiotserver.sh IoTMonitorServer/build/image/iotserver-linux/runiotserver.sh
mv IoTMonitorServer/build/image/iotserver-linux iotmonitorserver-linux
zip -r deploy/IotMonitorServer/iotmonitorserver-linux.zip iotmonitorserver-linux
rm -rf iotmonitorserver-linux

cp IoTMonitorServer/src/scripts/runiotserver.sh IoTMonitorServer/build/image/iotserver-mac/runiotserver.sh
mv IoTMonitorServer/build/image/iotserver-mac iotmonitorserver-mac
zip -r deploy/IotMonitorServer/iotmonitorserver-mac.zip iotmonitorserver-mac
rm -rf iotmonitorserver-mac

cp IoTMonitorServer/src/scripts/runiotserver.bat IoTMonitorServer/build/image/iotserver-win/runiotserver.bat
mv IoTMonitorServer/build/image/iotserver-win iotmonitorserver-win
zip -r deploy/IotMonitorServer/iotmonitorserver-win.zip iotmonitorserver-win
rm -rf iotmonitorserver-win

echo "Running DisplayApp::jlink..."
./gradlew DisplayApp:clean DisplayApp:jlink

cp DisplayApp/src/scripts/rundisplayapp.sh DisplayApp/build/image/display-linux/rundisplayapp.sh
mv DisplayApp/build/image/display-linux displayapp-linux
zip -r deploy/DisplayApp/displayapp-linux.zip displayapp-linux
rm -rf displayapp-linux

cp DisplayApp/src/scripts/rundisplayapp.sh DisplayApp/build/image/display-mac/rundisplayapp.sh
mv DisplayApp/build/image/display-mac displayapp-mac
zip -r deploy/DisplayApp/displayapp-mac.zip displayapp-mac
rm -rf displayapp-mac

cp DisplayApp/src/scripts/rundisplayapp.bat DisplayApp/build/image/display-win/rundisplayapp.bat
mv DisplayApp/build/image/display-win displayapp-win
zip -r deploy/DisplayApp/displayapp-win.zip displayapp-win
rm -rf displayapp-win

echo "Running IoTMonitorClient::build..."
./gradlew IoTMonitorClient:clean IoTMonitorClient:build
mkdir -p pimonitorclientbundle/class
sed "s/NFS_SHARED/$6/g" IoTMonitorClient/src/scripts/pimonitor.service > deploy/PiMonitor/pimonitor.service
cp -r IoTMonitorClient/build/classes/java/main/com pimonitorclientbundle/class
sed -e "s/IP_MON_SERVER/$1/g" -e "s#NFS_SHARED#$6#g" IoTMonitorClient/src/scripts/startclientmonitor.sh > pimonitorclientbundle/startclientmonitor.sh
chmod +x pimonitorclientbundle/startclientmonitor.sh
zip -r deploy/PiMonitor/pimonitorclientbundle.zip pimonitorclientbundle
rm -rf pimonitorclientbundle


echo "Running IoTWorker::build..."
./gradlew IoTWorker:clean IoTWorker:build

mkdir -p iotworkerbundle/class
sed "s#NFS_SHARED#$6#g" IotWorker/src/scripts/iotworker.service > deploy/IoTWorker/iotworker.service
cp -r IoTWorker/build/classes/java/main/com iotworkerbundle/class
sed -e "s/IP_SERVER/$2/g" -e "s/IP_DISPLAY/$3/g" -e "s#NFS_SHARED#$6#g" IoTWorker/src/scripts/startiotworker.sh > iotworkerbundle/startiotworker.sh
chmod +x iotworkerbundle/startiotworker.sh
zip -r deploy/IoTWorker/iotworkerbundle.zip iotworkerbundle
rm -rf iotworkerbundle

echo "Running Server::build..."
./gradlew Server:clean Server:build

mkdir -p serverbundle
cp Server/build/libs/Server.jar serverbundle
sed -e "s/DB_USER/$4/g" -e "s/DB_PASS/$5/g" Server/src/scripts/startserver.sh > serverbundle/startserver.sh
chmod +x serverbundle/startserver.sh
unzip -o Server/files/wallet.zip -d serverbundle/
zip -r deploy/Server/serverbundle.zip serverbundle
rm -rf serverbundle

echo "Deploy done!"

