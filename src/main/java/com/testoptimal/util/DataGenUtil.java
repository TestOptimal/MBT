package com.testoptimal.util;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class DataGenUtil {


	private static String lowerAlphabets = "abcdefghijklmnopqrstuvwxyz";
	private static String upperAlphabets = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
	private static String numbers = "0123456789";
	private static String alphaNumeric = lowerAlphabets + upperAlphabets + numbers;
	
	public static String randString(String pattern_p) {
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
	
	private static Random randObj = new Random();
	public static String randString(int minChar_p, int maxChar_p) {
		
		int charNum = randObj.nextInt(maxChar_p - minChar_p+1) + minChar_p;
		int numChoices = alphaNumeric.length();
		StringBuffer retBuf = new StringBuffer();
		for (int i=0; i<charNum; i++) {
			int rn = randObj.nextInt(numChoices);
			retBuf.append(alphaNumeric.charAt(rn));
		}
		return retBuf.toString();
		
	}
	
	public static String randFromList(String [] list_p) {
		if (list_p==null) return null;
		if (list_p.length<=0) return "";
		int charNum = randObj.nextInt(list_p.length);
		return list_p[charNum];
	}
	
	public static String randFromList(List<String> list_p) {
		if (list_p==null) return null;
		if (list_p.isEmpty()) return "";
		int charNum = randObj.nextInt(list_p.size());
		return list_p.get(charNum);
	}

	public static String randEmail(int minName_p, int maxName_p, String[] domainList_p) {
		String email = randString("a") + randString(minName_p-1, maxName_p);
		String domain = randFromList(domainList_p);
		String host = randString (2, 10);
		String ret = email + "@" + host;
		if (!domain.startsWith(".")) {
			ret += ".";
		}
		ret += domain;
		return ret;
	}
	
	public static String randPhoneNum(String pattern_p) {
		if (pattern_p==null || pattern_p.equals("")) {
			pattern_p = "US";
		}
		if (pattern_p.equalsIgnoreCase("US")) {
			pattern_p = "999-999-9999";
		}
		return randString(pattern_p);
	}

	/**
	 * generates a random word containing alphabets only
	 * @param minChar_p minimum # of characters, no more than 100 and must be less or equal to maxChar_p
	 * @param maxChar_p maximum # of characters but no more than 100
	 * @param cap_p UP for uppercase, LO for lowercase or actual string pattern a/A/n
	 * @return
	 */
	public static String randWord(int minChar_p, int maxChar_p, String cap_p) {
		cap_p = cap_p.toUpperCase();
		int wLength = randObj.nextInt(maxChar_p - minChar_p + 1) + minChar_p;
		String UpString = "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
		String LowString = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";
		if (cap_p.startsWith("UP")) {
			return randString(UpString.substring(0,wLength));
		}
		if (cap_p.startsWith("LO")) {
			return randString(LowString.substring(0,wLength));
		}
		String patn = cap_p + LowString;
		patn = patn.substring(0, wLength);
		return randString(patn);
	}

	public static String[] StateCodeListUS = {"AL", "AK", "AZ", "AR", 
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
	public static String[] StateListUS = {"ALABAMA", "ALASKA", "ARIZONA", "ARKANSAS",
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

	public static String randZipUS() {
		return randString("99999");
	}
	
	public static String resolveStateCodeUS(String stateName_p) {
		if (stateName_p==null) return null;
		for (int i=0; i<StateListUS.length; i++) {
			String stateCode = StateCodeListUS[i];
			String stateName = StateListUS[i];
			if (stateName_p.equalsIgnoreCase(stateName) || stateName_p.equalsIgnoreCase(stateCode)) {
				return stateCode;
			}
		}
		return null;
	}

	public static String resolveStateNameUS(String stateCode_p) {
		if (stateCode_p==null) return null;
		for (int i=0; i<StateCodeListUS.length; i++) {
			if (stateCode_p.equalsIgnoreCase(StateCodeListUS[i])) {
				return StateListUS[i];
			}
		}
		return null;
	}

	public static String patZero(long in_p, int digits_p) {
		String ret = String.valueOf(in_p);
		if (ret.length()>=digits_p) return ret;
		String padder = "00000000000000000000000000000000000000000000000000000";
		ret = padder+ ret;
		return ret.substring(ret.length()-digits_p);
	}

	private static long dayMillis = 24*60*60*1000;
	public static Date randDate(Calendar minDate_p, Calendar maxDate_p) {
		long diff = maxDate_p.getTime().getTime() - minDate_p.getTime().getTime();
		diff = diff / dayMillis;
		int randI = randObj.nextInt((int)diff);
		minDate_p.add(Calendar.DATE, randI);
		return minDate_p.getTime();
	}
	
	public static String randTime(int startHour_p, int endHour_p, String format_p, int increment_p) {
		return "";
	}

	
	public static long randNum(long start_p, long end_p) {
		long nLong = Math.round(randObj.nextFloat() * (end_p - start_p + 1));
		return nLong;
	}
	
	public static String camelCase(String in_p) {
		String [] list = in_p.split(" ");
		StringBuffer retBuf = new StringBuffer();
		for (int i=0; i<list.length; i++) {
			if (i>0) retBuf.append(" ");
			char chr = list[i].charAt(0);
			retBuf.append(String.valueOf(chr).toUpperCase()).append(list[i].substring(1).toLowerCase());
		}
		return retBuf.toString();
	}
}
