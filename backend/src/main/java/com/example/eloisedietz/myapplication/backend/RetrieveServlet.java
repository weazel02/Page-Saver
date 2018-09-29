package com.example.eloisedietz.myapplication.backend;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.example.eloisedietz.myapplication.backend.OfyService.ofy;

/**
 * Created by kelle on 3/4/2017.
 */

public class RetrieveServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    Logger logger = Logger.getLogger(Entry.class.getName());

    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        Logger logger = Logger.getLogger(Entry.class.getName());
        logger.info("entered");

        String regId = request.getParameter(Entry.REG_ID);
        Logger mLogger = Logger.getLogger(Entry.class.getName());
        mLogger.log(Level.INFO, "Regid is " + regId);
        RegistrationRecord regRecord = ofy().load().type(RegistrationRecord.class).
                filter("regId", regId).first().now();

        EntryDatastore entryDatastore = new EntryDatastore();
        ArrayList<Entry> entries = entryDatastore.query();

        MessagingEndpoint messenger = new MessagingEndpoint();

        String message = "timePages ";

        for (Entry entry : entries) {
            List<Entry.StartEndPages> pages = entry.getPageRanges();
            List<Entry.StartEndTimes> times = entry.getTimeRanges();

            logger.info("got info");

            // Format of message is:
            // timePages info1 info2 info3... - delimited by space
            // Format for each info is:
            // hourStarted,monthStarted,durationInHours,numPages, - delimited by ,

            // Only send info if they all match up
            if (pages.size() == times.size()) {

                logger.info("sizes were equal");

                for (int i = 0; i < times.size(); i++) {
                    Long startTime = times.get(i).startTime;
                    Long endTime = times.get(i).endTime;
                    Calendar cal = Calendar.getInstance();
                    cal.setTimeInMillis(startTime);
                    message = message + cal.get(Calendar.HOUR_OF_DAY) + ",";
                    message = message + cal.get(Calendar.MONTH) + ",";

                    // Difference in milliseconds - converted to hours
                    Long timeDiff = endTime - startTime;
                    int minutesRead = (int) (timeDiff / 1000 / 60 / 60);

                    message = message + minutesRead + ",";

                    int diff = pages.get(i).endPage - pages.get(i).startPage;

                    message = message + diff + " ";

                    logger.info(message);
                }
            }

            messenger.sendMessage(message, regRecord);

        }
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        doPost(request, response);
    }
}
