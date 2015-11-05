package com.java.searchengine.util;

import com.java.searchengine.datastructure.PositionalPostingsStructure;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Utility file for search engine application
 */
public class SearchEngineUtilities {

    /**
     * Prints spaces to format the output
     * @param spaces Number of spaces to be printed
     */
    public static void printSpaces(int spaces) {
        for (int i = 0; i < spaces; i++) {
            System.out.print(" ");
        }
    }
      
    /**
     * Performs positional search while searching for the phrase in positional 
     * inverted index.
     * 
     * @param list1 Postings list for term 1
     * @param list2 Postings list for term 2
     * @return list of postings after performing positional search on 2 terms
     */
        
    public static List<PositionalPostingsStructure> positionalSearch(
        List<PositionalPostingsStructure> list1, List<PositionalPostingsStructure> list2){
        int pointer1 = 0, pointer2 = 0;
        List<PositionalPostingsStructure> resultList = 
                new ArrayList<>();
        
        while(true){
            int docId1 = list1.get(pointer1).getDocumentId();
            int docId2 = list2.get(pointer2).getDocumentId();
            
            if(docId1 == docId2){
                boolean flag = compareList(list1.get(pointer1).getPositionList(),
                        list2.get(pointer2).getPositionList());
                if(flag)
                    resultList.add(list2.get(pointer2));
                pointer1++;
                pointer2++;
            }
            else if(docId1 > docId2){
                pointer2++;
            }
            else if(docId1 < docId2){
                pointer1++;
            }
            if(pointer1 >= list1.size() || pointer2 >= list2.size())    
                break;
        }
        return resultList;
    }
    
    /**
     * Compares 2 lists to search list2 for the presence of a number 1 greater than
     * number in list1
     * 
     * @param list1 list of postings in a particular document
     * @param list2 list of postings in a particular document
     * @return 
     */
    public static boolean compareList(List<Integer> list1, List<Integer> list2){
        int pointer1 = 0, pointer2 = 0;
        
        while(true){
            int position1 = list1.get(pointer1);
            int position2 = list2.get(pointer2);

            if(position2 == position1+1){
                return true;
            }
            else if(position2 > position1+1){
                pointer1++;
            }
            else if(position2 < position1+1){
                pointer2++;
            }
            
            if(pointer1 == list1.size() || pointer2 == list2.size())    
                break;
        }
        return false;
    }
    
    /**
     * Returns a set of document id from postings list
     * 
     * @param list
     * @return hash set of document id
     */
    public static HashSet<Integer> convertListToSet(List<PositionalPostingsStructure> list){
        if(list != null){
            HashSet<Integer> documentSet = new HashSet<Integer>();
            for(PositionalPostingsStructure positionalPosting : list){
                documentSet.add(positionalPosting.getDocumentId());
            }
            return documentSet;
        }
        else
            return null;
    }
    
    public static int[] convertListToArray(List<PositionalPostingsStructure> list){
        if(list != null){
            int listSize = list.size();
            int[] docId = new int[listSize];
            for(int i=0;i<listSize;i++){
                docId[i] = list.get(i).getDocumentId();
            }
            return docId;
        }
        else{
            return null;
        }
    }
    
    public static int[] arrayIntersection(int[] arr1, int[] arr2){
        int arr1size = arr1.length;
        int arr2size = arr2.length;
        
        int pointer1 = 0, pointer2 = 0;
        String str = "";
        
        while(true){
            int value1 = arr1[pointer1];
            int value2 = arr2[pointer2];
            
            if(value1 == value2){
                if(str.equals("")){
                    str += value1;
                }
                else
                    str = str + "," + value1;
                
                pointer1++;
                pointer2++;
            }
            else if (value1 < value2){
                pointer1++;
            }
            else if(value1 > value2){
                pointer2++;
            }
            
            if(pointer1 == arr1size || pointer2 == arr2size)
                break;
        }
        
        String[] tempArr = str.split(",");
        int[] resultArray = new int[tempArr.length];
        
        for(int i = 0; i < tempArr.length; i++){
            if(tempArr[i].equals("")){
                resultArray[i] = -1;
                continue;
            }
            resultArray[i] = Integer.parseInt(tempArr[i]);
        }
        return resultArray;
    }
    
    public static int[] arrayUnion(int[] arr1, int[] arr2){
        int arr1size = arr1.length;
        int arr2size = arr2.length;
        
        int pointer1 = 0, pointer2 = 0;
        int count = 0;
        String str = "";
        
        while(true){
            int value1 = arr1[pointer1];
            int value2 = arr2[pointer2];
            
            if(value1 == value2){
                str = str + "," + value1;
                
                pointer1++;
                pointer2++;
            }
            else if (value1 < value2){
                str = str + "," + value1;
                pointer1++;
            }
            else if(value1 > value2){
                str = str + "," + value2;
                pointer2++;
            }
            
            if(pointer1 == arr1size){
                while(pointer2 < arr2size){
                    value2 = arr2[pointer2];
                    str = str + "," + value2;
                    pointer2++;
                }
                break;
            }
            
            if(pointer2 == arr2size){
                while(pointer1 < arr1size){
                    value1 = arr1[pointer1];
                    str = str + "," + value1;
                    pointer1++;
                }
                break;
            }
        }
        
        str = str.replaceAll("^\\W+|\\W+$","");
        String[] tempArr = str.split(",");
        int[] resultArray = new int[tempArr.length];
        
        System.out.println("result string OR = " + str);
        
        for(int i = 0; i < tempArr.length; i++){
            resultArray[i] = Integer.parseInt(tempArr[i]);
        }
        
        return resultArray;
    }
}
