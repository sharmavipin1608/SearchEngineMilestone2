package com.java.searchengine.main;

import com.java.searchengine.datastructure.PositionalPostingsStructure;
import com.java.searchengine.index.PositionalInvertedIndex;
import com.java.searchengine.util.SimpleTokenStream;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Writes an inverted indexing of a directory to disk.
 */
public class IndexWriter {

    private String mFolderPath;
    
    private static int numberOfDocuments;
    
    private double[] averageTermFreq;
    
    private double[] documentWeights;

    /**
     * Constructs an IndexWriter object which is prepared to index the given
     * folder.
     */
    public IndexWriter(String folderPath) {
        mFolderPath = folderPath;
        numberOfDocuments = 0;
    }

    /**
     * Builds and writes an inverted index to disk. Creates three files:
     * vocab.bin, containing the vocabulary of the corpus; postings.bin,
     * containing the postings list of document IDs; vocabTable.bin, containing
     * a table that maps vocab terms to postings locations
     */
    public void buildIndex() {
        buildIndexForDirectory(mFolderPath);
    }

    /**
     * Builds the normal NaiveInvertedIndex for the folder.
     */
    private static void buildIndexForDirectory(String folder) {
        PositionalInvertedIndex index = new PositionalInvertedIndex();

        // Index the directory using a naive index
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        System.out.println("\nIndexing of files started at : " 
                + sdf.format(cal.getTime()) );
        indexFiles(folder, index);
        Calendar cal1 = Calendar.getInstance();
        SimpleDateFormat sdf1 = new SimpleDateFormat("HH:mm:ss");
        System.out.println("\nIndexing of files ended at : " 
                + sdf.format(cal1.getTime()) );
        
			// at this point, "index" contains the in-memory inverted index 
        // now we save the index to disk, building three files: the postings index,
        // the vocabulary list, and the vocabulary table.
        // the array of terms
        String[] dictionary = index.getDictionary();
        // an array of positions in the vocabulary file
        long[] vocabPositions = new long[dictionary.length];
        double[] averageTermFreq = new double[numberOfDocuments+1];
        
        Calendar cal2 = Calendar.getInstance();
        SimpleDateFormat sdf2 = new SimpleDateFormat("HH:mm:ss");
        System.out.println("\nWriting vocab file start : " 
                + sdf2.format(cal2.getTime()) );
        buildVocabFile(folder, dictionary, vocabPositions);
        Calendar cal3 = Calendar.getInstance();
        SimpleDateFormat sdf3 = new SimpleDateFormat("HH:mm:ss");
        System.out.println("\nWriting vocab file ends : " 
                + sdf3.format(cal3.getTime()) );
        
        Calendar cal4 = Calendar.getInstance();
        SimpleDateFormat sdf4 = new SimpleDateFormat("HH:mm:ss");
        System.out.println("\nWriting doc weight file start : " 
                + sdf4.format(cal4.getTime()) );
        double[] documentWeights = buildDocWeightsFile(folder,index,averageTermFreq);
        Calendar cal5 = Calendar.getInstance();
        SimpleDateFormat sdf5 = new SimpleDateFormat("HH:mm:ss");
        System.out.println("\nWriting doc weight file ends : " 
                + sdf5.format(cal5.getTime()) );
        
        Calendar cal6 = Calendar.getInstance();
        SimpleDateFormat sdf6 = new SimpleDateFormat("HH:mm:ss");
        System.out.println("\nWriting postings file start : " 
                + sdf6.format(cal6.getTime()) );
        buildPostingsFile(folder, index, dictionary, vocabPositions, averageTermFreq, documentWeights);
        Calendar cal7 = Calendar.getInstance();
        SimpleDateFormat sdf7 = new SimpleDateFormat("HH:mm:ss");
        System.out.println("\nWriting postings file end : " 
                + sdf7.format(cal7
                        
                        
                        
                        
                        
                        .getTime()) );
    }

