package com.testoptimal.plugin;

import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.testoptimal.plugin.MScriptInterface.IGNORE_INHERITED_METHOD;
import com.testoptimal.plugin.MScriptInterface.TO_PLUGIN;
import com.testoptimal.util.ArrayUtil;
import com.testoptimal.util.DataGenUtil;
import com.testoptimal.util.StringUtil;

import cern.jet.random.Binomial;
import cern.jet.random.Exponential;
import cern.jet.random.Gamma;
import cern.jet.random.Normal;
import cern.jet.random.Poisson;
import cern.jet.random.Uniform;

/**
 * Contains a collection of data generation functions to generate various types
 * of test data using pattern and random number generator.
 * 
 * @author yxl01
 *
 */
@TO_PLUGIN
public final class RandPlugin {
	private Random randNumObj = new Random(37);


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
		return DataGenUtil.randString(pattern_p);
	}


	/**
	 * generate a random alphanumeric (a-z, A-Z, 0-9) string with length between the ranges specified.
	 */
	public String string(int minCharNum_p, int maxCharNum_p) {
		return DataGenUtil.randString(minCharNum_p, maxCharNum_p);
	}

	/**
	 * returns the value for the list of values supplied.
	 */
	public String fromList(String[] list_p) {
		return DataGenUtil.randFromList(list_p);
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
		return DataGenUtil.randWord(minChar_p, maxChar_p, cap_p);
	}
	
	/**
	 * return an email of the specified length from a list of email domain specified. 
	 * @param minChar_p minimum length of the email name to the left of "@"
	 * @param maxChar_p maximum length of the email name to the left of "@"
	 * @param domainList_p a list of email domain (like .com, .gov, .edu, etc.) separated by comma,
	 * if specify blank value, it defaults to "com,net,edu,gov,biz,au,cn,ca,co.uk,co.in.
	 */
	public String email(int minChar_p, int maxChar_p, String domainList_p) {
		String[] domainList = null;
		if (StringUtil.isEmpty(domainList_p)) {
			domainList = new String [] {"com","net","edu","gov","biz","au","cn","ca","co.uk","co.in"};
		}
		else {
			domainList = ArrayUtil.stringToList(domainList_p, " ,;");
		}

		return DataGenUtil.randEmail(minChar_p, maxChar_p, domainList);
	}


	/**
	 * return a phone number of specified pattern.
	 * 
	 * @param ptn_p phone number pattern using "9" for each number and "-" to insert a "-" to the 
	 * phone number.
	 */
	public String phone(String ptn_p) {
		int idx9 = ptn_p.indexOf("9");
		return ptn_p.substring(0,idx9) + DataGenUtil.randNum(1, 9) + DataGenUtil.randPhoneNum(ptn_p.substring(idx9+1));
	}
	
	/**
	 * return a US zip code between 00000 - 99999.
	 */
	public String zipCode() {
		return DataGenUtil.randString("99999");
	}
	
	/**
	 * return a US ZIP+4
	 */
	public String zipCodePlus4() {
		return DataGenUtil.randString("99999-9999");
	}

	/**
	 * return a street address.
	 */
	public String streetAddr() {
		StringBuffer retBuf = new StringBuffer();
		String stNum = String.valueOf(DataGenUtil.randNum(1, 2000));
		String stName = "";
		if (this.randNumObj.nextInt()<0.5) {
			// alpha street name;
			stName = DataGenUtil.randWord(5, 15, "A");
		}
		else {
			// numeric street name
			stName = String.valueOf(DataGenUtil.randNum(1, 200));
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

		String stSuffix = DataGenUtil.randFromList(new String[]{"Street", "Lane", "Avenue", "Blvd"});

		if (this.randNumObj.nextInt()<0.5) {
			// add NE, SE, etc.
			stSuffix += " " + DataGenUtil.randFromList(new String[] {"NE", "SE", "SW", "NW", "North", "South", "West", "East"});
		}
		retBuf.append(stNum).append(" ").append(stName).append(" ").append(stSuffix);
		return retBuf.toString();
	}

	/**
	 * return a US state name.
	 */
	public String stateUS() {
		return DataGenUtil.randFromList(DataGenUtil.StateListUS);
	}
	
	/**
	 * return a US state code.
	 */
	public String stateCodeUS() {
		return DataGenUtil.randFromList(DataGenUtil.StateCodeListUS);
	}
}
