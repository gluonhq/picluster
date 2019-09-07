#!/bin/bash
java -XX:-UsePerfData -cp /mnt/nfsNFS_SHARED/pimonitorclientbundle/class com.gluonhq.iotmonitor.client.MainMonitor IP_MON_SERVER > /var/log/iotmonitor.log 2>&1
