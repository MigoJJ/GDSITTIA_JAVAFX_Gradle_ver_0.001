package com.ittia.gds.ui.mainframe.changestring;

import java.util.*;
import java.text.*;

public class EMRTextTransformer {

    /**
     * Processes onset-related abbreviations like "3d:(" into " (onset 3-day ago :cd )".
     * @param word The string containing the abbreviation.
     * @return The processed string.
     */
	public static String processAbbreviation(String word) {
	    String[] wordArray = word.split(" ");
	    for (int i = 0; i < wordArray.length; i++) {
	        if (wordArray[i].contains(":(")) {
	            String replacement = wordArray[i];
	            if (replacement.contains("d")) {
	                replacement = replacement.replace("d", "-day ago :cd )").replace(":(", " (onset ");
	            } else if (replacement.contains("w")) {
	                replacement = replacement.replace("w", "-week ago :cd )").replace(":(", " (onset ");
	            } else if (replacement.contains("m")) {
	                replacement = replacement.replace("m", "-month ago :cd )").replace(":(", " (onset ");
	            } else if (replacement.contains("y")) {
	                replacement = replacement.replace("y", "-year ago :cd )").replace(":(", " (onset ");
	            } else {
	                return word; // Return original if no valid time unit is found
	            }
	            // Replace :cd with current date
	            String currentDate = new java.text.SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date());
	            replacement = replacement.replace(":cd", currentDate);
	            wordArray[i] = replacement;
	            return String.join(" ", wordArray);
	        }
	    }
	    return word;
	}

    /**
     * Processes prescription abbreviations like ":>1" into " mg 1 tab p.o. q.d.".
     * @param word The string containing the prescription code.
     * @return The processed string.
     */
    public static String processPrescription(String word) {
        String[] wordArray = word.split(" ");
        StringBuilder retWord = new StringBuilder();
        for (String w : wordArray) {
            if (w.contains(":>")) {
                if (w.contains("1")) {
                    w = w.replace(":>1", " mg 1 tab p.o. q.d.");
                } else if (w.contains("2")) {
                    w = w.replace(":>2", " mg 1 tab p.o. b.i.d.");
                } else if (w.contains("3")) {
                    w = w.replace(":>3", " mg 1 tab p.o. t.i.d.");
                } else if (w.contains(":>0")) {
                    w = w.replace(":>0", " without medications");
                } else if (w.contains(":>4")) {
                    w = w.replace(":>4", " with medications");
                }
                // No 'else' block needed; if no match, original 'w' is appended
            }
            retWord.append(w).append(" ");
        }
        return retWord.toString().trim();
    }


    /**
     * Main method for date processing (wrapper for defineTime)
     * @param args The format specifier string
     * @return Formatted date/time string
     */
    public static void main(String args) {

    }
}