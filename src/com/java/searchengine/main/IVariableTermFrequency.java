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
public interface IVariableTermFrequency {
    public double queryTermWeight(int sizeOfCorpus, int documentFreqOfTerm);
    
    public double documentTermWeight(int freqOfTermInDoc, DiskInvertedIndex index, int docId);
    
    public double documentWeight(DiskInvertedIndex index, int docId);
}
