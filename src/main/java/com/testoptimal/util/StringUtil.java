package com.testoptimal.util;

import java.io.File;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ServerSocketFactory;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;

public class StringUtil {
	public static boolean isNumeric (String in_p) {
		try {
			if (StringUtil.isEmpty(in_p)) return false;
			in_p = in_p.trim();
			if (in_p.startsWith("-") || in_p.startsWith("+")) {
				in_p = in_p.substring(1);
			}
			Long.parseLong(in_p);
			return true;
		}
		catch(Exception e) {
			return false;
		}
	}
	
	public static boolean isEmpty(String in_p) {
		return (in_p==null || in_p.trim().equals("") || in_p.trim().equalsIgnoreCase("undefined"));
	}

	/**
	 * Converts string to a number.  If unknown, return the defaultValue_p passed in.
	 * @param in_p
	 * @param defaultValue_p
	 * @return
	 */
	public static int parseInt (String in_p, int defaultValue_p) {
		if (StringUtil.isEmpty(in_p)) return defaultValue_p;
		try {
			char signChar = '+';
			if (in_p.startsWith("+") || in_p.startsWith("-")) {
				signChar = in_p.charAt(0);
				in_p = in_p.substring(1);
			}
			
			if (signChar=='-') {
				return - Integer.parseInt(in_p);
			}
			else {
				return Integer.parseInt(in_p);
			}
		}
		catch (Exception e) {
			return defaultValue_p;
		}
	}
	
	public static long parseLong (String in_p, long defaultValue_p) {
		if (StringUtil.isEmpty(in_p)) return defaultValue_p;
		try {
			char signChar = '+';
			if (in_p.startsWith("+") || in_p.startsWith("-")) {
				signChar = in_p.charAt(0);
				in_p = in_p.substring(1);
			}
			
			if (signChar=='-') {
				return - Long.parseLong(in_p);
			}
			else {
				return Long.parseLong(in_p);
			}
		}
		catch (Exception e) {
			return defaultValue_p;
		}
	}
	
	public static float parseFloat (String in_p, float defaultValue_p) {
		if (StringUtil.isEmpty(in_p)) return defaultValue_p;
		try {
			char signChar = '+';
			if (in_p.startsWith("+") || in_p.startsWith("-")) {
				signChar = in_p.charAt(0);
				in_p = in_p.substring(1);
			}
			
			if (signChar=='-') {
				return - Float.parseFloat(in_p);
			}
			else {
				return Float.parseFloat(in_p);
			}
		}
		catch (Exception e) {
			return defaultValue_p;
		}
	}
	
	/**
	 * Returns a string representation of the array of strings using the delimiter
	 * passed in.
	 * @param list_p
	 * @param delimeter_p
	 * @param purgeBlank_p
	 * @return
	 */
	public static String toString(String[] list_p, String delimeter_p, boolean purgeBlank_p) {
		if (list_p==null || list_p.length==0) return "";
		StringBuffer retBuf = new StringBuffer();
		int idx=0;
		for (int i=0; i<list_p.length; i++) {
			if (purgeBlank_p && StringUtil.isEmpty(list_p[i])) continue;
			if (idx>0) retBuf.append(delimeter_p);
			idx++;
			retBuf.append(list_p[i]);
		}
		return retBuf.toString();
	}

	/**
	 * Returns a string representation of the array of strings using the delimiter
	 * passed in.
	 * @param list_p
	 * @param delimeter_p
	 * @param purgeBlank_p
	 * @return
	 */
	public static String toString(List<String> list_p, String delimeter_p, boolean purgeBlank_p) {
		if (list_p==null || list_p.size()==0) return "";
		StringBuffer retBuf = new StringBuffer();
		int idx=0;
		for (int i=0; i<list_p.size(); i++) {
			if (purgeBlank_p && StringUtil.isEmpty(list_p.get(i))) continue;
			if (idx>0) retBuf.append(delimeter_p);
			idx++;
			retBuf.append(list_p.get(i));
		}
		return retBuf.toString();
	}

	public static boolean isTrue(String in_p) {
		if (in_p==null) return false;
		if (in_p.equalsIgnoreCase("true")) return true;
		if (in_p.equalsIgnoreCase("yes")) return true;
		if (in_p.equalsIgnoreCase("y")) return true;
		if (in_p.equalsIgnoreCase("t")) return true;
		if (in_p.equalsIgnoreCase("1")) return true;
		if (in_p.equalsIgnoreCase("on")) return true;
		if (in_p.equalsIgnoreCase("checked")) return true;
		if (in_p.equalsIgnoreCase("selected")) return true;
		if (in_p.equalsIgnoreCase("valid")) return true;
		if (in_p.equalsIgnoreCase("pass")) return true;
		if (in_p.equalsIgnoreCase("passed")) return true;
		return false;
	}

