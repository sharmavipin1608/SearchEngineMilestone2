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
public class Default implements IVariableTermFrequency{

    @Override
    public double queryTermWeight(int sizeOfCorpus, int documentFreqOfTerm) {
        double weightQueryTerm = Math.log(1 + (sizeOfCorpus/documentFreqOfTerm));
        return weightQueryTerm;
    }

    @Override
    public double documentTermWeight(int freqOfTermInDoc, DiskInvertedIndex index, int docId) {
        double docWeightTerm = 1 + Math.log(freqOfTermInDoc);
        return docWeightTerm;
    }

    @Override
    public double documentWeight(DiskInvertedIndex index, int docId) {
        return index.getDocumentWeights(docId);
    }
    
    
}
