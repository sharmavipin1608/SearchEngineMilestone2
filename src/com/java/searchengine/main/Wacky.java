package com.java.searchengine.main;

import java.io.File;

/**
 * Calculate document weight term according to Wacky weighing scheme
 */
public class Wacky implements IVariableTermFrequency {

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
        double weightQueryTerm = Math.max(0,Math.log(((double)sizeOfCorpus - documentFreqOfTerm)/documentFreqOfTerm));
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
        double docWeightTerm;
        docWeightTerm = (1 + Math.log(freqOfTermInDoc))/(1 + Math.log(averageTermFreq));
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
        File file = new File(index.mPath,index.getFileNames().get(docId));
        return Math.sqrt(file.length());
    }
    
}
