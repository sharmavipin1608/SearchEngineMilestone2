package com.java.searchengine.main;

import java.io.File;
import java.io.RandomAccessFile;
import static java.lang.System.exit;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Scanner;

/**
 * Starting point for the application
 */

public class DiskEngine {

    public static void main(String[] args) {
        while (true) {
            Scanner scan = new Scanner(System.in);
            System.out.println("Angels corpus: /Users/vipinsharma/NetBeansProjects/SearchEngineAssignment2/angels");
            System.out.println("/Users/vipinsharma/NetBeansProjects/SearchEngineAssignment2/MobyDick");
            System.out.println("/Users/vipinsharma/Desktop/documentsTrial");
            System.out.println("Menu:");
            System.out.println("1) Build index");
            System.out.println("2) Boolean Mode");
            System.out.println("3) Ranked Retrieval Mode");
            System.out.println("4) Burst Ranked Retrieval Mode");
            System.out.println("5) Exit");
            System.out.println("Choose a selection:");
            int menuChoice = scan.nextInt();
            scan.nextLine();

            switch (menuChoice) {
                case 1:
                    System.out.println("Enter the name of a directory to index: ");
                    String folder = scan.nextLine();

                    IndexWriter writer = new IndexWriter(folder);
                    writer.buildIndex();
                    break;

                case 2:
                    System.out.println("Enter the name of an index to read:");
                    String indexName1 = scan.nextLine();

                    DiskInvertedIndex index1 = new DiskInvertedIndex(indexName1);
                    QueryProcessor query = new QueryProcessor(index1);

                    while (true) {
                        System.out.println("Enter one or more search terms, separated "
                                + "by spaces:");
                        Scanner scan2 = new Scanner(System.in);
                        String input2 = scan2.nextLine();

                        if (input2.equals("EXIT")) {
                            break;
                        }

                        int[] arr = query.queryProcessing(input2);

                        if (arr == null) {
                            System.out.println("not found");
                        } else {
                            for (int i : arr) {
                                System.out.println(index1.getFileNames().get(i));
                            }
                            System.out.println("Number of documents : " + arr.length);
                            System.out.println();
                        }
                    }
                    break;

                case 3:
                    System.out.println("\n\nEnter the name of an index to read:");
                    String indexName2 = scan.nextLine();

                    DiskInvertedIndex index2 = new DiskInvertedIndex(indexName2);
                    
                    System.out.println("\n\nWeighting scheme :");
                    System.out.println("1. Default");
                    System.out.println("2. Traditional");
                    System.out.println("3. Okapi");
                    System.out.println("4. Wacky");
                    System.out.print("Select weighting scheme : ");
                    int weighingScheme = scan.nextInt();
                    
                    RankedRetrievals rankedRetrievals = new RankedRetrievals(index2,weighingScheme);

                    //QueryProcessor query = new QueryProcessor(index1);
                    while (true) {
                        System.out.println("\tEnter one or more search terms, separated "
                                + "by spaces:");
                        Scanner scan3 = new Scanner(System.in);
                        String input3 = scan3.nextLine();

                        if (input3.equals("EXIT")) {
                            break;
                        }

                        rankedRetrievals.beginCalculations(input3);
                    }
                    break;
                
                case 4:
                    System.out.println("\n\nEnter the name of an index to read:");
                    //String indexName2 = scan.nextLine();
                    String path = "/Users/vipinsharma/NetBeansProjects/SearchEngineAssignment2/angels";
                    
                    
                    System.out.println("\n\nWeighting scheme :");
                    System.out.println("1. Default");
                    System.out.println("2. Traditional");
                    System.out.println("3. Okapi");
                    System.out.println("4. Wacky");
                    System.out.print("Select weighting scheme : ");
                    int weighingScheme1 = scan.nextInt();
                    System.out.println("weighing scheme " + weighingScheme1);
                        
                    
                    
                    long[] timeArr = new long[10];
                    DiskInvertedIndex index3 = new DiskInvertedIndex(path);
                                
                    for(int i=0;i<10;i++){
                        
                        Calendar cal1 = Calendar.getInstance();
                        SimpleDateFormat sdf1 = new SimpleDateFormat("HH:mm:ss");
                        System.out.println("Start time : " + sdf1.format(cal1.getTime()));
                    
                        try{
                            Scanner queries = new Scanner(new File(path, "queries.bin"));
                            //QueryProcessor query = new QueryProcessor(index1);
                            String line;
                            
                                
                            while (queries.hasNextLine()) {
                                System.out.println("-------------------------");
                                line = queries.nextLine();
                                System.out.println(line);
                                RankedRetrievals rankedRetrievals1 = new RankedRetrievals(index3,weighingScheme1);
                                rankedRetrievals1.beginCalculations(line);
                                //index3.closeFiles();
                                

                            }
                        }
                        catch(Exception ex){
                            System.out.println("here in main : " + ex.getMessage());
                        }

                        Calendar cal2 = Calendar.getInstance();
                        SimpleDateFormat sdf2 = new SimpleDateFormat("HH:mm:ss");
                        System.out.println("Start time : " + sdf2.format(cal2.getTime()));

                        long timediff = ((cal2.getTimeInMillis() - cal1.getTimeInMillis()));
                        System.out.println("Time taken = " + timediff);
                        
                        timeArr[i] = timediff;
                    }
                    
                    long sum = 0;
                    for(int i=0;i<10;i++)
                    {
                        sum += timeArr[i];
                    }
                    sum = sum/10;
                    System.out.println("Average time : " + sum);
                    break;
                    
                case 5:
                    exit(0);
                    break;
            }
        }
    }
}
