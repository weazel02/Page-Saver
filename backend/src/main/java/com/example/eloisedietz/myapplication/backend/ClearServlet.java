package com.example.eloisedietz.myapplication.backend;

import com.example.eloisedietz.myapplication.backend.EntryDatastore;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by eloisedietz on 2/21/17.
 */

public class ClearServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    public void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException, ServletException {
        EntryDatastore datastore = new EntryDatastore();
        datastore.clear();
    }

    public void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException, ServletException {
        doPost(req, resp);
    }
}
