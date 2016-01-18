package com.java.searchengine.main;

import com.java.searchengine.datastructure.PositionalPostingsStructure;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.PriorityQueue;

/**
 * Process the user query in ranked retrieval mode
 */
public class RankedRetrievals {

    private String userQuery;
    private DiskInvertedIndex index;
    private int corpusSize;
    private HashMap<Integer, Double> accumulator;
    private IVariableTermFrequency variableTermFreq;
    private int weighingScheme;
    private Accumulator[] accumulatorArray;

    /**
     * Constructor for the class
     * 
     * @param index -instance of the disk inverted index
     * @param weighingScheme - weighing scheme chosen by the user
     */
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

            //Step1: Calculate w(q,t)
            double weightQueryTerm = variableTermFreq.queryTermWeight(corpusSize, documentFrequencyTerm);

            //Step2: For each document in terms postings list
            for (PositionalPostingsStructure posStruct : postings) {
                //Step2.1: Acquire accumulator
                double accumulatorValueForDoc = 0;
                //start
                Accumulator tempAccumulator;
                if (accumulatorArray[posStruct.getDocumentId()] != null) {
                    tempAccumulator = accumulatorArray[posStruct.getDocumentId()];
                    accumulatorValueForDoc = tempAccumulator.getAccumulatorValue();
                } else {
                    tempAccumulator = new Accumulator();
                }
                tempAccumulator.setDocId(posStruct.getDocumentId());
                //end
                if (accumulator.get(posStruct.getDocumentId()) != null) {
                    accumulatorValueForDoc = accumulator.get(posStruct.getDocumentId());
                }

                //Step2.2: Calculate w(d,t)
                double docWeightTerm = posStruct.getDocWeight();

                //Step2.3: Increase accumulator value
//                if(posStruct.getDocumentId() > 657)
//                    System.out.println("term : " + term + " docid : " + posStruct.getDocumentId()
//                        + " accumulatorValueForDoc : " + accumulatorValueForDoc + " weightQueryTerm : "
//                            + weightQueryTerm + " docWeightTerm : " + docWeightTerm);
//                if(accumulatorValueForDoc > 100){
//                    System.out.println("too high");
//                }
                accumulatorValueForDoc += weightQueryTerm * docWeightTerm;

                //Step2.4: Update accumulator value in HashMap
                accumulator.put(posStruct.getDocumentId(), accumulatorValueForDoc);
                tempAccumulator.setAccumulatorValue(accumulatorValueForDoc);
                accumulatorArray[posStruct.getDocumentId()] = tempAccumulator;

            }
        }

    }

    //Step2: Divide accumulator value by document weight
    private void recalculateAccumulator() {
        for (int docId : accumulator.keySet()) {
            //System.out.println("Document weight : "+variableTermFreq.documentWeight(index,docId));
            double accumulatorValue = accumulator.get(docId);
//            System.out.println("recalculate -> doc id : " + docId + 
//                    " accumulatorValue : " + accumulatorValue + " variable term freq : "
//                    + variableTermFreq.documentWeight(index, docId));
            accumulatorValue = accumulatorValue / variableTermFreq.documentWeight(index, docId); //index.getDocumentWeights(docId)EXIT

            accumulator.put(docId, accumulatorValue);
        }

        Comparator<Accumulator> valueComparator = new Comparator<Accumulator>() {
            public int compare(Accumulator o1, Accumulator o2) {
                double difference = o2.getAccumulatorValue() - o1.getAccumulatorValue();
                if (difference == 0) {
                    return 0;
                } else if (difference > 0) {
                    return 1;
                } else {
                    return -1;
                }
            }
        };

        if (accumulator.keySet().size() > 0) {
            PriorityQueue aQueue = new PriorityQueue(accumulator.keySet().size(), valueComparator);
            for (Accumulator acc : accumulatorArray) {
                if (acc != null) {
                    //System.out.println(index.getFileNames().get(acc.getDocId()) + " -> " + variableTermFreq.documentWeight(index, acc.getDocId()));
                    acc.setAccumulatorValue(acc.getAccumulatorValue() / variableTermFreq.documentWeight(index, acc.getDocId()));
                    aQueue.add(acc);
                }

            }

            //aQueue.comparator() = new Comparator(
            int accSize = accumulator.keySet().size() > 10 ? 10 : accumulator.keySet().size();
            for (int i = 0; i < accSize; i++) {
                Accumulator acc = (Accumulator) aQueue.poll();
                System.out.println("doc id : " + acc.getDocId() + " doc name : " + index.getFileNames().get(acc.getDocId()) + " accumulator : " + acc.getAccumulatorValue());
            }
        }
        else{
            System.out.println("No match found");
        }

    }

    /**
     * Start calculations of the accumulator
     * 
     * @param userQuery - query entered by the user
     */
    public void beginCalculations(String userQuery) {
        String[] terms = userQuery.split(" ");
        for (String term : terms) {
            term = PorterStemmer.processToken(term);
            calculateQueryTermWeight(term);
        }

        recalculateAccumulator();
    }
    
    /**
     * Class is being used to club the values of docId and accumulator which 
     * are required when using the priority queue
     */
    class Accumulator implements Comparator<Accumulator> {

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

        public Accumulator() {
            docId = -1;
            accumulatorValue = 0;
        }

        @Override
        public int compare(Accumulator o1, Accumulator o2) {
            double difference = o2.getAccumulatorValue() - o1.getAccumulatorValue();
            if (difference == 0) {
                return 0;
            } else if (difference > 0) {
                return 1;
            } else {
                return -1;
            }
        }
    }
}
