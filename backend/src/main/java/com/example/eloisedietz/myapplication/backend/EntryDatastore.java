package com.example.eloisedietz.myapplication.backend;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by eloisedietz on 2/24/17.
 */

public class EntryDatastore {
    private Logger mLogger;
    private DatastoreService mDatastore;

    public EntryDatastore(){
        mLogger = Logger.getLogger(Entry.class.getName());
        mDatastore = DatastoreServiceFactory
                .getDatastoreService();
    }

    private static Key getParentKey() {
        return KeyFactory.createKey(Entry.PARENT_KIND,
                Entry.PARENT_IDENTIFIER);
    }

    /*
This method clears all of the entries in the datastore
 */
    public  void clear(){
        Query query = new Query(Entry.ENTITY_NAME);
        query.setFilter(null);
        query.setAncestor(getParentKey());
        PreparedQuery pq = mDatastore.prepare(query);
        Iterator<Entity> entities = pq.asIterator();
        if(entities!=null) {
            while (entities.hasNext()) {
                Entity entity = entities.next();
                if (entity != null) {
                    mDatastore.delete(entity.getKey());
                }
            }
        }
    }

    public Entry getEntryByIdentifier(String id, String phoneId){
        Entity entity = null;
        Key key = KeyFactory.createKey(getParentKey(), Entry.ENTITY_NAME, id + " " + phoneId);
        try {
            entity = mDatastore.get(key);
        }catch (Exception e){}
        if(entity != null) {
            mLogger.log(Level.INFO, "Entry with id " + id + " " + phoneId + " was found");
            return convertEntity2Entry(entity);
        }
        else {
            mLogger.log(Level.INFO, "Entry with id " + id + " " + phoneId + " not found");
            return null;
        }


    }

    private Entry convertEntity2Entry(Entity entity) {
        Entry entry = new Entry();
        entry.setPhoneId(entity.getProperty(Entry.PHONE_ID).toString());
        entry.setId(entity.getProperty(Entry.ID).toString());
        entry.setTitle(entity.getProperty(Entry.TITLE).toString());
        entry.setAuthor(entity.getProperty(Entry.AUTHOR).toString());
        entry.setGenre(entity.getProperty(Entry.GENRE).toString());
        entry.setRating(entity.getProperty(Entry.RATING).toString());
        entry.setComment(entity.getProperty(Entry.COMMENT).toString());
        entry.setStatus(entity.getProperty(Entry.STATUS).toString());
        entry.setLocationList(entity.getProperty(Entry.LOCATION_LIST).toString());
        entry.setTimeList(entity.getProperty(Entry.TIMES_LIST).toString());
        entry.setPageList(entity.getProperty(Entry.PAGES_LIST).toString());
        entry.setTotalPages(entity.getProperty(Entry.PAGES).toString());

        return entry;
    }

    /*
    Adds a given entry to the datastore
     */
    public boolean addEntry2Datastore(Entry entry){
        if(getEntryByIdentifier(entry.getId(), entry.getPhoneId()) != null) {
            mLogger.log(Level.INFO, "Entry already in datastore ");
            return false;
        } else{

            Entity entity = new Entity(Entry.ENTITY_NAME, entry.getId() + " " + entry.getPhoneId(), getParentKey());
            entity.setProperty(Entry.PHONE_ID, entry.getPhoneId());
            entity.setProperty(Entry.ID, entry.getId());
            entity.setProperty(Entry.TITLE, entry.getTitle());
            entity.setProperty(Entry.AUTHOR, entry.getAuthor());
            entity.setProperty(Entry.GENRE, entry.getGenre());
            entity.setProperty(Entry.RATING, entry.getRating());
            entity.setProperty(Entry.COMMENT, entry.getComment());
            entity.setProperty(Entry.STATUS, entry.getStatus());
            entity.setProperty(Entry.LOCATION_LIST, entry.getLocationList());
            entity.setProperty(Entry.PAGES_LIST, entry.getPageList());
            entity.setProperty(Entry.TIMES_LIST, entry.getTimeList());
            entity.setProperty(Entry.PAGES, entry.getTotalPages());

            mDatastore.put(entity);

            mLogger.log(Level.INFO, "Entry added ");
            return true;
        }
    }


    public boolean delete(String id, String phoneId) {
//        Query.Filter filter = new Query.FilterPredicate(Entry.ID, Query.FilterOperator.EQUAL, id);
//        Query query = new Query(Entry.ENTITY_NAME);
//        query.setFilter(filter);
//        query.setAncestor(getParentKey());
//        PreparedQuery preparedQuery = mDatastore.prepare(query);
//        Entity entity = preparedQuery.asSingleEntity();
        Entity entity = null;
        Key key = KeyFactory.createKey(getParentKey(), Entry.ENTITY_NAME, id + " " + phoneId);
        try {
            entity = mDatastore.get(key);
        }catch (Exception e){}

//        Key key = KeyFactory.createKey(getParentKey(), Entry.ENTITY_NAME, id + " " + phoneId);
//        mDatastore.delete(key);
        if (entity == null) {
            mLogger.log(Level.INFO, "DELETE UNSUCCESSFUL");
            return false;
        }else {
            mLogger.log(Level.INFO, "DELETE SUCCESSFUL KEY IS " + entity.getKey());
            mDatastore.delete(entity.getKey());
            return true;
        }
    }

    /*
    This method queries and returns an arraylist of all current entries
     */
    public ArrayList<Entry> query() {
        ArrayList<Entry> result = new ArrayList<Entry>();
        //Query query = new Query(Entry.ID);
        Query query = new Query(Entry.ENTITY_NAME);
        query.setFilter(null);
        query.setAncestor(getParentKey());
        PreparedQuery pq = mDatastore.prepare(query);
        Iterator<Entity> entities = pq.asIterator();
        if(entities!=null) {
            while (entities.hasNext()) {
                Entity entity = entities.next();
                if (entity != null) {
                    result.add(convertEntity2Entry(entity));
                }
            }
        } else {
            mLogger.log(Level.INFO, "No entries found");
        }

        return result;
    }

    public void updateEntry(Entry entry) {
        Entity entity = new Entity(Entry.ENTITY_NAME, entry.getId() + " " + entry.getPhoneId(), getParentKey());
        entity.setProperty(Entry.PHONE_ID, entry.getPhoneId());
        entity.setProperty(Entry.ID, entry.getId());
        entity.setProperty(Entry.TITLE, entry.getTitle());
        entity.setProperty(Entry.AUTHOR, entry.getAuthor());
        entity.setProperty(Entry.GENRE, entry.getGenre());
        entity.setProperty(Entry.RATING, entry.getRating());
        entity.setProperty(Entry.COMMENT, entry.getComment());
        entity.setProperty(Entry.STATUS, entry.getStatus());
        entity.setProperty(Entry.LOCATION_LIST, entry.getLocationList());
        entity.setProperty(Entry.PAGES_LIST, entry.getPageList());
        entity.setProperty(Entry.TIMES_LIST, entry.getTimeList());
        entity.setProperty(Entry.PAGES, entry.getTotalPages());

        mDatastore.put(entity);
    }


}
