package com.java.searchengine.index;


import Archive.IndexBuilderFactory;
import com.java.searchengine.datastructure.PositionalPostingsStructure;
import com.java.searchengine.util.SearchEngineUtilities;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

/**
 * Creates and manages the positional inverted index for all the text files 
 * in a folder selected by the user
 * 
 */
public class PositionalInvertedIndex {
    //HashMap containing the positional inverted index
    private HashMap<String, List<PositionalPostingsStructure>> pIndex;

    //Variables to save Index Statistics
    private HashSet<String> typeSet;
    private int numOfTerms = 0;
    private int numOfTypes = 0;
    private HashMap<String, Integer> averagePostings;
    private double[] documentProportion;
    private long totalMemoryRequirement = 0;
    Map<String, Integer> averagePostingsTree;

    /**
     * Class attributes are being initialized
     */
    public PositionalInvertedIndex() {
        pIndex = new HashMap<>();
        typeSet = new HashSet<>();
        averagePostings = new HashMap<>();
        documentProportion = new double[10];
    }

    /**
     * Adds all the types to the set typeSet
     * 
     * @param term
     */
    public void addType(String term) {
        typeSet.add(term);
    }

    /**
     * Adds a term to the positional inverted index. 
     * Checks for the following three conditions
     * 1. New term is being added
     * 2. Term already present but new document being indexed
     * 3. Term and document id are present, new positional information is being
     *    added
     * 
     * @param term
     * @param documentId
     * @param position
     */
    public void addTerm(String term, int documentId, int position) {
        if (pIndex.containsKey(term)) {
            List<PositionalPostingsStructure> termPostingsList
                    = pIndex.get(term);
            if (termPostingsList.get(termPostingsList.size() - 1).getDocumentId()
                    == documentId) {
                termPostingsList.get(termPostingsList.size() - 1)
                        .addPosition(position);
            } else {
                PositionalPostingsStructure termPostings
                        = new PositionalPostingsStructure(documentId, position);
                termPostingsList.add(termPostings);
            }
        } else {
            List<PositionalPostingsStructure> termPostingsList
                    = new ArrayList<>();
            PositionalPostingsStructure termPostings
                    = new PositionalPostingsStructure(documentId, position);
            termPostingsList.add(termPostings);
            pIndex.put(term, termPostingsList);
        }
    }

    /**
     * Get postings list for a particular term
     * 
     * @param term
     * @return
     */
    public List<PositionalPostingsStructure> getPostings(String term) {
        return pIndex.get(term);
    }

    /**
     * Prints the whole positional inverted index on console
     */
    public void printResults() {
        int longestTerm = 0;
        String[] termsArray = pIndex.keySet().toArray(
                new String[pIndex.keySet().size()]);
        Arrays.sort(termsArray);
        for (String term : termsArray) {
            longestTerm = Math.max(longestTerm, term.length());
        }

        for (String term : termsArray) {
            System.out.print("\n" + term);
            SearchEngineUtilities.printSpaces(longestTerm - term.length());
            int longestTermTemp = 0;
            
            for (PositionalPostingsStructure posStructure : pIndex.get(term)) {
                posStructure.printData(longestTermTemp);
                System.out.print("\n");
                longestTermTemp = longestTerm;
            }
        }
    }

    /**
     * Calculate index metrics for the positional inverted index.
     * Following metrics are being calculated
     * 1. Number of terms and types
     * 2. Average postings for each term
     * 3. Top 10 most frequent terms and the proportion of documents
     * 4. Memory required to store the positional inverted index
     */
    public void calculateMetrics() {
        numOfTerms = pIndex.keySet().size();
        numOfTypes = typeSet.size();

        //calculating average num of documents in a posting list
        int numOfDocuments;
        int positionListSize;

        long termMemory = 0;
        long postingListMemory = 0;
        long postingsMemory = 0;

        HashMap<String, Integer> totalPostings = new HashMap<>();

        for (String term : pIndex.keySet()) {
            numOfDocuments = pIndex.get(term).size();
            positionListSize = 0;

            for (PositionalPostingsStructure posStructure : pIndex.get(term)) {
                positionListSize += posStructure.getPositionList().size();
            }

            averagePostings.put(term, positionListSize / numOfDocuments);
            totalPostings.put(term, positionListSize);

            termMemory += (40 + 2 * (term.length()));
            postingListMemory += (24 + 8 * numOfDocuments);
            postingsMemory += ((48 * numOfDocuments) + (4 * positionListSize));
        }

        totalMemoryRequirement = (24 + 36 * numOfTerms) + termMemory + postingListMemory + postingsMemory;

        //converting averagePostings to treemap sorted on the average number of 
        //documents in the posting list
        Comparator<String> valueComparator = new Comparator<String>() {
            public int compare(String k1, String k2) {
                int compare = totalPostings.get(k2).compareTo(totalPostings.get(k1));
                if (compare == 0) {
                    return 1;
                } else {
                    return compare;
                }
            }
        };

        averagePostingsTree = new TreeMap<>(valueComparator);

        averagePostingsTree.putAll(totalPostings);
    }

    /**
     * Prints the positional index metrics
     */
    public void printMetrics() {
        IndexBuilderFactory indexBuilderFactory = IndexBuilderFactory.getInstance();
        int numOfDocsIndexed = indexBuilderFactory.fileNamesHashSet().size();
        
        System.out.println("\n\n----Index Statistics----");
        System.out.println("\nNumber of types : " + numOfTypes);
        System.out.println("\nNumber of terms : " + numOfTerms);
        System.out.println("\nMost frequent terms : ");
        int count = 0;

        for (Entry<String, Integer> e : averagePostingsTree.entrySet()) {
            if (count == 10) {
                break;
            }
            documentProportion[count] = (double) 
                    (indexBuilderFactory.searchTerm(e.getKey()).size()) / 
                    numOfDocsIndexed;
            
            System.out.println("\t" + (count+1) + ":  " + e.getKey() + " : " + 
                    indexBuilderFactory.searchTerm(e.getKey()).size() + 
                    " : " + documentProportion[count]);
            
            count++;
        }
        
        System.out.println("\nTotal Memory Requirement : " + ((double) totalMemoryRequirement / (1024 * 1024)) + " MB");
    }
    
    /**
     * Get all the terms in the index in a sorted order
     * 
     * @return Sorted String array of all the terms
     */
    public String[] getDictionary() {
        String[] termsArray = pIndex.keySet().toArray(
                new String[pIndex.keySet().size()]);
        Arrays.sort(termsArray);
        return termsArray;
    }
}
