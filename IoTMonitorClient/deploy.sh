#!/bin/bash
rm -rf pimonitorclientbundle
mkdir -p pimonitorclientbundle/class
cp -r build/classes/java/main/com pimonitorclientbundle/class
cp src/scripts/startclientmonitor.sh pimonitorclientbundle
