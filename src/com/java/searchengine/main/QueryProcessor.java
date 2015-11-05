/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.java.searchengine.main;

import com.java.searchengine.datastructure.PositionalPostingsStructure;
import com.java.searchengine.util.SearchEngineUtilities;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Pattern;

/**
 *
 * @author vipinsharma
 */
public class QueryProcessor {
    DiskInvertedIndex index;
    
    public QueryProcessor(DiskInvertedIndex diskIndex){
        index = diskIndex;
    }
    
    /**
     * Processes the query and divides the query on OR (+)
     * 
     * @param userQuery Query entered by the user
     * @return Hash set of the document id
     */
    public int[] queryProcessing(String userQuery){
        int[] resultSet = null;
        
        Pattern orProcessing = Pattern.compile("(.*)\\+(.*)");   
        
        if(orProcessing.matcher(userQuery).find()){
            String[] orTerms = userQuery.split("\\+");
            int count = 0;
            for(String queryterm : orTerms){
                int[] tempResultSet = parseQueryPart(queryterm);
                if( tempResultSet != null ){
                    if(count == 0){
                        resultSet = tempResultSet;
                        count++;
                        continue;
                    }
                    resultSet = SearchEngineUtilities.arrayUnion(resultSet,tempResultSet);
                }
            }
        }
        else{
            resultSet = parseQueryPart(userQuery);
        }
        
        return resultSet;
    }
    
    /**
     * Processes each part of the OR query which can be a single term or a 
     * phrase
     * 
     * @param userQuery Query entered by the user
     * @return Hash set of the document id
     */
    public int[] parseQueryPart(String userQuery){
        userQuery = userQuery.trim();
        String[] termParts = userQuery.split(" ");
        List<String> searchTermParts = new ArrayList<>();
        Pattern startingQuotes = Pattern.compile("^\"");
        Pattern endingQuotes = Pattern.compile("\"$");
        
        for(int i = 0; i < termParts.length ; i++){
            if(startingQuotes.matcher(termParts[i]).find()){
                String phraseQuery = termParts[i];
                while(!endingQuotes.matcher(termParts[i]).find()){
                    phraseQuery = phraseQuery + " " + termParts[i+1];
                    i++;
                }
                
                searchTermParts.add(phraseQuery);
                i++;
                
                if(i == termParts.length)
                    break;
            }
            searchTermParts.add(termParts[i]);
        }
        
        int index = 0;
        
        int[] resultSet = null;
        
        if(searchTermParts.size() > 1){
            while(index < searchTermParts.size() - 1){
                if(index == 0){
                    resultSet = searchVocab(searchTermParts.get(index));
                }

                int[] docIdSet1 = searchVocab(searchTermParts.get(index + 1));
                
                if(resultSet != null && docIdSet1 != null){
                    resultSet = SearchEngineUtilities.arrayIntersection
                                                        (resultSet, docIdSet1);
                    
                    if(resultSet.length == 1 && resultSet[0] == -1)
                        return null;
                    
                    index++;
                }
                else
                    return null;
            }
        }
        else{
            resultSet = searchVocab(searchTermParts.get(0));
        }
        return resultSet;
    }
    
    /**
     * Search for the phrase and the terms and also takes care of the NOT 
     * query
     * 
     * @param term Term to be searched in the positional inverted index
     * @return Hash set of the document id
     */
    public int[] searchVocab(String term){
        if(term.split(" ").length > 1){
            return SearchEngineUtilities.convertListToArray(searchPhrase(term));
        }
        else{
            boolean notQuery = false;
            Pattern notTerm = Pattern.compile("^-(.*)");
            if(notTerm.matcher(term).find()){
                notQuery = true;
            }
            
            ArrayList<PositionalPostingsStructure> temp = searchTerm(term, false);
            if(temp == null)
                return null;
                       
            int[] docIdSet = 
                    SearchEngineUtilities.convertListToArray(temp);
            int listSize = docIdSet.length;
            
            int[] resultDocIds = new int[index.getFileNames().size()-listSize];
            
            int count = 0;
            int resultCounter = 0;
            if(notQuery){
                for(int i=0;i<index.getFileNames().size();i++){
                    if(listSize > 0 && count < listSize){
                        if(docIdSet[count] == i){
                            count = count+1;
                            continue;
                        }
                    }
                    resultDocIds[resultCounter] = i;
                    resultCounter += 1;
                }
                return resultDocIds;
            }
            return docIdSet;
        }
    }
    
    /**
     * Search the phrase in the positional inverted index
     * 
     * @param phraseQuery Query entered by the user
     * @return Postings list for the phrase
     */
    public List<PositionalPostingsStructure> searchPhrase(String phraseQuery){
        List<PositionalPostingsStructure> list1;
        List<PositionalPostingsStructure> list2;
        List<PositionalPostingsStructure> resultList = new ArrayList<>();
        String terms[] = phraseQuery.split(" ");
        for(int i = 0; i < (terms.length - 1); i++){
            if( i == 0 ){
                list1 = searchTerm(terms[i], true);
            }
            else{
                list1 = resultList;
            }
            list2 = searchTerm(terms[i+1], true);
            if(list1 == null || list2 == null){
                resultList = null;
                break;
            }
                
            resultList = SearchEngineUtilities.positionalSearch(list1, list2);
        }
        
        return resultList;
    }
    
    /**
     * Search the term in the positional inverted index
     * 
     * @param term Term to be searched
     * @return Postings list for the term
     */
    public ArrayList<PositionalPostingsStructure> searchTerm(String term, 
            boolean getPositions){
        term = term.replaceAll("^\\W+|\\W+$","");
        term = PorterStemmer.processToken(term);

        //Searching the term in PositionalInvertedIndex
        ArrayList<PositionalPostingsStructure> termPostingsList = 
                index.getPostingsBooleanMode(term, getPositions);
        return termPostingsList;
    }
}
