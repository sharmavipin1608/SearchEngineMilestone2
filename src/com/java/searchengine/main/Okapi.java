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
public class Okapi implements IVariableTermFrequency {

    @Override
    public double queryTermWeight(int sizeOfCorpus, int documentFreqOfTerm) {
        double weightQueryTerm = Math.log((sizeOfCorpus - documentFreqOfTerm + 0.5)/(documentFreqOfTerm + 0.5));
        return weightQueryTerm;
    }

    @Override
    public double documentTermWeight(int freqOfTermInDoc, DiskInvertedIndex index, int docId) {
        double averageDocumentWeight = 0;
        for(int i=0; i<index.getFileNames().size(); i++){
            averageDocumentWeight += index.getDocumentWeights(i);
        }
        averageDocumentWeight = (averageDocumentWeight/index.getFileNames().size());
        
        double documentWeight = index.getDocumentWeights(docId);
        
        double kDoc = 1.2 * (0.25 + (0.75 * (documentWeight/averageDocumentWeight)));
        double docWeightTerm = (2.2 * freqOfTermInDoc)/(freqOfTermInDoc + kDoc);
        return docWeightTerm;
    }

    @Override
    public double documentWeight(DiskInvertedIndex index, int docId) {
        return 1;
    }
    
}
