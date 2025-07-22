package com.ittia.gds.ui.mainframe.changestring;


import java.util.*;						
import java.text.*;

public class Date_current {
	public static String save_time;

	public static String defineTime(String df) {
		  Date dNow = new Date( );
	      SimpleDateFormat ft = new SimpleDateFormat ("E'요일'  yyyy.MM.dd 'at' hh:mm:ss a zzz");
	      
	      if (df == "y") {
		      ft = new SimpleDateFormat ("yyyy");
	      }
	      else if (df == "m") {
		      ft = new SimpleDateFormat ("yyyy.MM");
	      }
	      else if (df == "d") {
	    	  ft = new SimpleDateFormat ("yyyy.MM.dd");
	      }
	      else {
	    	  ft =  new SimpleDateFormat ("yyyy.MM.dd E  hh:mm a ");  
	      }
	      save_time = ft.format(dNow);
//	      System.out.println("Current Date: " + ft.format(dNow));
	      return save_time;
	}

	public static String main(String args) {
		String dateinput = args;
		Date_current add_date = new Date_current();
		String rcd = add_date.defineTime(dateinput);
//		System.out.println("Returned Current Date: " + rcd);		
	   return rcd;
	}
}