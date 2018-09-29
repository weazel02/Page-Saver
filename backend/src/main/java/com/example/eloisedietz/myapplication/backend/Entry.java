package com.example.eloisedietz.myapplication.backend;


import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by eloisedietz on 2/24/17.
 */

public class Entry {

    public static final String PARENT_KIND = "parent kind";
    public static final String PARENT_IDENTIFIER = "parent identifier";
    public static final String ENTITY_NAME = "entry";


    public static final String ID = "id";
    public static final String PHONE_ID = "phone_id";
    public static final String REG_ID = "reg_id";
    public static final String TITLE = "title";
    public static final String AUTHOR = "author";
    public static final String GENRE = "genre";
    public static final String RATING = "rating";
    public static final String COMMENT = "comment";
    public static final String STATUS = "status";
    public static final String LOCATION_LIST = "location_list";
    public static final String TIMES_LIST = "times_list";
    public static final String PAGES_LIST = "pages_list";
    public static final String PAGES = "total_pages";


    private String mId = "";
    private String mPhone = "";

    private String mTitle = "";
    private String mAuthor= "";
    private String mGenre= "";
    private String mRating= "";
    private String mComment= "";
    private String mStatus= "";

    private String mLocationList = "";   //string of the bytes of the location list
    private String mTimeList = "";   //string of the bytes of the times list
    private String mPageList = "";   //string of the bytes of the page ranges list
    private String mTotalPages = "";

    public void setPhoneId(String id){
        mPhone = id;
    }

    public String getPhoneId() {
        return mPhone;
    }

    public void setId(String id){
        mId = id;
    }

    public String  getId() {
        return mId;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setAuthor(String author) {
        mAuthor = author;
    }

    public String getAuthor() {
        return mAuthor;
    }

    public void setGenre(String genre) {
        mGenre = genre;
    }

    public String getGenre() {
        return mGenre;
    }

    public void setRating(String rating) {
        mRating = rating;
    }

    public String getRating() {
        return mRating;
    }

    public void setComment(String comment) {
        mComment = comment;
    }

    public String getComment() {
        return mComment;
    }

    public void setStatus(String status) {
        mStatus = status;
    }

    public String getStatus() {
        return mStatus;
    }

    public void setLocationList(String locationList) {
        mLocationList = locationList;
    }

    public String getLocationList() {
        return mLocationList;
    }

    public void setTimeList(String timeList) {
        mTimeList = timeList;
    }

    public String getTimeList() {
        return mTimeList;
    }

    public void setPageList(String pageList) {
        mPageList = pageList;
    }

    public String getPageList() {
        return mPageList;
    }

    public void setTotalPages(String totalPages) { mTotalPages = totalPages; }

    public String getTotalPages() { return mTotalPages; }

    public static Entry requestToEntry(HttpServletRequest request) {
        Entry entry = new Entry();

        entry.setId(request.getParameter(Entry.ID));
        entry.setPhoneId(request.getParameter(Entry.PHONE_ID));
        entry.setTitle(request.getParameter(Entry.TITLE));
        entry.setAuthor(request.getParameter(Entry.AUTHOR));
        entry.setGenre(request.getParameter(Entry.GENRE));
        entry.setRating(request.getParameter(Entry.RATING));
        entry.setComment(request.getParameter(Entry.COMMENT));
        entry.setTotalPages(request.getParameter(Entry.PAGES));
        entry.setStatus(request.getParameter(Entry.STATUS));
        entry.setLocationList(request.getParameter(Entry.LOCATION_LIST));
        entry.setPageList(request.getParameter(Entry.PAGES_LIST));
        entry.setTimeList(request.getParameter(Entry.TIMES_LIST));

        return entry;

    }


    public List<StartEndTimes> getTimeRanges() {
        String [] entries = mTimeList.split(" ");
        List<StartEndTimes> times = new ArrayList<>(entries.length / 2);

        for(int i = 0; i < entries.length; i += 2) {
            long start = Long.parseLong(entries[i]);
            long end = Long.parseLong(entries[i + 1]);

            times.add(new StartEndTimes(start, end));
        }

        return times;
    }

    public List<StartEndPages> getPageRanges() {
        String [] entries = mPageList.split(" ");
        List<StartEndPages> pages = new ArrayList<>(entries.length / 2);

        for(int i = 0; i < entries.length; i += 2) {
            int start = Integer.parseInt(entries[i]);
            int end = Integer.parseInt(entries[i + 1]);

            pages.add(new StartEndPages(start, end));
        }

        return pages;
    }

    public class StartEndTimes {
        Long startTime;  // start times in milliseconds
        Long endTime;    // end times in milliseconds

        public StartEndTimes(Long mStartTime, Long mEndTime){
            startTime = mStartTime;
            endTime = mEndTime;
        }
    }


    /* List of page ranges for each individual reading period */
    public class StartEndPages {
        int startPage;  // page number where this reading starts
        int endPage;    // page number where this reading finishes

        public StartEndPages(int mStartPage, int mEndPage){
            startPage = mStartPage;
            endPage = mEndPage;
        }
    }

}
