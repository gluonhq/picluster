#!/bin/bash
$JAVA_HOME/bin/java -Dfile.encoding=UTF-8 -Duser.country=US -Duser.language=en -Duser.variant -cp Server.jar com.gluonhq.picluster.server.Main DB_USER DB_PASS
