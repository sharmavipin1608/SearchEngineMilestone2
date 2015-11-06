package com.java.searchengine.main;

import com.java.searchengine.datastructure.PositionalPostingsStructure;
import static java.lang.System.exit;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;

/**
 * Entry point for the application. It will display options and asks user for
 * input.
 */
public class QueryParsing {

    List<PositionalPostingsStructure> resultantPostings
            = new ArrayList<>();

    private static final Pattern sQuotes = Pattern.compile("\"(.*)\"");

    /**
     *
     * @param args
     */
    public static void main(String[] args) {
        
        String str = "vipin NEAR/2        sharma";
        str = str.trim();
        Pattern ptr = Pattern.compile("(.*)NEAR/[0-9](.*)");
        if(ptr.matcher(str).find())
            System.out.println("match found");
        else
            System.out.println("match not found");
        String str1 = str.replaceAll("NEAR/2", "");
        String str2 = str.substring(str.indexOf("/")+1, str.indexOf("/")+2);
        System.out.println(str1 + " num : "+ str2);
        str = "vipin sharma";
        if(ptr.matcher(str).find())
            System.out.println("match found");
        else
            System.out.println("match not found");

        System.out.println("Angels corpus: /Users/vipinsharma/NetBeansProjects/SearchEngineAssignment2/angels");
        System.out.println("Custom corpus : /Users/vipinsharma/Downloads/Corpus");
        System.out.println("Moby Dick : /Users/vipinsharma/NetBeansProjects/SearchEngineAssignment2/MobyDick");
        System.out.print("\n\n\nEnter directory path you want to index : ");
        Scanner pathInput = new Scanner(System.in);
        String corpusPath = pathInput.nextLine();

        final Path currentWorkingPath = Paths.get(corpusPath).toAbsolutePath();

        //final Path currentWorkingPath = Paths.get("").toAbsolutePath();
        System.out.println("Current working path  : " + currentWorkingPath);
        IndexBuilderFactory.createIndex(currentWorkingPath);

        while (true) {
            System.out.println("\n\n\nChoose option : ");
            System.out.println("1. Search phrase");
            System.out.println("2. Index Statistics");
            System.out.println("3. Exit");
            System.out.print("\nEnter your choice : ");

            Scanner input = new Scanner(System.in);
            int userChoice = input.nextInt();

            switch (userChoice) {
                case 1:
                    System.out.print("Enter query : ");
                    Scanner input1 = new Scanner(System.in);
                    String userInput = input1.nextLine();
                    userInput = userInput.toLowerCase();

                    HashSet<Integer> resultSet = IndexBuilderFactory.getInstance().queryProcessing(userInput);

                    System.out.println("Query result documents : ");
                    if (resultSet != null && resultSet.size() > 0) {
                        for (Integer docId : resultSet) {
                            System.out.println(IndexBuilderFactory.getInstance().getFilesNames(docId));
                        }
                        System.out.println("Number of documents : " + resultSet.size());
                    } else {
                        System.out.println("No matches found");
                    }
                    break;

                case 2:
                    IndexBuilderFactory.getInstance().getPositionalIndex().printMetrics();
                    break;

                case 3:
                    exit(0);
                    break;

                default:
                    System.out.println("Invalid Choice");

            }
        }
    }
}
