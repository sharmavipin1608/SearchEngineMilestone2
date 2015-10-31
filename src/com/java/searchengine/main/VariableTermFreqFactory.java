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
public class VariableTermFreqFactory {
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
