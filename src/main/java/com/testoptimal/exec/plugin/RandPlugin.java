/***********************************************************************************************
 * Copyright (c) 2009-2024 TestOptimal.com
 *
 * This file is part of TestOptimal MBT.
 *
 * TestOptimal MBT is free software: you can redistribute it and/or modify it under the terms of 
 * the GNU General Public License as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *
 * TestOptimal MBT is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See 
 * the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with TestOptimal MBT. 
 * If not, see <https://www.gnu.org/licenses/>.
 ***********************************************************************************************/

package com.testoptimal.exec.plugin;

import java.util.Random;

import cern.jet.random.Uniform;

/**
 * Contains a collection of data generation functions to generate various types
 * of test data using pattern and random number generator.
 * 
 * @author yxl01
 *
 */
public final class RandPlugin {
	private static String lowerAlphabets = "abcdefghijklmnopqrstuvwxyz";
	private static String upperAlphabets = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
	private static String numbers = "0123456789";
	private static String alphaNumeric = lowerAlphabets + upperAlphabets + numbers;

	private Random randObj = new Random();

	/**
	 * returns a random integer number between startInt_p (inclusive) and endInt_p (exclusive).
	 * @param startInt_p min value inclusive
	 * @param endInt_p max value exlusive
	 */
	public int number(int startInt_p, int endInt_p) {
		return Uniform.staticNextIntFromTo(startInt_p, endInt_p);
	}

	/**
	 * generate a random string using the pattern specified. 'a' for one lower case letter, 'A' for
	 * one uppercase letter, '9' for one 0-9 digit, '*' for any one alphanumeric letter.  All other
	 * chars are inserted literally and returned.
	 * <p>
	 * Example: stringPattern('aaaAA99.99') to generate 3 lower case letters followed by 2 upper case letter
	 *   followed by 2 digits of numbers then a period and finally 2 digits of numbers, like xyzAB12.20.
	 * @param pattern_p a for a lower case letter, A for uppercase letter, 9 for numbers, other chars are treated as static char and inserted
	 *   in the return string as they appear. 
	 */
	public String stringPattern(String pattern_p) {
		StringBuffer retBuf = new StringBuffer();
		if (pattern_p==null || pattern_p.equals("")) pattern_p = "*";
		for (int i=0; i<pattern_p.length(); i++) {
			char p = pattern_p.charAt(i);
			String choices = alphaNumeric;
			switch (p) {
				case 'a':	
					choices = lowerAlphabets;
					break;
				case 'A':
					choices = upperAlphabets;
					break;
				case '9':
					choices = numbers;
					break;
				case '*':
					choices = alphaNumeric;
					break;
				default:
					choices = String.valueOf(p);
			}
			if (choices.length()==1) retBuf.append(choices);
			else retBuf.append(choices.charAt(randObj.nextInt(choices.length())));
		}
		return retBuf.toString();
	}


	/**
	 * generate a random alphanumeric (a-z, A-Z, 0-9) string with length between the ranges specified.
	 */
	public String string(int minCharNum_p, int maxCharNum_p) {
		int charNum = randObj.nextInt(maxCharNum_p - minCharNum_p+1) + minCharNum_p;
		int numChoices = alphaNumeric.length();
		StringBuffer retBuf = new StringBuffer();
		for (int i=0; i<charNum; i++) {
			int rn = randObj.nextInt(numChoices);
			retBuf.append(alphaNumeric.charAt(rn));
		}
		return retBuf.toString();
	}

	/**
	 * returns the value for the list of values supplied.
	 */
	public String fromList(String[] list_p) {
		if (list_p==null) return null;
		if (list_p.length<=0) return "";
		int charNum = randObj.nextInt(list_p.length);
		return list_p[charNum];
	}



	/**
	 * generate a random word with option of uppercase, lowercase or uppercase on a specific char.
	 * Example: 
	 *  <ul>
	 *  	<li>word('5','10','UP') to return a word with length between
	 * 5 chars to 10 chars in all uppercase.
	 * 		<li>word('5','10','aA') to return a word with uppercase in the second char.
	 * 		<li>word('5','10','A') to return a word with uppercase in the first char.
	 * </ul>
	 * @param minChar_p minimum length for the word 
	 * @param maxChar_p maximum length for the word
	 * @param cap_p UP for upercase, LO for lowercase, pattern "aAa" for word starts with lower case
	 * and upercase at the second char and the rest in lowercase.
	 */
	public String word(int minChar_p, int maxChar_p, String cap_p) {
		cap_p = cap_p.toUpperCase();
		int wLength = randObj.nextInt(maxChar_p - minChar_p + 1) + minChar_p;
		String UpString = "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
		String LowString = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";
		if (cap_p.startsWith("UP")) {
			return this.stringPattern(UpString.substring(0,wLength));
		}
		if (cap_p.startsWith("LO")) {
			return this.stringPattern(LowString.substring(0,wLength));
		}
		String patn = cap_p + LowString;
		patn = patn.substring(0, wLength);
		return this.stringPattern(patn);
	}
	
