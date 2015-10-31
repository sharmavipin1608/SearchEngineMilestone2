/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.java.searchengine.main;

import java.io.File;

/**
 *
 * @author vipinsharma
 */
public class Wacky implements IVariableTermFrequency {

    @Override
    public double queryTermWeight(int sizeOfCorpus, int documentFreqOfTerm) {
        double weightQueryTerm = Math.max(0,Math.log((double)(sizeOfCorpus - documentFreqOfTerm)/documentFreqOfTerm));
        
        System.out.println("wacky : queryTermWeight() : " + weightQueryTerm + " size : " + sizeOfCorpus + " freq : " + documentFreqOfTerm);
        
        return weightQueryTerm;
    }

    @Override
    public double documentTermWeight(int freqOfTermInDoc, DiskInvertedIndex index, int docId) {
        double docWeightTerm;
        docWeightTerm = (1 + Math.log(freqOfTermInDoc))/(1 + Math.log(index.getAvgTermFreqForDoc(docId)));
        System.out.println("wacky : documentTermWeight() : " + docWeightTerm);
        return docWeightTerm;
    }

    @Override
    public double documentWeight(DiskInvertedIndex index, int docId) {
        File file = new File(index.mPath,index.getFileNames().get(docId));
        System.out.println("wacky file length : "+file.length());
        return file.length();
    }
    
}
