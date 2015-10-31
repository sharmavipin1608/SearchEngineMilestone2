package com.java.searchengine.main;


import java.util.regex.*;

/**
 * Stems the terms in the documents using the porter stemmer algorithm.
 */
public class PorterStemmer {

    // a single consonant
    private static final String c = "[^aeiou]";
    // a single vowel
    private static final String v = "[aeiouy]";

    // a sequence of consonants; the second/third/etc consonant cannot be 'y'
    private static final String C = c + "[^aeiouy]*";
    // a sequence of vowels; the second/third/etc cannot be 'y'
    private static final String V = v + "[aeiou]*";

    // this regex pattern tests if the token has measure > 0 [at least one VC].
    private static final Pattern mGr0 = Pattern.compile("^(" + C + ")?" + V + C);

   //private static final Pattern mGr0 = Pattern.compile("^(" + C + ")?" +
    //"(" + V + C + ")+(" + V + ")?");
    // add more Pattern variables for the following patterns:
    // m equals 1: token has measure == 1
    private static final Pattern mEq1 = Pattern.compile("^(" + C + ")?" + "(("
            + V + C + "){1})(" + V + ")*$");

    // m greater than 1: token has measure > 1
    private static final Pattern mGr1 = Pattern.compile("^(" + C + ")?" + "(("
            + V + C + "){2})");

    // vowel: token has a vowel after the first (optional) C
    // double consonant: token ends in two consonants that are the same, 
    //			unless they are L, S, or Z. (look up "backreferencing" to help 
    //			with this)
    private static final Pattern doubleCNotLSZ = Pattern.compile("([^aeioulsz])(\\1)$");

    //double consonant
    private static final Pattern doubleC = Pattern.compile("([^aeiou])(\\1)$");

    // m equals 1, Cvc: token is in Cvc form, where the last c is not w, x, 
    //			or y.
    private static final Pattern cvcNotWXY = Pattern.compile(c + v + "[^aeiouwxy]$");

    //matches single S at the end of the token and checks that its not ending in SS
    private static final Pattern singleS = Pattern.compile("[^s]s$");

    //matches if a token contains a vowel
    private static final Pattern containsVowel = Pattern.compile(v);

