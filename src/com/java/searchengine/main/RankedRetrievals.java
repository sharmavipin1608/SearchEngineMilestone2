/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.java.searchengine.main;

import com.java.searchengine.datastructure.PositionalPostingsStructure;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.PriorityQueue;

/**
 *
 * @author vipinsharma
 */
public class RankedRetrievals {

    private String userQuery;
    private DiskInvertedIndex index;
    private int corpusSize;
    private HashMap<Integer, Double> accumulator;
    private IVariableTermFrequency variableTermFreq;
    private int weighingScheme;
    private Accumulator[] accumulatorArray;

    public RankedRetrievals(DiskInvertedIndex index, int weighingScheme) {
        this.index = index;
        this.corpusSize = index.getFileNames().size();
        this.accumulator = new HashMap<>();
        this.accumulatorArray = new Accumulator[corpusSize];
        this.variableTermFreq = VariableTermFreqFactory.getWeightingScheme(weighingScheme);
        this.weighingScheme = weighingScheme;
    }

    //Step1: to be run for each term in the user query
    private void calculateQueryTermWeight(String term) {
        ArrayList<PositionalPostingsStructure> postings
                = index.getPostingsRankedMode(term, weighingScheme);
        int documentFrequencyTerm = 0;
        if (postings != null) {
            documentFrequencyTerm = postings.size();

        //variableTermFreq = new Wacky();
            //Step1: Calculate w(q,t)
            double weightQueryTerm = variableTermFreq.queryTermWeight(corpusSize, documentFrequencyTerm);
//        double weightQueryTerm1 = Math.log(1 + (corpusSize/documentFrequencyTerm));
//        System.out.println("Weight : "+weightQueryTerm + " weightQueryTerm1 : " + weightQueryTerm1);

            //Step2: For each document in terms postings list
            for (PositionalPostingsStructure posStruct : postings) {
                //Step2.1: Acquire accumulator
                double accumulatorValueForDoc = 0;
                //start
                Accumulator tempAccumulator;
                if(accumulatorArray[posStruct.getDocumentId()] != null){
                    tempAccumulator = accumulatorArray[posStruct.getDocumentId()];
                    accumulatorValueForDoc = tempAccumulator.getAccumulatorValue();
                }
                else{
                    tempAccumulator = new Accumulator();
                }    
                tempAccumulator.setDocId(posStruct.getDocumentId());
                //end
                if (accumulator.get(posStruct.getDocumentId()) != null) {
                    accumulatorValueForDoc = accumulator.get(posStruct.getDocumentId());
                }
//
//                //Step2.2: Calculate w(d,t)
                double docWeightTerm = posStruct.getDocWeight();
//                System.out.println("docWeightTerm : " + docWeightTerm);
////            double docWeightTerm1 = 1 + Math.log(posStruct.getTermFrequency());
////            System.out.println("docWeightTerm : "+docWeightTerm + " docWeightTerm1 : " + docWeightTerm1);
//
//                //Step2.3: Increase accumulator value
                accumulatorValueForDoc += weightQueryTerm * docWeightTerm;
//
//                //Step2.4: Update accumulator value in HashMap
                accumulator.put(posStruct.getDocumentId(), accumulatorValueForDoc);
                tempAccumulator.setAccumulatorValue(accumulatorValueForDoc);
                accumulatorArray[posStruct.getDocumentId()] = tempAccumulator;
                
            }
        }
        
        
    }

    //Step2: Divide accumulator value by document weight
    private void recalculateAccumulator() {
        //System.out.println("recalculateAccumulator--------");
//        for (int docId : accumulator.keySet()) {
//            System.out.println("Doc Id -> " + docId + " ; Accumulator -> " + accumulator.get(docId));
//        }

        for (int docId : accumulator.keySet()) {
            //System.out.println("Document weight : "+variableTermFreq.documentWeight(index,docId));
            double accumulatorValue = accumulator.get(docId);
            accumulatorValue = accumulatorValue / variableTermFreq.documentWeight(index, docId); //index.getDocumentWeights(docId)EXIT

            accumulator.put(docId, accumulatorValue);
        }
        
        Comparator<Accumulator> valueComparator = new Comparator<Accumulator>() {
            public int compare(Accumulator o1, Accumulator o2) {
            double difference = o2.getAccumulatorValue() - o1.getAccumulatorValue();
            if( difference == 0)
                return 0;
            else if (difference > 0)
                return 1;
            else
                return -1;
        }
        };
        
        PriorityQueue aQueue = new PriorityQueue(accumulator.keySet().size(),valueComparator);
        for(Accumulator acc : accumulatorArray){
            if(acc != null){
                //System.out.println(index.getFileNames().get(acc.getDocId()) + " -> " + variableTermFreq.documentWeight(index, acc.getDocId()));
                acc.setAccumulatorValue(acc.getAccumulatorValue()/variableTermFreq.documentWeight(index, acc.getDocId()));
                aQueue.add(acc);
            }
                
        }
        
        //aQueue.comparator() = new Comparator(
        int accSize = accumulator.keySet().size() > 10 ? 10 : accumulator.keySet().size();
        for(int i=0;i<accSize;i++){
            Accumulator acc = (Accumulator)aQueue.poll();
            System.out.println("doc id : " + index.getFileNames().get(acc.getDocId()) + " accumulator : " + acc.getAccumulatorValue());
        }
    }

    public void beginCalculations(String userQuery) {
        String[] terms = userQuery.split(" ");
        for (String term : terms) {
            term = PorterStemmer.processToken(term);
            calculateQueryTermWeight(term);
        }
        
        
        
//        System.out.println("beginCalculations--------");
//        for (int docId : accumulator.keySet()) {
//            System.out.println("Doc Id -> " + docId + " ; Accumulator -> " + accumulator.get(docId));
//        }

        recalculateAccumulator();

//        System.out.println("beginCalculations--------");
//        for (int docId : accumulator.keySet()) {
//            System.out.println("Doc Id -> " + docId + " ; Accumulator -> " + accumulator.get(docId));
//        }
        
    }
    
    class Accumulator implements Comparator<Accumulator>{
        private int docId;
        private double accumulatorValue;

        public int getDocId() {
            return docId;
        }

        public void setDocId(int docId) {
            this.docId = docId;
        }

        public double getAccumulatorValue() {
            return accumulatorValue;
        }

        public void setAccumulatorValue(double accumulatorValue) {
            this.accumulatorValue = accumulatorValue;
        }
        
        public Accumulator(){
            docId = -1;
            accumulatorValue = 0;
        }

        @Override
        public int compare(Accumulator o1, Accumulator o2) {
            double difference = o2.getAccumulatorValue() - o1.getAccumulatorValue();
            if( difference == 0)
                return 0;
            else if (difference > 0)
                return 1;
            else
                return -1;
        }
    }
}
