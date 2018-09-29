package com.example.eloisedietz.myapplication.backend;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.example.eloisedietz.myapplication.backend.OfyService.ofy;

/**
 * Created by eloisedietz on 2/24/17.
 */

public class AddServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        Entry entry = Entry.requestToEntry(request);

        EntryDatastore entryDatastore = new EntryDatastore();
        boolean wasAdded = entryDatastore.addEntry2Datastore(entry);



        String regId = request.getParameter(Entry.REG_ID);
        Logger mLogger = Logger.getLogger(Entry.class.getName());
        mLogger.log(Level.INFO, "Regid is " + regId);
        RegistrationRecord regRecord = ofy().load().type(RegistrationRecord.class).filter("regId", regId).first().now();

        if(regRecord != null) {
            MessagingEndpoint messagingEndpoint = new MessagingEndpoint();
            if(wasAdded)
                messagingEndpoint.sendMessage("Add successful", regRecord);
            else
                messagingEndpoint.sendMessage("Failed to add", regRecord);
        } else {
            mLogger.log(Level.INFO, "REG RECORD NULL");
        }



        response.sendRedirect("/query.do");
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        doPost(request, response);
    }
}
