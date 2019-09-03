package com.gluonhq.picluster.server;

import oracle.ucp.jdbc.PoolDataSource;
import oracle.ucp.jdbc.PoolDataSourceFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

public class AutonomousDatabaseWriter {

    private final Logger logger = Logger.getLogger(AutonomousDatabaseWriter.class.getName());

    private final Executor executor = Executors.newFixedThreadPool(16);

    final static String DB_URL = "jdbc:oracle:thin:@db201909030638_tp?TNS_ADMIN=" + System.getProperty("user.dir") + "/wallet/";
    final static String CONN_FACTORY_CLASS_NAME = "oracle.jdbc.pool.OracleDataSource";

    private boolean ready = false;
    private PoolDataSource pds;

    private final String username;
    private final String password;

    public AutonomousDatabaseWriter(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public void setupConnectionPool() throws SQLException {
        // Get the PoolDataSource for UCP
        this.pds = PoolDataSourceFactory.getPoolDataSource();

        logger.info("Connecting to Oracle Autonomouse Database at " + DB_URL);

        // Set the connection factory first before all other properties
        pds.setConnectionFactoryClassName(CONN_FACTORY_CLASS_NAME);
        pds.setURL(DB_URL);
        pds.setUser(username);
        pds.setPassword(password);
        pds.setConnectionPoolName("JDBC_UCP_POOL");

        // Default is 0. Set the initial number of connections to be created
        // when UCP is started.
        pds.setInitialPoolSize(5);

        // Default is 0. Set the minimum number of connections
        // that is maintained by UCP at runtime.
        pds.setMinPoolSize(5);

        // Default is Integer.MAX_VALUE (2147483647). Set the maximum number of
        // connections allowed on the connection pool.
        pds.setMaxPoolSize(20);

        // Default is 30secs. Set the frequency in seconds to enforce the timeout
        // properties. Applies to inactiveConnectionTimeout(int secs),
        // AbandonedConnectionTimeout(secs)& TimeToLiveConnectionTimeout(int secs).
        // Range of valid values is 0 to Integer.MAX_VALUE.
        pds.setTimeoutCheckInterval(5);

        // Default is 0. Set the maximum time, in seconds, that a
        // connection remains available in the connection pool.
        pds.setInactiveConnectionTimeout(10);

        createTables();

        this.ready = true;
    }

    private void createTables() throws SQLException {
        logger.info("Creating tables in the Oracle Autonomous Database...");
        try (Connection conn = pds.getConnection(); Statement statement = conn.createStatement()) {
            conn.setAutoCommit(false);

            statement.executeUpdate("create table TASKOUTPUT(TASKID VARCHAR2 (36),"
                    + "URL NVARCHAR2 (4000),"
                    + "ANSWER VARCHAR2 (255)"
                    + ")");

            conn.commit();
        } catch (SQLException sql) {
            if (sql.getErrorCode() == 955) {
                logger.info("Table TASKOUTPUT already exists.");
            } else {
                throw sql;
            }
        }
        logger.info("Creating tables completed!");
    }

    public void logClientRequestAndAnswer(String taskId, String url, String answer) {
        logger.fine("Starting task to add record for task " + taskId + " with answer " + answer);
        if (ready) {
            executor.execute(() -> {
                logger.fine("Adding record for task " + taskId + " with answer " + answer + "...");
                try (Connection conn = pds.getConnection(); Statement statement = conn.createStatement()) {
                    conn.setAutoCommit(false);

                    statement.executeUpdate("insert into TASKOUTPUT values ('" + taskId + "', '" + url + "', '" + answer + "')");

                    conn.commit();
                } catch (SQLException e) {
                    logger.severe("SQLException occurred while inserting task answer: " + e.getMessage());
                }
                logger.info("Added record for task " + taskId + " with answer " + answer + "!");
            });
        }
    }
}
