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
 * of test data using pattern and random generator.
 * 
 * @author yxl01
 *
 */
@IGNORE_INHERITED_METHOD
@TO_PLUGIN
public final class RandPlugin extends PluginAncestor {
	private static Logger logger = LoggerFactory.getLogger(RandPlugin.class);
	private Random randNumObj = new Random(37);

	/**
	 * <p>INTERNAL USE ONLY</p>
	 */
	@NOT_MSCRIPT_METHOD
	@Override
	public void close() {
		try {
			// disconnect from dbs, release file locks, etc.
		}
		catch (Exception e) {
			//ok
		}
	}

	/**
	 * <p>INTERNAL USE ONLY</p>
	 */
	@NOT_MSCRIPT_METHOD
	@Override
	public String getPluginID() {
		return "RAND";
	}

	/**
	 * <p>INTERNAL USE ONLY</p>
	 */
	@NOT_MSCRIPT_METHOD
	@Override
	public void start() throws Exception {
		return;
	}

	/**
	 * returns a random integer number between startInt_p (inclusive) and endInt_p (exclusive).
	 * <p>Example, "$RAND.randNum(2,5)" to return a random integer of 2, 3, or 4.
	 * </p>
	 * @param startInt_p min value inclusive
	 * @param endInt_p max value exlusive
	 */
	public int randNum(int startInt_p, int endInt_p) {
		return Uniform.staticNextIntFromTo(startInt_p, endInt_p);
	}

	/**
	 * generate a random string using the pattern specified. 'a' for one lower case letter, 'A' for
	 * one uppercase letter, '9' for one 0-9 digit, '*' for any one alphanumeric letter.  All other
	 * chars are inserted literally and returned.
	 * <p>
	 * Example: $RAND.randString('aaaAA99.99') to generate 3 lower case letters followed by 2 upper case letter
	 *   followed by 2 digits of numbers then a period and finally 2 digits of numbers, like xyzAB12.20.
	 * @param pattern_p a for a lower case letter, A for uppercase letter, 9 for numbers, other chars are treated as static char and inserted
	 *   in the return string as they appear. 
	 *  
	 * @return
	 */
	public String randString(String pattern_p) {
		return DataGenUtil.randString(pattern_p);
	}


	/**
	 * generate a random alphanumeric (a-z, A-Z, 0-9) string with length between the ranges specified.
	 * 
	 * <p>
	 * Example: $RAND.randString(2, 3) to generate a random string consisting of 2 or 3 alphanumeric characters.
	 * 
	 * @param minCharNum_p
	 * @param maxCharNum_p
	 * @return
	 */
	public String randString(int minCharNum_p, int maxCharNum_p) {
		return DataGenUtil.randString(minCharNum_p, maxCharNum_p);
	}

	
	/**
	 * returns a random number from a normal (gaussian) distribution with mean and stddev 
	 * specified.  The return string is formated with the number of precision digits after
	 * the decimal point.  If invalid number is passed for mean or stddev, they will be 
	 * defaulted to 0 for mean and 1.0 for stddev. To return an integer, specify 0 for the precisionDigit param.
	 * 
	 * <p>
	 * Example: $RAND.randNormal(20,5.0,2) to generate a number from the normal distribution
	 * with mean of 20 and standard deviation of 5.0.  The number will have two digits after the decimal 
	 * point.
	 * @param mean_p
	 * @param stddev_p
	 * @param precisionDigit_p
	 * @return
	 */
	public float randNormal(float mean_p, float stddev_p, int precisionDigit_p) {
		String retS = StringUtil.format(Normal.staticNextDouble(mean_p, stddev_p), precisionDigit_p);
		return Float.parseFloat(retS);
	}

	/**
	 * returns a random number from a Poisson distribution with mean specified. 
	 * The return string is formated with the number of precision digits after
	 * the decimal point.  If invalid mean is passed in, it will be defaulted
	 * to 0.0.
	 * 
	 * Example: $RAND.randPoisson(5,0) to generate an poisson integer number
	 * with mean time between arrival of 5.
	 * 
	 * @param mean_p
	 * @param precisionDigit_p
	 * @return
	 */
	public float randPoisson(float mean_p, int precisionDigit_p) {
		String retS = StringUtil.format(Poisson.staticNextInt(mean_p), precisionDigit_p);
		return Float.parseFloat(retS);
	}

	/**
	 * returns a random number from a Binomial distribution with n and p specified. 
	 * The return string is formated with the number of precision digits after
	 * the decimal point.
	 * @param mean_p
	 * @param stddev_p
	 * @param precisionDigit_p
	 * @return
	 */
	public float randBinomial(int mean_p, float stddev_p, int precisionDigit_p) {
		String retS = StringUtil.format(Binomial.staticNextInt(mean_p, stddev_p), precisionDigit_p);
		return Float.parseFloat(retS);
	}
	

	/**
	 * returns a random number from an Exponential distribution with lambda specified. 
	 * The return string is formated with the number of precision digits after
	 * the decimal point.
	 * @param mean_p
	 * @param precisionDigit_p
	 * @return
	 */
	public float randExponential(float mean_p, int precisionDigit_p) {
		String retS = StringUtil.format(Exponential.staticNextDouble(mean_p), precisionDigit_p);
		return Float.parseFloat(retS);
	}


