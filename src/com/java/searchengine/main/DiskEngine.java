package com.java.searchengine.main;

import com.java.searchengine.datastructure.PositionalPostingsStructure;
import static java.lang.System.exit;
import java.util.ArrayList;
import java.util.Scanner;

public class DiskEngine {

    public static void main(String[] args) {
        while (true) {
            Scanner scan = new Scanner(System.in);

            System.out.println("Menu:");
            System.out.println("1) Build index");
            System.out.println("2) Boolean Mode");
            System.out.println("3) Ranked Retrieval Mode");
            System.out.println("4) Exit");
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
                    String indexName = scan.nextLine();

                    DiskInvertedIndex index = new DiskInvertedIndex(indexName);

                    while (true) {
                        System.out.println("Enter one or more search terms, separated "
                                + "by spaces:");
                        Scanner scan1 = new Scanner(System.in);
                        String input = scan1.nextLine();

                        if (input.equals("EXIT")) {
                            break;
                        }

                        ArrayList<PositionalPostingsStructure> postingsList = index.GetPostings(
                                PorterStemmer.processToken(input.toLowerCase())
                        );

                        if (postingsList == null) {
                            System.out.println("Term not found");
                        } else {
                            System.out.print("Docs: ");
                            for (PositionalPostingsStructure posStruct : postingsList) {
                                posStruct.printData(0, index);
                                System.out.println();
                            }
                            System.out.println();
                            System.out.println();
                        }
                    }
                    break;

                case 3:
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
                        }

                    }

                    break;

                case 4:
                    exit(0);
                    break;

                case 5:
                    System.out.println("Enter the name of an index to read:");
                    String indexName2 = scan.nextLine();

                    DiskInvertedIndex index2 = new DiskInvertedIndex(indexName2);
                    RankedRetrievals rankedRetrievals = new RankedRetrievals(index2);

                    //QueryProcessor query = new QueryProcessor(index1);
                    while (true) {
                        System.out.println("Enter one or more search terms, separated "
                                + "by spaces:");
                        Scanner scan3 = new Scanner(System.in);
                        String input3 = scan3.nextLine();

                        if (input3.equals("EXIT")) {
                            break;
                        }

                        rankedRetrievals.beginCalculations(input3);

                    }

                    break;
            }
        }
    }
}
