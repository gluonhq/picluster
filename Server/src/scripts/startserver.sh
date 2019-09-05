#!/bin/bash
$JAVA_HOME/bin/java -Dfile.encoding=UTF-8 -Duser.country=US -Duser.language=en -Duser.variant -cp Server.jar:ojdbc8.jar:ucp.jar:oraclepki.jar:osdt_cert.jar:osdt_core.jar com.gluonhq.picluster.server.Main DB_USER DB_PASS
