package com.example.eloisedietz.myapplication.backend;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.example.eloisedietz.myapplication.backend.OfyService.ofy;
import static jdk.nashorn.internal.runtime.regexp.joni.Config.log;

/*
This class deletes an entry from the datastore
 */

public class DeleteServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        EntryDatastore datastore = new EntryDatastore();
        String id = request.getParameter(Entry.ID);
        String phoneid = request.getParameter(Entry.PHONE_ID);
        boolean wasDeleted = datastore.delete(id, phoneid);

        Logger mLogger = Logger.getLogger(Entry.class.getName());

        String regId = request.getParameter(Entry.REG_ID);
        mLogger.log(Level.INFO, "Regid is " + regId);

        RegistrationRecord regRecord = ofy().load().type(RegistrationRecord.class).filter("regId", regId).first().now();
        if(regRecord != null) {
            MessagingEndpoint messagingEndpoint = new MessagingEndpoint();
            if(wasDeleted)
                messagingEndpoint.sendMessage("Deleted rowID " + id + " phone id " + phoneid, regRecord);
            else
                messagingEndpoint.sendMessage("Failed to delete", regRecord);
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

