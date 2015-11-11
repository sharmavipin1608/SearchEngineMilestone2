package com.java.searchengine.main;

/**
 * Calculate document weight term according to Okapi weighing scheme
 */
public class Okapi implements IVariableTermFrequency {

    /**
     * Calculate the weight of term in the query
     * 
     * @param sizeOfCorpus - Total number of documents
     * @param documentFreqOfTerm - Number of documents containing the term
     * 
     * @return term weight in the query
     */
    @Override
    public double queryTermWeight(int sizeOfCorpus, int documentFreqOfTerm) {
        double weightQueryTerm = Math.log(((double)sizeOfCorpus - (double)documentFreqOfTerm + 0.5)/((double)documentFreqOfTerm + 0.5));
        return weightQueryTerm;
    }

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
    @Override
    public double documentTermWeight(int freqOfTermInDoc, double averageTermFreq, 
            double documentWeight, double averageDocumentWeight, int docId) {
//        System.out.println("Okapi : freqOfTermInDoc : " + freqOfTermInDoc + " averageTermFreq : " + averageTermFreq
//            + " documentWeight : " + documentWeight + " averageDocumentWeight : " + averageDocumentWeight
//            + " docId : " + docId);
        double kDoc = 1.2 * (0.25 + (0.75 * (documentWeight/averageDocumentWeight)));
        double docWeightTerm = (double)((2.2 * freqOfTermInDoc)/(freqOfTermInDoc + kDoc));
        return docWeightTerm;
    }

    /**
     * Calculate the document weight
     * 
     * @param index - DiskInvertedIndex instance
     * @param docId - document Id
     * 
     * @return document weight
     */
    @Override
    public double documentWeight(DiskInvertedIndex index, int docId) {
        return 1;
    }
    
}
