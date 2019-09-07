#!/bin/bash
java -XX:-UsePerfData -cp /mnt/nfsNFS_SHARED/iotworkerbundle/class com.gluonhq.picluster.iotworker.MainWorker IP_SERVER IP_DISPLAY > /var/log/iotworker.log 2>&1
