/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.java.searchengine.main;

import com.java.searchengine.datastructure.PositionalPostingsStructure;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author vipinsharma
 */
public class RankedRetrievals {
    private String userQuery;
    private DiskInvertedIndex index;
    private int corpusSize;
    private HashMap<Integer,Double> accumulator;
    IVariableTermFrequency variableTermFreq;

    
    public RankedRetrievals(DiskInvertedIndex index){
        this.index = index;
        this.corpusSize = index.getFileNames().size();
        this.accumulator = new HashMap<>();
    }
    
    //Step1: to be run for each term in the user query
    private void calculateQueryTermWeight(String term){
        ArrayList<PositionalPostingsStructure> postings = index.GetPostings(term);
        int documentFrequencyTerm = postings.size();
        variableTermFreq = new Wacky();
        
        //Step1: Calculate w(q,t)
        double weightQueryTerm = variableTermFreq.queryTermWeight(corpusSize, documentFrequencyTerm);
//        double weightQueryTerm1 = Math.log(1 + (corpusSize/documentFrequencyTerm));
//        System.out.println("Weight : "+weightQueryTerm + " weightQueryTerm1 : " + weightQueryTerm1);
        
        //Step2: For each document in terms postings list
        for(PositionalPostingsStructure posStruct : postings){
            //Step2.1: Acquire accumulator
            double accumulatorValueForDoc = 0;
            if(accumulator.get(posStruct.getDocumentId()) != null){
                accumulatorValueForDoc = accumulator.get(posStruct.getDocumentId());
            }
            
            //Step2.2: Calculate w(d,t)
            double docWeightTerm = variableTermFreq.documentTermWeight(posStruct.getTermFrequency(), index, posStruct.getDocumentId());
//            double docWeightTerm1 = 1 + Math.log(posStruct.getTermFrequency());
//            System.out.println("docWeightTerm : "+docWeightTerm + " docWeightTerm1 : " + docWeightTerm1);
            
            //Step2.3: Increase accumulator value
            accumulatorValueForDoc += weightQueryTerm * docWeightTerm;
            
            //Step2.4: Update accumulator value in HashMap
            accumulator.put(posStruct.getDocumentId(), accumulatorValueForDoc);
        }
    }
    
    //Step2: Divide accumulator value by document weight
    private void recalculateAccumulator(){
        System.out.println("recalculateAccumulator--------");
        for(int docId : accumulator.keySet()){
            System.out.println("Doc Id -> " + docId + " ; Accumulator -> " + accumulator.get(docId));
        }
        
        for(int docId : accumulator.keySet()){
            double accumulatorValue = accumulator.get(docId);
            accumulatorValue = accumulatorValue/variableTermFreq.documentWeight(index,docId); //index.getDocumentWeights(docId)EXIT
            
            accumulator.put(docId, accumulatorValue);
        }
    }
    
    public void beginCalculations(String userQuery){
        String[] terms = userQuery.split(" ");
        for(String term : terms){
            calculateQueryTermWeight(term);
        }
        
        System.out.println("beginCalculations--------");
        for(int docId : accumulator.keySet()){
            System.out.println("Doc Id -> " + docId + " ; Accumulator -> " + accumulator.get(docId));
        }
        
        recalculateAccumulator();
        
        System.out.println("beginCalculations--------");
        for(int docId : accumulator.keySet()){
            System.out.println("Doc Id -> " + docId + " ; Accumulator -> " + accumulator.get(docId));
        }
    }
}
