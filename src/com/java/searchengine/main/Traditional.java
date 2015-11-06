/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.java.searchengine.main;

/**
 *
 * @author vipinsharma
 */
public class Traditional implements IVariableTermFrequency{

    @Override
    public double queryTermWeight(int sizeOfCorpus, int documentFreqOfTerm) {
        double weightQueryTerm = Math.log((double)sizeOfCorpus/documentFreqOfTerm);
        return weightQueryTerm;
    }

    @Override
    public double documentTermWeight(int freqOfTermInDoc, double averageTermFreq, 
            double documentWeight, double averageDocumentWeight, int docId) {
        double docWeightTerm = freqOfTermInDoc;
        return docWeightTerm;
    }

    @Override
    public double documentWeight(DiskInvertedIndex index, int docId) {
        return index.getDocumentWeights(docId);
    }
    
    
}