    /**
     * Builds the postings.bin file for the indexed directory, using the given
     * NaiveInvertedIndex of that directory.
     */
    private static void buildPostingsFile(String folder, PositionalInvertedIndex index,
            String[] dictionary, long[] vocabPositions, double[] averageTermFreq,
            double[] documentWeights) {
        FileOutputStream postingsFile = null;
        //folder = "/Users/vipinsharma/NetBeansProjects/SearchEngineHW5/src/diskFiles";
        try {
            postingsFile = new FileOutputStream(
                    new File(folder, "postings.bin")
            );

         // simultaneously build the vocabulary table on disk, mapping a term index to a
            // file location in the postings file.
            FileOutputStream vocabTable = new FileOutputStream(
                    new File(folder, "vocabTable.bin")
            );

            // the first thing we must write to the vocabTable file is the number of vocab terms.
            byte[] tSize = ByteBuffer.allocate(4)
                    .putInt(dictionary.length).array();
            vocabTable.write(tSize, 0, tSize.length);
            int vocabI = 0;
            
            //start 
            double averageDocumentWeight = 0;
            for(int i=0; i<=numberOfDocuments; i++){
                averageDocumentWeight += documentWeights[i];
                System.out.println("Index : " + i + " averageTermFreq : " 
                        + averageTermFreq[i] + " documentWeights : " + documentWeights[i]);
                
            }
            averageDocumentWeight = (averageDocumentWeight/(numberOfDocuments+1));
            System.out.println("averageDocumentWeight : "+averageDocumentWeight);
            //end
            
            for (String s : dictionary) {
                // for each String in dictionary, retrieve its postings.
                List<PositionalPostingsStructure> postings = index.getPostings(s);

            // write the vocab table entry for this term: the byte location of the term in the vocab list file,
                // and the byte location of the postings for the term in the postings file.
                byte[] vPositionBytes = ByteBuffer.allocate(8)
                        .putLong(vocabPositions[vocabI]).array();
                vocabTable.write(vPositionBytes, 0, vPositionBytes.length);

                byte[] pPositionBytes = ByteBuffer.allocate(8)
                        .putLong(postingsFile.getChannel().position()).array();
                vocabTable.write(pPositionBytes, 0, pPositionBytes.length);

            // write the postings file for this term. first, the document frequency for the term, then
                // the document IDs, encoded as gaps.
                byte[] docFreqBytes = ByteBuffer.allocate(4)
                        .putInt(postings.size()).array();
                postingsFile.write(docFreqBytes, 0, docFreqBytes.length);

                int lastDocId = 0;
                for (PositionalPostingsStructure posStruct : postings) {
                    //System.out.println("sending term frequency to memory : " + posStruct.getTermFrequency() + 
                            //" list size : "+ posStruct.getPositionList().size());
                    int docId = posStruct.getDocumentId();
                    byte[] docIdBytes = ByteBuffer.allocate(4)
                            .putInt(docId - lastDocId).array(); // encode a gap, not a doc ID
                    postingsFile.write(docIdBytes, 0, docIdBytes.length);
                    
                    //TODO: read the positions and encode gaps into the files
                    //Start - Writing term frequency of the term in document on file
                    List<Integer> positionPostings = posStruct.getPositionList();
                    
                    //storing document weights on file
                    for(int i=0; i<4; i++){
                        IVariableTermFrequency variableTermFrequency = VariableTermFreqFactory.getWeightingScheme(i+1);
                        double docTermWght = variableTermFrequency.documentTermWeight
                            (positionPostings.size(), averageTermFreq[docId], documentWeights[docId],
                                    averageDocumentWeight, docId);
                        //System.out.println("term : " + s + " doc ID : " + docId + " docTermWght : " + docTermWght);
                        byte[] docTermWghtBytes = ByteBuffer.allocate(8)
                                .putDouble(docTermWght).array();
                        postingsFile.write(docTermWghtBytes, 0, docTermWghtBytes.length);
                    }
                    //end
                    
                    byte[] termFreqBytes = ByteBuffer.allocate(4)
                        .putInt(positionPostings.size()).array();
                    postingsFile.write(termFreqBytes, 0, termFreqBytes.length);
                    int lastPosition = 0;
                    for(int position : positionPostings){
                        byte[] positionBytes = ByteBuffer.allocate(4)
                            .putInt(position - lastPosition).array(); // encode a gap, not a doc ID
                        postingsFile.write(positionBytes, 0, positionBytes.length);
                        lastPosition = position;
                    }
                    //End 
                    
                    lastDocId = docId;
                }

                vocabI++;
            }
            vocabTable.close();
            postingsFile.close();
        } catch (FileNotFoundException ex) {
        } catch (IOException ex) {
        } finally {
            try {
                postingsFile.close();
            } catch (IOException ex) {
            }
        }
    }

