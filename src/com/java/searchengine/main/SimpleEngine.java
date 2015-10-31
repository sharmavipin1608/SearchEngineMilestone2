package com.java.searchengine.main;


import com.java.searchengine.index.NaiveInvertedIndex;
import com.java.searchengine.index.PositionalInvertedIndex;
import com.java.searchengine.datastructure.PositionalPostingsStructure;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 *
 * @author vipinsharma
 */
public class SimpleEngine {

    /**
     *
     * @param args
     */
    public static void main(String[] args){
        
        final Path currentWorkingPath = Paths.get("").toAbsolutePath();

        // the inverted index
        final NaiveInvertedIndex index = new NaiveInvertedIndex();

        // the list of file names that were processed
        final List<String> fileNames = new ArrayList<String>();
        
        //positional inverted index
        PositionalInvertedIndex posIndex = new PositionalInvertedIndex();

        IndexBuilderFactory.createIndex(currentWorkingPath);
        
        posIndex = IndexBuilderFactory.getInstance().getPositionalIndex();
        
        posIndex.printResults();
        
        while(true) {
            System.out.print("\n\nEnter a term to search for : ");
            Scanner userInput = new Scanner(System.in);
            String input = userInput.nextLine();
            
            if(input.equalsIgnoreCase("quit"))
                break;
                
            List<PositionalPostingsStructure> termPostingsList = 
                    IndexBuilderFactory.getInstance().searchTerm(input);
            if(termPostingsList != null){
//                Integer[] docIdArray = postingsMap.keySet().toArray(new Integer[postingsMap.keySet().size()]);
//                Arrays.sort(docIdArray);
            
//                for(Integer docId : docIdArray){
//                    List<Integer> posIndexes = postingsMap.get(docId);
//                    System.out.print("\n" + fileNames.get(docId) + " -> ");
//                    for(Integer positionIndex : posIndexes){
//                        System.out.print(positionIndex + " , ");
//                    }
//                }
                for(PositionalPostingsStructure posStructure : termPostingsList){
                    posStructure.printData(0);
                    System.out.print("\n");
                }
            }
            else               
                System.out.println("This term is not present "
                        + "in any of the documents");
        }
        System.out.println("Bye...");
    }

}