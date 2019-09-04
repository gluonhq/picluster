#!/bin/bash
echo "we assume there is a build/image dir with all that is needed"
echo "created via cd .. ./gradlew IoTMonitorServer:jlink"
cp src/script/runiotserver.sh build/image
cd build
mv image iotmonitorserver
zip -r ../iotmonitorserver.zip iotmonitorserver
