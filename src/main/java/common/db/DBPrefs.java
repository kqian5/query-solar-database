package common.db;

import com.rabbitmq.client.ConnectionFactory;
import org.apache.commons.dbcp2.BasicDataSource;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

/**
 * Created by ahmetkucuk on 28/12/15.
 */
public class DBPrefs {

//    public static final String DB_HOST = "postgres-exposed";
//    public static final String DB_NAME = "isd";
//    public static String DB_USERNAME = "postgres";
//    public static String DB_USER_PASSWORD = "r3mot3p8sswo4d";

    final static String DEFAULT_RABBIT_MQ_HOST = "rabbitmq";
    final static int DEFAULT_RABBIT_MQ_PORT = 5672;

    public static final int DB_PORT = 5432;


    public static BasicDataSource getDataSource() {

        Map<String, String> env = System.getenv();

        BasicDataSource dbPoolSourc = new BasicDataSource();
		dbPoolSourc.setSoftMinEvictableIdleTimeMillis(6500);
		dbPoolSourc.setDefaultAutoCommit(true);
		dbPoolSourc.setPoolPreparedStatements(false);
        dbPoolSourc.setDefaultQueryTimeout(60);
        dbPoolSourc.setMinIdle(1);
        dbPoolSourc.setMaxIdle(10);
        dbPoolSourc.setMaxTotal(100);
        dbPoolSourc.setUsername(env.get("POSTGRES_USER"));
        dbPoolSourc.setPassword(env.get("POSTGRES_PASSWORD"));
        dbPoolSourc.setValidationQuery("SELECT 1;");
        dbPoolSourc.setDriverClassName("org.postgresql.Driver");
        dbPoolSourc.setUrl("jdbc:postgresql://" + env.get("POSTGRES_DB_HOST") + "/" + env.get("POSTGRES_DB"));
        return dbPoolSourc;
    }

    public static Map<String, String> getTestEnv(){
        Map<String, String> env = new HashMap<>();
        env.put("POSTGRES_USER", "postgres");
        env.put("POSTGRES_PASSWORD", "");
        env.put("POSTGRES_DB", "postgres");
        env.put("POSTGRES_DB_HOST", "localhost");
        return env;
    }

    public static void pause() {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void waitUntilConnected() {

        boolean connected = false;
        Connection ex = null;
        while(!connected) {
            try {
                DataSource dsourc = getDataSource();
                try {
                    ex = dsourc.getConnection();
                    ex.setAutoCommit(true);
                    Statement ex1 = ex.createStatement();
                    connected = ex1.execute("SELECT 1;");
                } catch (SQLException var7) {
                    System.out.println("Failed to Connect. Will retry");
                } finally {
                    if(ex != null) {
                        ex.close();
                    }

                }
            } catch (SQLException var9) {
                System.out.println("Connection error 1");
            }
            pause();
            System.out.println("Waiting for DB Connection");
        }
        try {
            ex.close();
        } catch (SQLException e) {
            System.out.println("Connection error 2");
        }
    }

    public static void waitUntilRabbitMqReady(String host, int port) {
        System.out.println("Sending Task");
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(host);
        factory.setPort(port);

        boolean connected = false;
        while(!connected) {
            try {
                com.rabbitmq.client.Connection e = factory.newConnection();
                e.close();
                connected = true;
            } catch (TimeoutException | IOException var5) {
                var5.printStackTrace();
            }

            pause();
            System.out.println("Waiting for Rabbit MQ Connection");
        }

    }

    public static BasicDataSource getImageDataSource() {

        Map<String, String> env = System.getenv();

        BasicDataSource dbPoolSourc = new BasicDataSource();
        dbPoolSourc.setSoftMinEvictableIdleTimeMillis(6500);
        dbPoolSourc.setDefaultAutoCommit(true);
        dbPoolSourc.setPoolPreparedStatements(false);
        dbPoolSourc.setDefaultQueryTimeout(60);
        dbPoolSourc.setMinIdle(2);
        dbPoolSourc.setMaxIdle(10);
        dbPoolSourc.setMaxTotal(100);
        dbPoolSourc.setUsername(env.get("MYSQL_USER"));
        dbPoolSourc.setPassword(env.get("MYSQL_PASSWORD"));
        dbPoolSourc.setDriverClassName("com.mysql.jdbc.Driver");
        dbPoolSourc.setUrl("jdbc:mysql://" + env.get("MYSQL_DB_HOST") + "/" + env.get("MYSQL_DB"));
        return dbPoolSourc;
    }

    public static void waitDefaultDBConnections() {
        DBPrefs.waitUntilConnected();
        DBPrefs.waitUntilRabbitMqReady(DEFAULT_RABBIT_MQ_HOST, DEFAULT_RABBIT_MQ_PORT);
    }

}

