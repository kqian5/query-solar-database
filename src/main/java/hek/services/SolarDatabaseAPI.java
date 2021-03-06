package hek.services;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import hek.core.TableCreator;
import hek.core.DBConnection;
import hek.core.EventJsonDownloader;
import hek.core.InsertStatementGenerator;
import hek.models.Event;
import hek.models.EventType;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by ahmetkucuk on 28/12/15.
 */
public class SolarDatabaseAPI {

    private static final int numberOfElementInEachQuery = 200;
    private Queue<EventJsonDownloader> downloadQueue = new PriorityQueue<>();


    public void pullEvents(String startDate, String endDate, int page) {

        String eventsToBeIncluded = StringUtils.join(EventType.getAsList().stream().map(e -> e.toQualifiedString()).collect(Collectors.toList()), ",");
        EventJsonDownloader eventJsonDownloader = new EventJsonDownloader(eventsToBeIncluded, startDate, endDate, numberOfElementInEachQuery, page);

        downloadQueue();
        try {
            downloadAndInsert(eventJsonDownloader);
        } catch (Exception e) {
            System.out.println("Couldn't download. Adding Queue. Start: " + startDate + " End: " + endDate);
            e.printStackTrace();
            eventJsonDownloader.reset();
            downloadQueue.add(eventJsonDownloader);
        }
    }

    private void downloadQueue() {

        EventJsonDownloader downloader = null;
        while ((downloader = downloadQueue.peek()) != null) {
            try {
                downloadAndInsert(downloader);
            } catch (Exception e) {
                System.out.println("Couldn't download again.");
                downloader.reset();
                break;
            }
            downloadQueue.poll();
        }
    }

    public void downloadAndInsert(EventJsonDownloader eventJsonDownloader) throws Exception {
        JsonArray array;
        Set<String> insertedEvents = new HashSet<>();
        DBConnection connection = DBConnection.getNewConnection();
        while ((array = eventJsonDownloader.next()) != null) {
            for (JsonElement j : array) {
                Event e = new Event(j.getAsJsonObject());
                String kb = e.getAttr("kb_archivid");
                if (!insertedEvents.contains(kb)) {
                    new InsertStatementGenerator(e).getInsertQueries().forEach(q -> connection.executeCommand(q));
                    insertedEvents.add(kb);
                }
            }
        }
        connection.closeConnection();
    }

    public void createDatabaseSchema() {

        DBConnection connection = DBConnection.getNewConnection();
        if(!connection.exists("ar")) {
            new TableCreator(connection).createTables();
        } else {
            System.out.println("Ar Table exists, skipping table creation");
        }
    }

//    public void addAdditionalFunctions() {
//
//        String[] listOfFunctions = new String[]{"query/temporal_filter.sql"};
//        for(String fileName : listOfFunctions) {
//            DBConnection.getInstance().executeFromFile(FileManager.getInstance().getPath(fileName));
//        }
//    }
}
