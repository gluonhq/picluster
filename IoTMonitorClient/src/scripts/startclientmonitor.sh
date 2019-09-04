#!/bin/bash
java -XX:-UsePerfData -cp class com.gluonhq.iotmonitor.client.MainMonitor IP_MON_SERVER
java -XX:-UsePerfData -cp class com.gluonhq.iotmonitor.iotworker.MainWorker IP_SERVER IP_DISPLAY
