package com.java.searchengine.main;


import com.java.searchengine.index.PositionalInvertedIndex;
import com.java.searchengine.datastructure.PositionalPostingsStructure;
import com.java.searchengine.util.SearchEngineUtilities;
import com.java.searchengine.util.SimpleTokenStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Singleton class which is initializing the positional inverted index
 * 
 */
public class IndexBuilderFactory {
    private static PositionalInvertedIndex posIndex = new PositionalInvertedIndex();
    private static IndexBuilderFactory indexBuilderFactory = null;
    private List<String> fileNames = new ArrayList<>();
    
    /**
     * Private constructor which is being called from createIndex().
     * It will index all the '.txt' files present in the 
     * directory path passed by the user
     * 
     * @param currentWorkingPath Path of the directory to index
     */
    private IndexBuilderFactory( Path currentWorkingPath ){
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        System.out.println("\nIndexing of files started at : " 
                + sdf.format(cal.getTime()) );
        
        // This is our standard "walk through all .txt files" code.
        try{
            Files.walkFileTree(currentWorkingPath, new SimpleFileVisitor<Path>() {
                int mDocumentID = 0;

                public FileVisitResult preVisitDirectory(Path dir,
                        BasicFileAttributes attrs) {
                    
                    // make sure we only process the current working directory
                    if (currentWorkingPath.equals(dir)) {
                        return FileVisitResult.CONTINUE;
                    }
                    return FileVisitResult.SKIP_SUBTREE;
                }

                public FileVisitResult visitFile(Path file,
                        BasicFileAttributes attrs) {
                    
                    // only process .txt files
                    if (file.toString().endsWith(".txt")) {
                        // we have found a .txt file; add its name to the fileName list,
                        // then index the file and increase the document ID counter.
                        fileNames.add(file.getFileName().toString());
                        indexFile(file.toFile(), mDocumentID);
                        mDocumentID++;
                    }
                    return FileVisitResult.CONTINUE;
                }

                // don't throw exceptions if files are locked/other errors occur
                public FileVisitResult visitFileFailed(Path file,
                        IOException e) {
                    return FileVisitResult.CONTINUE;
                }
                
                //call this method to calculate the indices
                //make the call asynchronous 
                public FileVisitResult postVisitDirectory(Path dir,
                                                      IOException exc) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try{
                                Thread.sleep(10);
                                posIndex.calculateMetrics();
                            }
                            catch(Exception ex){
                                System.out.println("Thread issues");
                            }
                        }    
                    }).start();
                    
                    return FileVisitResult.CONTINUE;
                }
            });
        }
        catch(IOException ex){
            System.out.println("Exception : " + ex.getMessage());
        }
        
        Calendar cal1 = Calendar.getInstance();
        SimpleDateFormat sdf1 = new SimpleDateFormat("HH:mm:ss");
        System.out.println("Indexing of files ended at : " +
                sdf1.format(cal1.getTime()) );
    }
    
    /**
     * Returns the instance of the IndexBuilderFactory
     * 
     * @return
     */
    public static IndexBuilderFactory getInstance(){
        return indexBuilderFactory;
    }
    
    /**
     * Get file name for a particular document id
     * 
     * @param docId Integer ID of the document
     * @return
     */
    public String getFilesNames(int docId){
        return fileNames.get(docId);
    }
    
    /**
     * Initializes the object of IndexBuilderFactory and is called just once
     * from the main method at the beginning of the execution
     * 
     * @param currentWorkingPath Path of the directory to index
     */
    public static void createIndex(Path currentWorkingPath){
        indexBuilderFactory = new IndexBuilderFactory(currentWorkingPath);
    }
    
    /**
     * Returns the positional inverted index 
     * 
     * @return
     */
    public PositionalInvertedIndex getPositionalIndex(){
        return posIndex;
    }
    
    /**
     * Returns the document names in hash set which is needed for calculation
     * of intersection and union of sets
     * 
     * @return
     */
    public HashSet<Integer> fileNamesHashSet(){
        HashSet<Integer> fileSet = new HashSet<>();
        for(int i = 0; i < fileNames.size(); i++){
            fileSet.add(i);
        }
        return fileSet;
    }
    /**
     * Indexes a file by reading a series of tokens from the file, treating each
     * token as a term, and then adding the given document's ID to the inverted
     * index for the term.
     *
     * @param file a File object for the document to index.
     * @param docID the integer ID of the current document, needed when indexing
     * each term from the document.
     */
    private static void indexFile(File file, int docID) {
        // Construct a SimpleTokenStream for the given File.
        // Read each token from the stream and add it to the index.
        int count = 1;
        String term = null;    
        try {
            SimpleTokenStream tokenStream = new SimpleTokenStream(file);
            while (tokenStream.hasNextToken()) {
                term = tokenStream.nextToken();
                
                //hyphenation
                if(term.contains("-")){
                    String[] multipleTerms = term.split("-");
                    for(String termPart : multipleTerms){
                        if(termPart != ""){
                            stemAndAddToIndex(termPart, docID, count);
                        }
                    }
                    term = term.replaceAll("-", "");
                    stemAndAddToIndex(term, docID, count);
                }
                else{
                    stemAndAddToIndex(term, docID, count);
                }
                count++;
            }
        } catch (Exception ex) {
            //System.out.println("Exception in opening the file" + 
                    //ex.getMessage() + ex.getLocalizedMessage());
        }
    }
    
    /**
     * Stem the term using porter stemmer and add the term and position to the
     * positional inverted index
     * 
     * @param term Term to be added to the index
     * @param docId The integer ID of the current document, needed when indexing
     * @param position Position of the term in the current document 
     */
    private static void stemAndAddToIndex(String term, int docId, int position){
        posIndex.addType(term);
        term = PorterStemmer.processToken(term);
        posIndex.addTerm(term, docId, position);
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
                list1 = searchTerm(terms[i]);
            }
            else{
                list1 = resultList;
            }
            list2 = searchTerm(terms[i+1]);
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
    public List<PositionalPostingsStructure> searchTerm(String term){
        term = term.replaceAll("^\\W+|\\W+$","");
        term = PorterStemmer.processToken(term);

        //Searching the term in PositionalInvertedIndex
        List<PositionalPostingsStructure> termPostingsList = posIndex.getPostings(term);
        return termPostingsList;
    }
    
    /**
     * Processes the query and divides the query on OR (+)
     * 
     * @param userQuery Query entered by the user
     * @return Hash set of the document id
     */
    public HashSet<Integer> queryProcessing(String userQuery){
        HashSet<Integer> resultSet = new HashSet<>();
        
        Pattern orProcessing = Pattern.compile("(.*)\\+(.*)");   
        
        if(orProcessing.matcher(userQuery).find()){
            String[] orTerms = userQuery.split("\\+");
            int count = 0;
            for(String queryterm : orTerms){
                HashSet<Integer> tempResultSet = parseQueryPart(queryterm);
                if( tempResultSet != null )
                    resultSet.addAll(parseQueryPart(queryterm));
            }
        }
        else{
            HashSet<Integer> tempResultSet = parseQueryPart(userQuery);
            if( tempResultSet != null )
                resultSet.addAll(parseQueryPart(userQuery));
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
    public HashSet<Integer> parseQueryPart(String userQuery){
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
        
        HashSet<Integer> resultSet = new HashSet<>();
        
        if(searchTermParts.size() > 1){
            while(index < searchTermParts.size() - 1){
                if(index == 0){
                    resultSet = searchVocab(searchTermParts.get(index));
                }

                HashSet<Integer> docIdSet1 = searchVocab(searchTermParts.get(index + 1));
                
                if(resultSet != null && docIdSet1 != null){
                    resultSet.retainAll(docIdSet1);
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
    public HashSet<Integer> searchVocab(String term){
        if(term.split(" ").length > 1){
            return SearchEngineUtilities.convertListToSet(searchPhrase(term));
        }
        else{
            boolean notQuery = false;
            Pattern notTerm = Pattern.compile("^-(.*)");
            if(notTerm.matcher(term).find()){
                notQuery = true;
            }
            
            HashSet<Integer> docIdSet = SearchEngineUtilities.convertListToSet(searchTerm(term));
            HashSet<Integer> fileNameSet = fileNamesHashSet();
            if(notQuery){
                if(docIdSet != null)
                    fileNameSet.removeAll(docIdSet);
                return fileNameSet;
            }
            return docIdSet;
        }
    }
}