	/**
	 * returns a random number from a Gamma distribution with alpha and lambda specified. 
	 * The return string is formated with the number of precision digits after
	 * the decimal point.
	 * @param mean_p
	 * @param stddev_p
	 * @param precisionDigit_p
	 * @return
	 */
	public float randGamma(float mean_p, float stddev_p, int precisionDigit_p) {
		String retS = StringUtil.format(Gamma.staticNextDouble(mean_p, stddev_p), precisionDigit_p);
		return Float.parseFloat(retS);
	}
	
	/**
	 * returns the value for the list of values supplied. List is separated by "|" or ";" (check | first then ;).
	 * <p>Example: $RAND.randFromList('string1|string2|string3') to randomly return any of the three
	 * strings specified.  
	 * </p>
	 * @param list_p list of values separated by |.
	 * @return
	 */
	public String randFromList(String list_p) {
		if (list_p==null) list_p = "";
		String[] list = list_p.split("\\|");
		if (list.length==1) {
			list = list_p.split(";");
		}
		return DataGenUtil.randFromList(list);
	}


	/**
	 * 
	 * returns the value for the list of values supplied. List is separated by the
	 * specified delimiter.
	 * <p>Example: $RAND.randFromList('string1,string2,string3',',') to randomly return any of the three
	 * strings specified.  
	 * </p>
	 * @param list_p list of values separated by the specified deliimiter.
	 * @param delimiter_p delimiter to be used to split the value list supplie in list_p
	 * @return
	 */
	public String randFromList(String list_p, String delimiter_p) {
		if (list_p==null) list_p = "";
		String[] list = list_p.split(delimiter_p);
		return DataGenUtil.randFromList(list);
	}

	/**
	 * generate a random word with option of uppercase, lowercase or uppercase on a specific char.
	 * Example: 
	 *  <ul>
	 *  	<li>$randWord('5','10','UP') to return a word with length between
	 * 5 chars to 10 chars in all uppercase.
	 * 		<li>$randWord('5','10','aA') to return a word with uppercase in the second char.
	 * 		<li>$randWord('5','10','A') to return a word with uppercase in the first char.
	 * </ul>
	 * @param minChar_p minimum length for the word 
	 * @param maxChar_p maximum length for the word
	 * @param cap_p UP for upercase, LO for lowercase, pattern "aAa" for word starts with lower case
	 * and upercase at the second char and the rest in lowercase.
	 * @return
	 */
	public String randWord(int minChar_p, int maxChar_p, String cap_p) {
		return DataGenUtil.randWord(minChar_p, maxChar_p, cap_p);
	}
	
	/**
	 * return an email of the specified length from a list of email domain specified. 
	 * @param minChar_p minimum length of the email name to the left of "@"
	 * @param maxChar_p maximum length of the email name to the left of "@"
	 * @param domainList_p a list of email domain (like .com, .gov, .edu, etc.) separated by comma,
	 * if specify blank value, it defaults to "com,net,edu,gov,biz,au,cn,ca,co.uk,co.in.
	 * @return
	 */
	public String randEmail(int minChar_p, int maxChar_p, String domainList_p) {
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
	 * Exammple: 
	 *   <ul>
	 *      <li>$RAND.randPhone('999-999-9999')
	 *      <li>$RAND.randPhone('(999)-999-9999')
	 *      <li>$RAND.randPhone('999-9999')
	 * 	 </ul>
	 * @param ptn_p phone number pattern using "9" for each number and "-" to insert a "-" to the 
	 * phone number.
	 * @return phone number
	 */
	public String randPhone(String ptn_p) {
		int idx9 = ptn_p.indexOf("9");
		return ptn_p.substring(0,idx9) + DataGenUtil.randNum(1, 9) + DataGenUtil.randPhoneNum(ptn_p.substring(idx9+1));
	}
	
	/**
	 * returns a list of words with each word in the specified range in length separated by space.
	 * <p>Example $RAND.randWordList('10','3','8') to generate 10 words list with each word
	 * between 3 and 8 chars in length.
	 * </p>
	 * @param numWord_p number of words to be returned
	 * @param minChar_p minimum length of each word
	 * @param maxChar_p maximum length of each word
	 * @return
	 */
	public String randWordList(int numWord_p, int minChar_p, int maxChar_p) {
		StringBuffer retBuf = new StringBuffer();
		for (int i=0; i<numWord_p; i++) {
			if (i>0) retBuf.append(" ");
			retBuf.append(DataGenUtil.randWord(minChar_p, maxChar_p, "LO"));
		}
		return retBuf.toString();
	}
	
	/**
	 * return a us zip code between 00000 - 99999.
	 * <p>Example $RAND.randZip()
	 * </p>
	 * @return
	 */
	public String randZip() {
		return DataGenUtil.randString("99999");
	}
	
	/**
	 * return a US ZIP+4
	 * <p>Example $RAND.randZipPlus4()
	 * </p>
	 * @return
	 */
	public String randZipPlus4() {
		return DataGenUtil.randString("99999-9999");
	}

	/**
	 * return a street address.
	 * <p>Example: $RAND.randStreetAddr()
	 * </p>
	 * 
	 * @return
	 */
	public String randStreetAddr() {
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
	 * <p>Example: $RAND.randState()
	 * </p>
	 * @return
	 */
	public String randState() {
		return DataGenUtil.randFromList(DataGenUtil.StateListUS);
	}
	
	/**
	 * return a US state code.
	 * <p>Example: $RAND.randStateCode()
	 * </p>
	 * @return
	 */
	public String randStateCode() {
		return DataGenUtil.randFromList(DataGenUtil.StateCodeListUS);
	}

	@Override
	public String getPluginDesc() {
		return "Pattern-based data generator";
	}
}