    /**
     *
     * @param token
     * @return
     */
    public static String processToken(String token) {
        //System.out.println("token : " + token);
        if (token.length() < 3) {
            return token; // token must be at least 3 chars
        }

        // step 1a
        if (token.endsWith("sses")) {
            token = token.substring(0, token.length() - 2);
        } // program the other steps in 1a. 
        // note that Step 1a.3 implies that there is only a single 's' as the 
        //	suffix; ss does not count. you may need a regex pattern here for 
        // "not s followed by s".
        else if (token.endsWith("ies")) {
            token = token.substring(0, token.length() - 2);
        } else if (singleS.matcher(token).find()) {
            token = token.substring(0, token.length() - 1);
        }

        // step 1b
        boolean doStep1bb = false;
        //step 1b.1
        if (token.endsWith("eed")) {
            // token.substring(0, token.length() - 3) is the stem prior to "eed".
            // if that has m>0, then remove the "d".
            String stem = token.substring(0, token.length() - 3);
            if (mGr0.matcher(stem).find()) { // if the pattern matches the stem
                token = stem + "ee";
            }
        } //step 1b.2
        else if (token.endsWith("ed")) {
            String stem = token.substring(0, token.length() - 2);
            if (containsVowel.matcher(stem).find()) {
                token = stem;
                doStep1bb = true;
            }
        } //step 1b.3
        else if (token.endsWith("ing")) {
            String stem = token.substring(0, token.length() - 3);
            if (containsVowel.matcher(stem).find()) {
                token = stem;
                doStep1bb = true;
            }
        }

        //step 1bb
        // step 1b*, only if the 1b.2 or 1b.3 were performed.
        if (doStep1bb) {
            if (token.endsWith("at") || token.endsWith("bl")
                    || token.endsWith("iz")) {
                token = token + "e";
            } //step 1b.4
            //token ends in double consonant and consonant is not L, S or Z
            else if (doubleCNotLSZ.matcher(token).find()) {
                token = token.substring(0, token.length() - 1);
            } //step 1b.5
            //m = 1 and *o
            else if (mEq1.matcher(token).find()) {
                if (cvcNotWXY.matcher(token).find()) {
                    token = token + "e";
                }
            }
            // use the regex patterns you wrote for 1b*.4 and 1b*.5
        }

        // step 1c
        // program this step. test the suffix of 'y' first, then test the 
        // condition *v*.
        if (token.endsWith("y")) {
            String stem = token.substring(0, token.length() - 1);
            if (containsVowel.matcher(stem).find()) {
                token = stem + "i";
            }
        }

        // step 2
        // program this step. for each suffix, see if the token ends in the 
        // suffix. 
        //		* if it does, extract the stem, and do NOT test any other suffix.
        //    * take the stem and make sure it has m > 0.
        //			* if it does, complete the step. if it does not, do not 
        //				attempt any other suffix.
        // you may want to write a helper method for this. a matrix of 
        // "suffix"/"replacement" pairs might be helpful. It could look like
        // string[][] step2pairs = {  new string[] {"ational", "ate"}, 
        //new string[] {"tional", "tion"}, ....
        boolean step2applied = false;

        for (int i = 0; i < 20; i++) {
            String[] rule = step2Helper(i);

            if (token.endsWith(rule[0])) {
                String stem = token.substring(0, token.length() - rule[0].length());
                if (mGr0.matcher(stem).find()) {
                    token = stem + rule[1];
                    step2applied = true;
                }
            }

            if (step2applied) {
                break;
            }
        }

        // step 3
        // program this step. the rules are identical to step 2 and you can use
        // the same helper method. you may also want a matrix here.
        boolean step3applied = false;

        for (int i = 0; i < 7; i++) {
            String[] rule = step3Helper(i);

            if (token.endsWith(rule[0])) {
                String stem = token.substring(0, token.length() - rule[0].length());
                if (mGr0.matcher(stem).find()) {
                    token = stem + rule[1];
                    step3applied = true;
                }
            }

            if (step3applied) {
                break;
            }
        }

        // step 4
        // program this step similar to step 2/3, except now the stem must have
        // measure > 1.
        // note that ION should only be removed if the suffix is SION or TION, 
        // which would leave the S or T.
        // as before, if one suffix matches, do not try any others even if the 
        // stem does not have measure > 1.
        boolean step4applied = false;

        for (int i = 0; i < 19; i++) {
            String rule = step4Helper(i);

            if (token.endsWith(rule)) {
                String stem = token.substring(0, token.length() - rule.length());
                if (mGr1.matcher(stem).find()) {
                    if (rule.equals("ion")) {
                        if (!(stem.endsWith("s") || stem.endsWith("t"))) {
                            continue;
                        }
                    }
                    token = stem;
                    step4applied = true;
                }
            }

            if (step4applied) {
                break;
            }
        }

      // step 5
        // program this step. you have a regex for m=1 and for "Cvc", which
        // you can use to see if m=1 and NOT Cvc.
        // all your code should change the variable token, which represents
        // the stemmed term for the token.
        //step 5a
        if (token.endsWith("e")) {
            String stem = token.substring(0, token.length() - 1);
            if (mGr1.matcher(stem).find()) {
                token = stem;
            } else if (mEq1.matcher(stem).find()) {
                if (!cvcNotWXY.matcher(stem).find()) {
                    token = stem;
                }
            }
        } //step 5b
        else if (token.endsWith("l")
                && doubleC.matcher(token).find()
                && mGr1.matcher(token).find()) {
            token = token.substring(0, token.length() - 1);
        }
        //System.out.println("token token : " + token);
        return token;
    }

    /**
     * Helper function for step2 of the porter stemmer, checks if any of the 
     * pattern of step2 matches
     * 
     * @param ruleNo
     * @return
     */
    public static String[] step2Helper(int ruleNo) {
        String[][] step2pairs = {new String[]{"ational", "ate"},
        new String[]{"tional", "tion"},
        new String[]{"enci", "ence"},
        new String[]{"anci", "ance"},
        new String[]{"izer", "ize"},
        new String[]{"abli", "able"},
        new String[]{"alli", "al"},
        new String[]{"entli", "ent"},
        new String[]{"eli", "e"},
        new String[]{"ousli", "ous"},
        new String[]{"ization", "ize"},
        new String[]{"ation", "ate"},
        new String[]{"ator", "ate"},
        new String[]{"alism", "al"},
        new String[]{"iveness", "ive"},
        new String[]{"fulness", "ful"},
        new String[]{"ousness", "ous"},
        new String[]{"aliti", "al"},
        new String[]{"iviti", "ive"},
        new String[]{"biliti", "ble"}};
        return step2pairs[ruleNo];
    }

    /**
     * Helper function for step3 of the porter stemmer, checks if any of the 
     * pattern of step3 matches
     * 
     * @param ruleNo
     * @return
     */
    public static String[] step3Helper(int ruleNo) {
        String[][] step3pairs = {new String[]{"icate", "ic"},
        new String[]{"ative", ""},
        new String[]{"alize", "al"},
        new String[]{"iciti", "ic"},
        new String[]{"ical", "ic"},
        new String[]{"ful", ""},
        new String[]{"ness", ""}};
        return step3pairs[ruleNo];
    }

    /**
     * Helper function for step4 of the porter stemmer, checks if any of the 
     * pattern of step4 matches
     * 
     * @param ruleNo 
     * @return 
     */
    public static String step4Helper(int ruleNo) {
        String[] step4pairs = {"al", "ance", "ence", "er", "ic", "able", "ible",
            "ant", "ement", "ment", "ent", "ion", "ou", "ism",
            "ate", "iti", "ous", "ive", "ize"};
        return step4pairs[ruleNo];
    }
}