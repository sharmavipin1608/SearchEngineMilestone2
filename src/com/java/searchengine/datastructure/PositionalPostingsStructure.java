package com.java.searchengine.datastructure;

import com.java.searchengine.main.DiskInvertedIndex;
import Archive.IndexBuilderFactory;
import com.java.searchengine.util.SearchEngineUtilities;
import java.util.ArrayList;
import java.util.List;

/**
 * This class defines the data structure being used to save document id along
 * with the positional information of the terms
 */
public class PositionalPostingsStructure {

    //stores the document id of the term

    private int docId;

    //stores the positions term is present in a particular document id
    private List<Integer> positionList;

    //stores the term frequency of the document 
    private int termFrequency;

    //stores the document weight used for calculation of accumulator
    private double docWeight;

    /**
     * Get the document weight according to the weighing scheme selected
     *
     * @return document weight
     */
    public double getDocWeight() {
        return docWeight;
    }

    /**
     * Constructor for the class being used to initialize the attributes of the
     * class and set the value of the document id
     *
     * @param documentId
     * @param position
     */
    public PositionalPostingsStructure(int documentId, int position) {
        docId = documentId;
        positionList = new ArrayList<>();
        positionList.add(position);
        termFrequency = 1;
    }

    /**
     * Set term frequency
     *
     * @param freq
     */
    public void setTermFrequency(int freq) {
        termFrequency = freq;
    }

    /**
     * Get term frequency
     *
     * @return term frequency
     */
    public int getTermFrequency() {
        return termFrequency;
    }

    /**
     * Constructor for the class being used to initialize the attributes of the
     * class and set the value of the document id
     *
     * @param documentId
     * @param positionList
     * @param freq
     * @param position
     * @param docWght
     */
    public PositionalPostingsStructure(int documentId,
            ArrayList<Integer> positionList, int freq, double docWght) {
        this.docId = documentId;
        this.positionList = positionList;
        this.termFrequency = freq;
        this.docWeight = docWght;
    }

    /**
     * Get the document id of the term
     *
     * @return document id
     */
    public int getDocumentId() {
        return docId;
    }

    /**
     * Set the document id of the term
     *
     * @param docId
     * @return document id
     */
    public void setDocumentId(int docId) {
        this.docId = docId;
    }

    /**
     * Get the document name of the term
     *
     * @return document name
     */
    public String getDocumentName() {
        return IndexBuilderFactory.getInstance().getFilesNames(docId);
    }

    /**
     * Get the position list of a particular term in the current document
     *
     * @return list of positions
     */
    public List<Integer> getPositionList() {
        return positionList;
    }

    /**
     * Set the position list for a particular term in a particular document
     *
     * @param list
     * @return list of positions
     */
    public void setPositionList(List<Integer> list) {
        positionList.addAll(list);
    }

    /**
     * Add position to the positionList
     *
     * @param position - new position to be added to the positionList
     */
    public void addPosition(int position) {
        positionList.add(position);
        termFrequency++;
    }

    /**
     * Prints document name along with the positional information of the term
     *
     * @param longestTerm - longest term in the index
     */
    public void printData(int longestTerm) {
        SearchEngineUtilities.printSpaces(longestTerm);
        System.out.print(" : " + IndexBuilderFactory.getInstance().getFilesNames(docId) + " -> ");
        for (int i = 0; i < positionList.size(); i++) {
            System.out.print(positionList.get(i) + ", ");
        }
    }

    /**
     * Prints document name along with the positional information of the term
     *
     * @param longestTerm - longest term in the index
     * @param index
     */
    public void printData(int longestTerm, DiskInvertedIndex index) {
        SearchEngineUtilities.printSpaces(longestTerm);
        System.out.print(" : " + index.getFileNames().get(docId) + " -> ");
        for (int i = 0; i < positionList.size(); i++) {
            System.out.print(positionList.get(i) + ", ");
        }
    }
}