    private static void buildVocabFile(String folder, String[] dictionary,
            long[] vocabPositions) {
        OutputStreamWriter vocabList = null;
        try {
         // first build the vocabulary list: a file of each vocab word concatenated together.
            // also build an array associating each term with its byte location in this file.
            int vocabI = 0;
            //folder = "/Users/vipinsharma/NetBeansProjects/SearchEngineHW5/src/diskFiles";
            vocabList = new OutputStreamWriter(
                    new FileOutputStream(new File(folder, "vocab.bin")), "ASCII"
            );

            int vocabPos = 0;
            for (String vocabWord : dictionary) {
                // for each String in dictionary, save the byte position where that term will start in the vocab file.
                vocabPositions[vocabI] = vocabPos;
                vocabList.write(vocabWord); // then write the String
                vocabI++;
                vocabPos += vocabWord.length();
                //System.out.println(vocabWord + " = " + vocabPos);
            }
        } catch (FileNotFoundException ex) {
            System.out.println(ex.toString());
        } catch (UnsupportedEncodingException ex) {
            System.out.println(ex.toString());
        } catch (IOException ex) {
            System.out.println(ex.toString());
        } finally {
            try {
                vocabList.close();
            } catch (IOException ex) {
                System.out.println(ex.toString());
            }
        }
    }