	/**
	 * Replaces the invalid chars with "_" that are not valid chars for the java method name
	 * @param in_p
	 * @return
	 */
	public static String removeInvalidChar (String in_p) {
		if (in_p==null) return null;
		StringBuffer retBuf = new StringBuffer();
		for (int i=0; i<in_p.length(); i++) {
			char curChar = in_p.charAt(i);
			if (i==0 && curChar>='0' && curChar<='9') retBuf.append("X");
			if (curChar>='a' && curChar<='z' ||
				curChar>='A' && curChar<='Z' ||	
				curChar>='0' && curChar<='9' ||
				curChar=='_') retBuf.append(curChar);
			else retBuf.append('_');
		}
		
		return retBuf.toString();
	}
	
	public static String cleanMsgJSON(String in_p) {
		if (in_p==null) return null;
		StringBuffer retBuf = new StringBuffer();
		for (int i=0; i<in_p.length(); i++) {
			char curChar = in_p.charAt(i);
			switch (curChar) {
			case '\n':
			case '\r':
				retBuf.append("");
				break;
			case '\t':
				retBuf.append(" ");
				break;
			case '\\':
				retBuf.append("/");
				break;
			case '"':
				retBuf.append("'");
				break;
				
			default:
				retBuf.append(curChar);
			}
		}
		
		return retBuf.toString();
	}
	
	public static String removeInvalidXMLChar (String in_p) {

		if (in_p==null) return null;
		StringBuffer retBuf = new StringBuffer();
		for (int i=0; i<in_p.length(); i++) {
			char curChar = in_p.charAt(i);
			switch (curChar) {
			case '\n':
			case '\r':
			case '\t':
				retBuf.append("");
				break;
			case '\\':
//			case '/':
				retBuf.append("/");
				break;
			case '"':
				retBuf.append("'");
				break;
			case '<':
				retBuf.append("(");
				break;
			case '>':
				retBuf.append(")");
				break;
			case '&':
				retBuf.append("&amp;");
				break;
			default:
				retBuf.append(curChar);
			}
		}
		
		return retBuf.toString();
		
	}
	
	/**
	 * extracts the file name with extension.
	 */
	public static String extractFileName (String fullFilePath_p) {
		return extractFileName(fullFilePath_p, false);
	}
	
	/**
	 * extracts the file name with extension.
	 */
	public static String extractFileName (String fullFilePath_p, boolean stripExtension_p) {
		if (fullFilePath_p==null) return null;
		String ret = fullFilePath_p;
		int idx = ret.lastIndexOf("/");
		if (idx>=0) ret = ret.substring(idx+1);
		idx = ret.lastIndexOf("\\");
		if (idx>=0) ret = ret.substring(idx+1);
		if (!stripExtension_p) return ret;
		idx = ret.lastIndexOf(".");
		if (idx>=0) ret = ret.substring(0, idx);
		return ret;
	}
	
	/**
	 * returns a file path for the java class name path without the file name.
	 * @param javaClass_p
	 * @return
	 */
	public static String genJavaFilePath(String javaClass_p, boolean includeFileName_p) {
	   String[] path = javaClass_p.split("\\.");
	   StringBuffer retBuf = new StringBuffer();
	   for (int i=0; i<path.length-1; i++) {
		   retBuf.append(path[i]).append(File.separator);
	   }
	   if (includeFileName_p) {
		   retBuf.append(path[path.length-1]).append(".class");
	   }
	   return retBuf.toString();
	}
	
	/**
	 * returns the java class name
	 */
	public static String extractClassName(String javaClass_p) {
		int idx = javaClass_p.lastIndexOf(".");
		if (idx<=0) return javaClass_p;
		String ret = javaClass_p.substring(0,idx);
		return ret;
	}
	
	/**
	 * checks if a port is available.
	 */
	public static boolean isPortInUse(int portNum_p) {
		try {
			ServerSocket socket = ServerSocketFactory.getDefault().createServerSocket(portNum_p);
			socket.close();
			return false;
	    } catch (Exception e) {
	        // port in use or unable to check.
	    }
	    return true;
	}
	
