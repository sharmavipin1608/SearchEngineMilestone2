package com.java.searchengine.main;

import com.java.searchengine.datastructure.PositionalPostingsStructure;
import com.java.searchengine.util.SearchEngineUtilities;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

/**
 * Reading .bin files from disk and return the data relevant to the query
 */
public class DiskInvertedIndex {

    public String mPath;
    private RandomAccessFile mVocabList;
    private RandomAccessFile mPostings;
    private long[] mVocabTable;
    private List<String> mFileNames;

    /**
     * Constructor to initialize the path of the .bin files
     * 
     * @param path - path of the corpus
     */
    public DiskInvertedIndex(String path) {
        try {
            mPath = path;
            mVocabList = new RandomAccessFile(new File(path, "vocab.bin"), "r");
            mPostings = new RandomAccessFile(new File(path, "postings.bin"), "r");
            mVocabTable = readVocabTable(path);
            mFileNames = readFileNames(path);
        } catch (FileNotFoundException ex) {
            System.out.println(ex.toString());
        }
    }

    private ArrayList<PositionalPostingsStructure>
            readPostingsFromFile(RandomAccessFile postings, long postingsPosition,
                    int weighingScheme, boolean readPositions) {
        try {
            // seek to the position in the file where the postings start.
            postings.seek(postingsPosition);

            // read the 4 bytes for the document frequency
            byte[] buffer = new byte[4];
            postings.read(buffer, 0, buffer.length);

            // use ByteBuffer to convert the 4 bytes into an int.
            int documentFrequency = ByteBuffer.wrap(buffer).getInt();

            // initialize the array that will hold the postings. 
            ArrayList<PositionalPostingsStructure> posStruct
                    = new ArrayList<>(documentFrequency);

         // write the following code:
            // read 4 bytes at a time from the file, until you have read as many
            //    postings as the document frequency promised.
            //    
            // after each read, convert the bytes to an int posting. this value
            //    is the GAP since the last posting. decode the document ID from
            //    the gap and put it in the array.
            //
            // repeat until all postings are read.
            int docId = 0;
            for (int i = 0; i < documentFrequency; i++) {
                double docWeight = 0;
                postings.read(buffer, 0, buffer.length);
                int lastDoc = ByteBuffer.wrap(buffer).getInt();
                docId += lastDoc;
                //System.out.println("doc ID" + docId + " last doc : "+lastDoc);
                //postings.seek(postingsPosition+40);
                
                //start - modifications for masking pre-computed logs
//                if(weighingScheme > 0){
//                    byte[] docWghtBuffer = new byte[8];
//                    int index = (weighingScheme-1)*8;
//                    postings.skipBytes(index);
//                    postings.read(docWghtBuffer, 0, docWghtBuffer.length);
//                    docWeight = ByteBuffer.wrap(docWghtBuffer).getDouble();
//                    postings.skipBytes(32 - (8 * weighingScheme)); 
//                }
//                else{
                    postings.skipBytes(32);
//                }
                    
                //end - modifications for masking pre-computed logs
                
                //read positions
                postings.read(buffer, 0, buffer.length);
                int termFrequency = ByteBuffer.wrap(buffer).getInt();
                //System.out.println("term freq : " + termFrequency);
                
                //start - modifications
                IVariableTermFrequency scheme = VariableTermFreqFactory.getWeightingScheme(weighingScheme);
                double avgTermFreqForDoc;
                avgTermFreqForDoc = getAvgTermFreqForDoc(docId);
                double documentWeight = 0;
                double avgDocWght = 0;
                
                if(weighingScheme == 3){
                    documentWeight = getDocumentWeights(docId);
                    for(int z=0;z<mFileNames.size();z++)
                        avgDocWght += getDocumentWeights(z);
                    avgDocWght = avgDocWght/mFileNames.size();
                }
                docWeight = scheme.documentTermWeight(termFrequency,avgTermFreqForDoc,documentWeight,avgDocWght,0);
                //end modifications
                
                ArrayList<Integer> positionList = null;
                if(readPositions){
                    positionList = new ArrayList<>(termFrequency);
                    int position = 0;
                    for (int j = 0; j < termFrequency; j++) {
                        postings.read(buffer, 0, buffer.length);
                        position += ByteBuffer.wrap(buffer).getInt();
                        positionList.add(position);
                    }
                }
                else{
                    postings.skipBytes(4 * termFrequency);
                }
                
                //posStruct.add();
                posStruct.add(i, new PositionalPostingsStructure(docId, 
                        positionList, termFrequency, docWeight));
                //end
            }
            return posStruct;
        } catch (IOException ex) {
            System.out.println(ex.toString());
        }
        return null;
    }

    /**
     * Read postings from disk
     * 
     * @param term
     * @param readPositions - boolean to check if positions have to be read from index
     * @param weighingScheme - weighing scheme selected by user
     * 
     * @return array list of postings for the term
     */
    public ArrayList<PositionalPostingsStructure> GetPostings(String term, 
            boolean readPositions, int weighingScheme) {
        long postingsPosition = binarySearchVocabulary(term);
        if (postingsPosition >= 0) {
            return readPostingsFromFile(mPostings, postingsPosition,
                    weighingScheme, readPositions);
        }
        return null;
    }
    
    /**
     * Read postings from disk for boolean mode queries
     * 
     * @param term
     * @param readPositions - boolean to check if positions have to be read from index
     * 
     * @return array list of postings for the term
     */
    public ArrayList<PositionalPostingsStructure> getPostingsBooleanMode(
            String term, boolean readPositions) {
        return GetPostings(term,readPositions,0);
    }
    
