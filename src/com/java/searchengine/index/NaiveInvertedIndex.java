package com.java.searchengine.index;


import java.util.*;

/**
 *
 * @author vipinsharma
 */
public class NaiveInvertedIndex {

    private HashMap<String, List<Integer>> mIndex;

    /**
     *
     */
    public NaiveInvertedIndex() {
        mIndex = new HashMap<String, List<Integer>>();
    }

    /**
     *
     * @param term
     * @param documentID
     */
    public void addTerm(String term, int documentID) {
        // TO-DO: add the term to the index hashtable. If the table does not have
        // an entry for the term, initialize a new ArrayList<Integer>, add the 
        // docID to the list, and put it into the map. Otherwise add the docID
        // to the list that already exists in the map, but ONLY IF the list does
        // not already contain the docID.
        
        List<Integer> docIdList = new ArrayList<Integer>();
        if(mIndex.containsKey(term)){
            docIdList = mIndex.get(term);
            int docIdListSize = docIdList.size();
            
            if(docIdList.get(docIdListSize-1) != documentID)
                docIdList.add(documentID);
        }
        else{
            docIdList.add(documentID);
            mIndex.put(term, docIdList);
        }
    }

    /**
     *
     * @param term
     * @return
     */
    public List<Integer> getPostings(String term) {
        // TO-DO: return the postings list for the given term from the index map.
        List<Integer> docIdList = mIndex.get(term);
        return docIdList;
    }

    /**
     *
     * @return
     */
    public int getTermCount() {
      // TO-DO: return the number of terms in the index.
        int termCount = mIndex.keySet().size();
        return termCount;
    }

    /**
     *
     * @return
     */
    public String[] getDictionary() {
      // TO-DO: fill an array of Strings with all the keys from the hashtable.
        // Sort the array and return it.
        String[] termsArray = mIndex.keySet().toArray(
                new String[mIndex.keySet().size()]);
        Arrays.sort(termsArray);
        return termsArray;
    }
}
