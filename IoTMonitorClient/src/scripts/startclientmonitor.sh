#!/bin/bash
java -XX:-UsePerfData -cp class com.gluonhq.iotmonitor.client.MainMonitor $1