    /**
     * Read postings from disk for ranked mode queries
     * 
     * @param term
     * @param weighingScheme - weighing scheme selected by user
     * 
     * @return array list of postings for the term
     */
    public ArrayList<PositionalPostingsStructure> getPostingsRankedMode(
            String term, int weighingScheme) {
        return GetPostings(term,false,weighingScheme);
    }

    private long binarySearchVocabulary(String term) {
        // do a binary search over the vocabulary, using the vocabTable and the file vocabList.
        int i = 0, j = mVocabTable.length / 2 - 1;
        while (i <= j) {
            try {
                int m = (i + j) / 2;
                long vListPosition = mVocabTable[m * 2];
                int termLength;
                if (m == mVocabTable.length / 2 - 1) {
                    termLength = (int) (mVocabList.length() - mVocabTable[m * 2]);
                } else {
                    termLength = (int) (mVocabTable[(m + 1) * 2] - vListPosition);
                }

                mVocabList.seek(vListPosition);

                byte[] buffer = new byte[termLength];
                mVocabList.read(buffer, 0, termLength);
                String fileTerm = new String(buffer, "ASCII");

                int compareValue = term.compareTo(fileTerm);
                if (compareValue == 0) {
                    // found it!
                    return mVocabTable[m * 2 + 1];
                } else if (compareValue < 0) {
                    j = m - 1;
                } else {
                    i = m + 1;
                }
            } catch (IOException ex) {
                System.out.println(ex.toString());
            }
        }
        return -1;
    }

    private static List<String> readFileNames(String indexName) {
        try {
            final List<String> names = new ArrayList<String>();
            final Path currentWorkingPath = Paths.get(indexName).toAbsolutePath();

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
                        names.add(file.toFile().getName());
                    }
                    return FileVisitResult.CONTINUE;
                }

                // don't throw exceptions if files are locked/other errors occur
                public FileVisitResult visitFileFailed(Path file,
                        IOException e) {

                    return FileVisitResult.CONTINUE;
                }

            });
            return names;
        } catch (IOException ex) {
            System.out.println(ex.toString());
        }
        return null;
    }

    /**
     * Read document weights from disk
     * 
     * @param docId - document id to get the weight for
     * 
     * @return document weight for the document
     */
    public double getDocumentWeights(int docId) {
        double weight = 0;
        try {
            RandomAccessFile mDocWeights = new RandomAccessFile(
                    new File(mPath, "docWeights.bin"), "r");
            byte[] byteBuffer = new byte[8];
            //System.out.println("here" + docId);

            mDocWeights.seek(2 * 8 * docId);

            mDocWeights.read(byteBuffer, 0, byteBuffer.length);
            weight = ByteBuffer.wrap(byteBuffer).getDouble();
            mDocWeights.close();
        } catch (Exception ex) {
            System.out.println("Document weights : " + ex.getMessage());
        }
        return weight;
    }

    /**
     * Calculate average term frequency for the document
     * 
     * @param docId - document id
     * 
     * @return - average term frequency for the document
     */
    public double getAvgTermFreqForDoc(int docId){
        double avgTermFreq = 0;
        try {
            RandomAccessFile mDocWeights = new RandomAccessFile(
                    new File(mPath, "docWeights.bin"), "r");
            byte[] byteBuffer = new byte[8];
            //System.out.println("here" + docId);

            mDocWeights.seek(2 * 8 * docId + 8);

            mDocWeights.read(byteBuffer, 0, byteBuffer.length);
            avgTermFreq = ByteBuffer.wrap(byteBuffer).getDouble();

            //System.out.println("avgTermFreq" + avgTermFreq);
            mDocWeights.close();
        } catch (Exception ex) {
            System.out.println(ex.getStackTrace());
        }
        return avgTermFreq;
    }
            
    private static long[] readVocabTable(String indexName) {
        try {
            long[] vocabTable;

            RandomAccessFile tableFile = new RandomAccessFile(
                    new File(indexName, "vocabTable.bin"),
                    "r");

            byte[] byteBuffer = new byte[4];
            tableFile.read(byteBuffer, 0, byteBuffer.length);

            int tableIndex = 0;
            vocabTable = new long[ByteBuffer.wrap(byteBuffer).getInt() * 2];
            byteBuffer = new byte[8];

            while (tableFile.read(byteBuffer, 0, byteBuffer.length) > 0) { // while we keep reading 8 bytes
                vocabTable[tableIndex] = ByteBuffer.wrap(byteBuffer).getLong();
                //System.out.println(new String(byteBuffer,"ASCII"));
                tableIndex++;
            }
            System.out.println("===closing file : === vocabtable");
            tableFile.close();
            return vocabTable;
        } catch (FileNotFoundException ex) {
            System.out.println("DiskInvertedIndex : " + ex.getLocalizedMessage());
            ex.printStackTrace();
        } catch (IOException ex) {
            System.out.println(ex.toString());
        }
        return null;
    }

    /**
     * Get a list of file Names
     * 
     * @return - list of file names
     */
    public List<String> getFileNames() {
        return mFileNames;
    }

    /**
     * Returns the term count of the index
     * 
     * @return - term count of index
     */
    public int getTermCount() {
        return mVocabTable.length / 2;
    }
    
    public void closeFiles(){
        try{
            System.out.println("closing files mvocablist and mpostings");
            mVocabList.close();
            mPostings.close();
        }
        catch(Exception ex){
            System.out.println(ex.getMessage());
        }
        
    }
}