    private static void indexFiles(String folder, final PositionalInvertedIndex index) {
        int documentID = 0;
        final Path currentWorkingPath = Paths.get(folder).toAbsolutePath();

        try {
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
                        // System.out.println("Indexing file " + file.getFileName());

                        indexFile(file.toFile(), index, mDocumentID);
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
                /*public FileVisitResult postVisitDirectory(Path dir,
                                                      IOException exc) {
                    System.out.println("postVisitDirectory");
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try{
                                Thread.sleep(10);
                                calculateDocumentWeights(index);
                            }
                            catch(Exception ex){
                                System.out.println("Thread issues");
                            }
                        }    
                    }).start();
                    
                    return FileVisitResult.CONTINUE;
                }*/

            });
        } catch (IOException ex) {
            Logger.getLogger(IndexWriter.class.getName()).log(Level.SEVERE, null, ex);
        }
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
    private static void indexFile(File fileName, PositionalInvertedIndex index,
            int documentID) {

//      try {
//         SimpleTokenStream stream = new SimpleTokenStream(fileName);
//         while (stream.hasNextToken()) {
//            String term = stream.nextToken();
//            System.out.println("term : " + term);
//            String stemmed = PorterStemmer.processToken(term);
//            System.out.println("stemmed : " + stemmed);
//            if (stemmed != null && stemmed.length() > 0) {
//               index.addTerm(stemmed, documentID);
//            }
//         }
//      }
//      catch (Exception ex) {
//         System.out.println("indexFile" + ex.toString());
//      }
        int count = 1;
        String term = null;
        try {
            SimpleTokenStream tokenStream = new SimpleTokenStream(fileName);
            while (tokenStream.hasNextToken()) {
                term = tokenStream.nextToken();

                //hyphenation
                if (term.contains("-")) {
                    String[] multipleTerms = term.split("-");
                    for (String termPart : multipleTerms) {
                        if (termPart != "") {
                            stemAndAddToIndex(termPart, index,
                                    documentID, count);
                        }
                    }
                    term = term.replaceAll("-", "");
                    stemAndAddToIndex(term, index, documentID, count);
                } else {
                    stemAndAddToIndex(term, index, documentID, count);
                }
                count++;
            }
            numberOfDocuments = numberOfDocuments > documentID ? numberOfDocuments : documentID;
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
    private static void stemAndAddToIndex(String term, PositionalInvertedIndex index,
            int docId, int position) {
        index.addType(term);
        term = PorterStemmer.processToken(term);
        index.addTerm(term, docId, position);
    }
    
    private static double[] calculateDocumentWeights(PositionalInvertedIndex index, double[] averageTermFreq){
        double[] documentWeights = new double[numberOfDocuments+1];
        double[] numOfTerms = new double[numberOfDocuments+1];
        double[] freqSum = new double[numberOfDocuments+1];
        
        System.out.println("--------calculateDocumentWeights-----------");
        System.out.println("num of docs" + numberOfDocuments);
        for(String term : index.getDictionary()){
            List<PositionalPostingsStructure> postings = index.getPostings(term);
            for(PositionalPostingsStructure posStruct : postings){
                double termWeight = 
                        Math.pow((1 + Math.log(posStruct.getTermFrequency())),2);
                documentWeights[posStruct.getDocumentId()] += termWeight;
                
                freqSum[posStruct.getDocumentId()] += posStruct.getTermFrequency();
                numOfTerms[posStruct.getDocumentId()] += 1;
                
//                System.out.println("term : " + term + " freq : " + posStruct.getTermFrequency()
//                    + " docid : " + posStruct.getDocumentId() + " termWeight : " + termWeight);
            }
        }
        
        for(int i = 0; i <= numberOfDocuments; i++){ 
            //System.out.println("pre weight : " + documentWeights[i]);
            documentWeights[i] = Math.sqrt(documentWeights[i]);
            
            averageTermFreq[i] = freqSum[i]/numOfTerms[i];
        }
        
        for(double weight : documentWeights){
            //System.out.println("post weight : " + weight);
            //weight = Math.sqrt(weight);
        }
        return documentWeights;
    }
    
    private static double[] buildDocWeightsFile(String folder, 
            PositionalInvertedIndex index,double[] averageTermFreq) {
        FileOutputStream docWeightsFile = null;
        
        double[] documentWeights = calculateDocumentWeights(index,averageTermFreq);
        
        for(int i = 0; i <= numberOfDocuments; i++){ 
            //System.out.println("averageTermFreq"+ averageTermFreq[i]);
        }
        
        try {
            docWeightsFile = new FileOutputStream(new File(folder, "docWeights.bin"));
            
//            for(double weight : documentWeights){
//                byte[] tSize = ByteBuffer.allocate(8)
//                    .putDouble(weight).array();
//                docWeightsFile.write(tSize, 0, tSize.length);
//            }
            for(int i=0; i<documentWeights.length; i++){
                byte[] dcumentWeight = ByteBuffer.allocate(8)
                    .putDouble(documentWeights[i]).array();
                docWeightsFile.write(dcumentWeight, 0, dcumentWeight.length);
                
                byte[] avgTermFreq = ByteBuffer.allocate(8)
                    .putDouble(averageTermFreq[i]).array();
                docWeightsFile.write(avgTermFreq, 0, avgTermFreq.length);
            }
            docWeightsFile.close();
        } catch (FileNotFoundException ex) {
        } catch (IOException ex) {
        } finally {
            try {
                docWeightsFile.close();
            } catch (IOException ex) {
            }
        }
        return documentWeights;
    }
}
