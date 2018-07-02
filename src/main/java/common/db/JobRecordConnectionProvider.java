package common.db;

import edu.gsu.dmlab.isd.monitor.IJobRecordConnection;
import edu.gsu.dmlab.isd.monitor.PostgresJobRecordConnection;

/**
 * Class that connects to the postgre database
 * @author - ahmetkucuk
 * @author - kqian5
 */
public class JobRecordConnectionProvider {
    private static JobRecordConnectionProvider instance;
    private PostgresJobRecordConnection monitorConnection;

    /**
     * Constructor for a JobRecordProvider object that
     * establishes a connection to the postgre database
     * using configurations from DBPrefs
     */
    private JobRecordConnectionProvider() {
        monitorConnection = new PostgresJobRecordConnection(DBPrefs.getDataSource());
    }

    /**
     * Returns the connection object from a new instance of 
     * a JobRecordConnectionProvider object
     * @return - a PostgresJobRecordConnection object from 
     * a new instance of a JobRecordConnectionProvider object
     */
    public static IJobRecordConnection connection() {
        if (instance == null) {
            instance = new JobRecordConnectionProvider();
        }
        return instance.monitorConnection;
    }
}
