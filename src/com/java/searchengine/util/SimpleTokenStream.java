package com.java.searchengine.util;

import java.io.*;
import java.util.*;

/**
Reads tokens one at a time from an input stream. Returns tokens with minimal
processing: removing all non-alphanumeric characters, and converting to 
lowercase.
*/
public class SimpleTokenStream implements TokenStream {
   private Scanner mReader;

   /**
   Constructs a SimpleTokenStream to read from the specified file.
     * @param fileToOpen
     * @throws java.io.FileNotFoundException
   */
   public SimpleTokenStream(File fileToOpen) throws FileNotFoundException {
      mReader = new Scanner(new FileReader(fileToOpen));
   }
   
   /**
   Constructs a SimpleTokenStream to read from a String of text.
     * @param text
   */
   public SimpleTokenStream(String text) {
      mReader = new Scanner(text);
   }

   /**
   Returns true if the stream has tokens remaining.
     * @return 
   */
   @Override
   public boolean hasNextToken() {
      return mReader.hasNext();
   }

   /**
   Returns the next token from the stream, or null if there is no token
   available.
     * @return 
   */
   @Override
   public String nextToken() {
      if (!hasNextToken())
         return null;
      
      //it was replacing all the special characters whereas we just want to 
      //remove special characters from the start and the end of the file
      
      String next = mReader.next();
      next = next.replaceAll("-", "hyphen");
      next = next.replaceAll("\\W", "");
      next = next.replaceAll("hyphen","-");
      next = next.toLowerCase();
      //String next = mReader.next().toLowerCase();
      return next.length() > 0 ? next : 
       hasNextToken() ? nextToken() : null;
   }
}