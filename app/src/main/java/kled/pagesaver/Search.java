package kled.pagesaver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by eloisedietz on 2/24/17.
 * A search class to search for
 */

public class Search {
    int mSearchBy;
    String keyWord;
    String[] words;
    List<BookEntry> entries;
    BookEntryDbHelper database;
    ArrayList<String> genres;

    public Search(int searchBy, String search) {
        mSearchBy = searchBy;
        keyWord = search.trim().toLowerCase();
        genres = getAllGenres();
    }

    public Search(String search, List<BookEntry> entries) {
        keyWord = search;
        this.entries = entries;
        words = keyWord.split(" ");
    }

    /*
    narrows the entries based on the search
     */
    public Set<BookEntry> narrowEntries() {
        Set<BookEntry> set = new HashSet<>();

        narrowList(set);

        return set;

    }

    /*
        This class narrows the list of book entries by what the user searched
     */
    public void narrowList(Set<BookEntry> set) {
        for(BookEntry entry : entries) {
            Map<String, String> entryMap = new HashMap<>();
            entry.entryToMap(entryMap);

            boolean foundFlag = false;
            for(String field : entryMap.values()) {
                for(String word : words) {
                    //searches won't be affected by capital vs. lowercase letters
                    if(field != null && field.toLowerCase().contains(word.toLowerCase())) {
                        set.add(entry);
                        foundFlag = true;
                        break;
                    }
                }

                if(foundFlag)
                    break;
            }
        }
    }

    public ArrayList<BookEntry> findAllEntries(){
        if(mSearchBy == 0){
            return findTitle();
        } else if(mSearchBy == 1){
            return findAuthor();
        } else if(mSearchBy == 2){
            return findGenre();
        }else if(mSearchBy == 3){
            return findRating();
        } else if(mSearchBy == 4){
            return findComment();
        } else if(mSearchBy == 5){
            return findStatus();
        } else {
            return new ArrayList<>();
        }
    }

    /*
    The following classes search based on each information type saved in the database
     */

    public ArrayList<BookEntry> findTitle(){
        ArrayList<BookEntry> result = new ArrayList<>();
        for(BookEntry entry : database.fetchEntries()) {
            if(entry.getTitle().equals(keyWord)){
                result.add(entry);
            }
        }
        return result;
    }


    public ArrayList<BookEntry> findAuthor(){
        ArrayList<BookEntry> result = new ArrayList<>();
        for(BookEntry entry : database.fetchEntries()) {
            if(entry.getAuthor().equals(keyWord)){
                result.add(entry);
            }
        }
        return result;
    }

    public ArrayList<BookEntry> findGenre(){
        ArrayList<BookEntry> result = new ArrayList<>();
        for(BookEntry entry : database.fetchEntries()) {
            if(genres.get(entry.getGenre()).equals(keyWord)){
                result.add(entry);
            }
        }
        return result;
    }

    public ArrayList<BookEntry> findRating(){
        ArrayList<BookEntry> result = new ArrayList<>();
        for(BookEntry entry : database.fetchEntries()) {
            if(entry.getRating() == Integer.valueOf(keyWord)){
                result.add(entry);
            }
        }
        return result;
    }

    public ArrayList<BookEntry> findComment(){
        ArrayList<BookEntry> result = new ArrayList<>();
        for(BookEntry entry : database.fetchEntries()) {
            if(entry.getComment().contains(keyWord)){
                result.add(entry);
            }
        }
        return result;
    }


    public ArrayList<BookEntry> findStatus(){
        ArrayList<BookEntry> result = new ArrayList<>();
        for(BookEntry entry : database.fetchEntries()) {
            if(entry.getStatus() == Integer.valueOf(keyWord)){
                result.add(entry);
            }
        }
        return result;
    }

    /*
    Converts genre from number to string
     */
    public static ArrayList<String> getAllGenres(){
        ArrayList<String> result = new ArrayList<String>();
        result.add("Fiction");
        result.add("Comedy");
        result.add("Action");
        result.add("Drama");
        result.add("Romance");
        result.add("Mystery");
        result.add("Horror");
        result.add("Self Help");
        result.add("Health");
        result.add("Travel");
        result.add("Chidren");
        result.add("Biography");
        result.add("Other");
        return result;
    }

}
