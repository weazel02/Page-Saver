package com.example.eloisedietz.myapplication.backend;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.example.eloisedietz.myapplication.backend.OfyService.ofy;

/**
 * Created by Danielle on 3/2/17.
 */

public class UpdateServlet extends HttpServlet {
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        Entry entry = Entry.requestToEntry(request);

        EntryDatastore entryDatastore = new EntryDatastore();
        entryDatastore.updateEntry(entry);

        response.sendRedirect("/query.do");
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        doPost(request, response);
    }
}
