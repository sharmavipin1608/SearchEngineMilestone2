package com.java.searchengine.main;

public interface IVariableTermFrequency {

    /**
     * Calculate the weight of term in the query
     * 
     * @param sizeOfCorpus - Total number of documents
     * @param documentFreqOfTerm - Number of documents containing the term
     * 
     * @return term weight in the query
     */
    public double queryTermWeight(int sizeOfCorpus, int documentFreqOfTerm);
    
    /**
     * Calculate the weight of term in the document
     * 
     * @param freqOfTermInDoc - Frequency of term in the particular document
     * @param averageTermFreq - Average term frequency
     * @param documentWeight - Weight of the document
     * @param averageDocumentWeight  Average weight of the document
     * @param docId - document Id
     * 
     * @return document weight of the term
     */
    public double documentTermWeight(int freqOfTermInDoc, double averageTermFreq, 
            double documentWeight, double averageDocumentWeight, int docId);
    
    /**
     * Calculate the document weight
     * 
     * @param index - DiskInvertedIndex instance
     * @param docId - document Id
     * 
     * @return document weight
     */
    public double documentWeight(DiskInvertedIndex index, int docId);
    
    //public double postDocTermWght(int freqOfTermInDoc);
}