	public static java.util.ArrayList<String> stringArrayToArrayList (String[] arr_p) {
		java.util.ArrayList<String> list = new java.util.ArrayList<String>(arr_p.length);
		for (int i=0; i<arr_p.length; i++) {
			list.add(arr_p[i].trim());
		}
		return list;
	}
	
/**
 * 	String content = "hello[world]this[[is]me";
 *	List<String> tokens = tokenize(content,"[","]");
 */
	private static String escapeRegexp(String regexp) {
        String specChars = "\\$.*+?|()[]{}^";
        String result = regexp;
        for (int i=0;i<specChars.length();i++){
        	Character curChar = specChars.charAt(i);
        	result = result.replaceAll("\\"+curChar, "\\\\" + (i<2?"\\":"") + curChar); // \ and $ must have special treatment
        }
        return result;
	}

	private static List<String> findGroup(String content, String pattern, int group) {
        Pattern p = Pattern.compile(pattern);
        Matcher m = p.matcher(content);
        List<String> result = new ArrayList<String>();
        while (m.find()) {
        	result.add(m.group(group));
        }
        return result;
	}
	
	public static boolean matchRegExpr(String text_p, String regExpr_p) {
		Pattern pt;
		Matcher fit = null;
		if (text_p==null || regExpr_p==null) return false;
		
		pt = Pattern.compile(regExpr_p);
	    fit = pt.matcher(text_p);
        if (fit.find()) {
        	return true;
        }
        else return false;
	}

	/**
	 * perforrms flexible search: lowcase for case insensitive, /expr/ for regexpr,
	 * *text* for contains *text for endswith, text* for starts with.
	 * 
	 * @param text_p
	 * @param regExpr_p
	 * @return
	 */
	public static boolean matchSearch(String text_p, String regExpr_p) {
		if (StringUtil.isEmpty(text_p)) {
			if (StringUtil.isEmpty(regExpr_p)) return true;
			else return false;
		}

		if (StringUtil.isEmpty(regExpr_p)) return false;
		
		boolean isRegExpr = text_p.startsWith("/") && regExpr_p.endsWith("/");
		if (isRegExpr) {
			regExpr_p = regExpr_p.substring(1, regExpr_p.length()-1);
			return StringUtil.matchRegExpr(text_p, regExpr_p);
		}
		
		String regExprLower = regExpr_p.toLowerCase();
		if (regExpr_p.equals(regExprLower))  {
			text_p = text_p.toLowerCase();
		}
		
		boolean wildStart = regExpr_p.startsWith("*");
		if (wildStart) {
			regExpr_p = regExpr_p.substring(1);
		}
		boolean wildEnd = regExpr_p.endsWith("*");
		if (wildEnd) {
			regExpr_p = regExpr_p.substring(0, regExpr_p.length()-1);
		}
		if (wildStart && wildEnd) {
			return text_p.contains(regExpr_p);
		}
		else if (wildStart) {
			return text_p.endsWith(regExpr_p);
		}
		else if (wildEnd) {
			return text_p.startsWith(regExpr_p);
		}
		else {
			return text_p.equals(regExpr_p);
		}
	}


	public static List<String> tokenize(String content, String firstToken, String lastToken){
        String regexp = lastToken.length()>1?escapeRegexp(firstToken) + "(.*?)"+ escapeRegexp(lastToken)
        				:escapeRegexp(firstToken) + "([^"+lastToken+"]*)"+ escapeRegexp(lastToken);
        return findGroup(content, regexp, 1);
	}
		
	public static String format(double num_p, int precisionDigit_p) {
		String ret = String.valueOf(num_p);
		int idx = ret.lastIndexOf(".");
		if (precisionDigit_p<0) return ret;
		if (precisionDigit_p==0) {
			if (idx<0) return ret;
			else return ret.substring(0,idx);
		}
		
		if (idx<0) {
			return ret + ".";
		}
		
		ret += "0000000000";
		ret = ret.substring(0, idx+precisionDigit_p+1);
		return ret;
	}

	/**
	 * wraps text to multiple lines resulting in the aspect
	 * ratio specified.
	 * 
	 * Assumption: line height = r * charWidth.
	 * 
	 * @param in_p
	 * @param widthOverHeight_p
	 * @return
	 */
	public static String wrapText(String in_p, float widthOverHeight_p, String newLineString_p) {
		int len = in_p.length();
		float r = 2.5f;
		
		int c = (int) Math.round(Math.sqrt(r * widthOverHeight_p * len));
		String ret = WordUtils.wrap(in_p, c).replace("\r","").replace("\n", newLineString_p);
		return ret;
	}
	
	public static int compareTwoString(String val1_p, String val2_p) {
		if (val1_p==null) val1_p = "";
		if (val2_p==null) val2_p = "";
		return val1_p.trim().compareToIgnoreCase(val2_p.trim());
	}

	public static String genTCName (String prefix_p, int num_p, int digits_p) {
		String tcN = "000000" + num_p;
		return prefix_p + tcN.substring(tcN.length()-digits_p);
	}
}