	/**
	 * return an email of the specified length from a list of email domain specified. 
	 * @param minChar_p minimum length of the email name to the left of "@"
	 * @param maxChar_p maximum length of the email name to the left of "@"
	 * @param domainList_p a list of email domain (like .com, .gov, .edu, etc.) separated by comma,
	 * if specify blank value, it defaults to "com,net,edu,gov,biz,au,cn,ca,co.uk,co.in.
	 */
	public String email(int minChar_p, int maxChar_p, String[] domainList_p) {
		if (domainList_p==null || domainList_p.length == 0) {
			domainList_p = new String [] {"com","net","edu","gov","biz","au","cn","ca","co.uk","co.in"};
		}

		String email = this.stringPattern("a") + this.string(minChar_p-1, maxChar_p);
		String domain = this.fromList(domainList_p);
		String host = this.string (2, 10);
		String ret = email + "@" + host;
		if (!domain.startsWith(".")) {
			ret += ".";
		}
		ret += domain;
		return ret;
	}


	/**
	 * return a phone number of specified pattern.
	 * 
	 * @param ptn_p phone number pattern using "9" for each number and "-" to insert a "-" to the 
	 * phone number.
	 */
	public String phone(String pattern_p) {
		if (pattern_p==null || pattern_p.equals("")) {
			pattern_p = "US";
		}
		if (pattern_p.equalsIgnoreCase("US")) {
			pattern_p = "999-999-9999";
		}
		return this.stringPattern(pattern_p);
	}
	
	/**
	 * return a US zip code between 00000 - 99999.
	 */
	public String zipCode() {
		return this.stringPattern("99999");
	}
	
	/**
	 * return a US ZIP+4
	 */
	public String zipCodePlus4() {
		return this.stringPattern("99999-9999");
	}

	/**
	 * return a street address.
	 */
	public String streetAddr() {
		StringBuffer retBuf = new StringBuffer();
		String stNum = String.valueOf(this.number(1, 2000));
		String stName = "";
		if (this.randObj.nextInt()<0.5) {
			// alpha street name;
			stName = this.word(5, 15, "A");
		}
		else {
			// numeric street name
			stName = String.valueOf(this.number(1, 200));
			char lastChr = stName.charAt(stName.length()-1);
			switch(lastChr) {
				case '1':
					stName += "st";
					break;
				case '2':
					stName += "nd";
					break;
				case '3':
					stName += "rd";
					break;
				default:
					stName += "th";
			}
		}

		String stSuffix = this.fromList(new String[]{"Street", "Lane", "Avenue", "Blvd"});

		if (this.randObj.nextInt()<0.5) {
			// add NE, SE, etc.
			stSuffix += " " + this.fromList(new String[] {"NE", "SE", "SW", "NW", "North", "South", "West", "East"});
		}
		retBuf.append(stNum).append(" ").append(stName).append(" ").append(stSuffix);
		return retBuf.toString();
	}

	/**
	 * return a US state name.
	 */
	public String stateUS() {
		String[] StateListUS = {"ALABAMA", "ALASKA", "ARIZONA", "ARKANSAS",
				"CALIFORNIA", "COLORADO", "CONNECTICUT", "DELAWARE", //"DISTRICT OF COLUMBIA",
				"FLORIDA", "GEORGIA", "HAWAII", "IDAHO", "ILLINOIS",
				"INDIANA", "IOWA", "KANSAS", "KENTUCKY", "LOUISIANA",
				"MAINE", "MARYLAND", "MASSACHUSETTS", "MICHIGAN", "MINNESOTA",
				"MISSISSIPPI", "MISSOURI", "MONTANA", "NEBRASKA", "NEVADA",
				"NEW HAMPSHIRE", "NEW JERSEY", "NEW MEXICO", "NEW YORK", "NORTH CAROLINA",
				"NORTH DAKOTA", "OHIO", "OKLAHOMA", "OREGON", "PENNSYLVANIA",
				"RHODE ISLAND", "SOUTH CAROLINA", "SOUTH DAKOTA", "TENNESSEE", "TEXAS",
				"UTAH", "VERMONT", "VIRGINIA", "WASHINGTON", "WEST VIRGINIA",
				"WISCONSIN", "WYOMING" };

		return this.fromList(StateListUS);
	}
	
	/**
	 * return a US state code.
	 */
	public String stateCodeUS() {
		String[] StateCodeListUS = {"AL", "AK", "AZ", "AR", 
				"CA", "CO", "CT", "DE", //"DC",
				"FL", "GA", "HI", "ID", "IL",
				"IN", "IA", "KS", "KY", "LA",
				"ME", "MD", "MA", "MI", "MN",
				"MS", "MO", "MT", "NE", "NV",
				"NH", "NJ", "NM", "NY", "NC",
				"ND", "OH", "OK", "OR", "PA",
				"RI", "SC", "SD", "TN", "TX",
				"UT", "VT", "VA", "WA", "WV",
				"WI", "WY"};
		return this.fromList(StateCodeListUS);
	}
}
