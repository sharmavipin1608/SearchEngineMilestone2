package com.java.searchengine.main;

/**
 * Factory class to return an object of IVariableTermFrequency depending on the 
 * weighing scheme chosen by the user.
 */
public class VariableTermFreqFactory {

    /**
     * Returns object of Default, Traditional, Okapi or Wacky depending on the
     * weighing scheme chosen by the user.
     * 
     * @param scheme - weighing scheme chosen by the user
     * 
     * @return IVariableTermFrequency
     */
    public static IVariableTermFrequency getWeightingScheme(int scheme){
        IVariableTermFrequency variableTermFrequency;
        
        switch(scheme){
            case 1:
                variableTermFrequency = new Default();
                break;
                
            case 2:
                variableTermFrequency = new Traditional();
                break;
                
            case 3:
                variableTermFrequency = new Okapi();
                break;
                
            case 4:
                variableTermFrequency = new Wacky();
                break;
                
            default:
                variableTermFrequency = new Default();
                break;
        }
        
        return variableTermFrequency;
    }
}
