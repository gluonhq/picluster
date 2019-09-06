#!/bin/bash
java -XX:-UsePerfData -cp /mnt/nfs/var/nfsshare/pimonitorclientbundle/class com.gluonhq.iotmonitor.client.MainMonitor IP_MON_SERVER &
java -XX:-UsePerfData -cp /mnt/nfs/var/nfsshare/pimonitorclientbundle/class com.gluonhq.picluster.iotworker.MainWorker IP_SERVER IP_DISPLAY
