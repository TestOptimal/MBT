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

package com.testoptimal.util;

import java.util.List;
import java.util.TreeMap;

public class ArrayUtil {

	/**
	 * finds the matchin column idx, if any column not found, return null.
	 * columns can be matched by name or integer.
	 * @param itemList_p
	 * @param searchList_p
	 * @return
	 */
	public static int[] findColIdx(String[] itemList_p, String[] searchList_p) {
		if (searchList_p==null || searchList_p.length <=0) {
			int [] retList = new int [itemList_p.length];
			for (int i=0; i<itemList_p.length; i++) {
				retList[i] = i;
			}
			return retList;
		}
		
		int [] retList = new int [searchList_p.length];
		for (int i=0; i<searchList_p.length; i++) {
			String search = searchList_p[i];
			int foundIdx = -1;
			for (int j=0; j<itemList_p.length; j++) {
				String check = itemList_p[j];
				if (check.equalsIgnoreCase(search)) {
					foundIdx = j;
					break;
				}
			}
			
			if (foundIdx<0) {
				try {
					int idx = Integer.parseInt(search);
					if (idx>=0 && idx<itemList_p.length) foundIdx = idx;
				}
				catch (Exception e) {
					foundIdx = i; // fallback default if all attempts failed to find the matching column
				}
			}
			
			if (foundIdx < 0) return null;
			retList[i] = foundIdx;
		}
		
		return retList;
	}

	/**
	 * returns a list of values for the column indices requested.
	 * @param itemList_p
	 * @param idxList_p
	 * @return
	 */
	public static String[] extractColumn(String[] itemList_p, int[] idxList_p, boolean purgeBlankLine_p) {
		if (idxList_p==null || idxList_p.length<=0) {
			return itemList_p;
		}
		
		String [] retList = new String [idxList_p.length];
		int dataCount = 0;
		for (int i=0; i<idxList_p.length; i++) {
			if (idxList_p[i]<itemList_p.length) {
				retList[i] = itemList_p[idxList_p[i]];
				if (!StringUtil.isEmpty(retList[i])) {
					dataCount++;
				}
			}
		}
		if (dataCount==0) return null;
		return retList;
	}

	public static String join(List<String> list_p, String delimiter_p) {
		if (list_p==null || list_p.isEmpty()) return "";
		
		StringBuffer retBuf = new StringBuffer();
		int i = 0;
		for (int idx=0; idx<list_p.size(); idx++) {
			String item = list_p.get(idx);
			if (item==null) continue;
			if (i++>0) retBuf.append(delimiter_p);
			retBuf.append(item);
		}
		return retBuf.toString();
	}

	public static String join(String[] list_p, String delimiter_p) {
		return join(list_p, delimiter_p, 0);
	}

	public static String join(String[] list_p, String delimiter_p, int startIdx_p) {
		if (list_p==null || list_p.length<=0) return "";
		
		StringBuffer retBuf = new StringBuffer();
		int i = 0;
		for (int idx = startIdx_p; idx < list_p.length; idx++) {
			String item = list_p [idx];
			if (i++>0) retBuf.append(delimiter_p);
			retBuf.append(item==null?"":item);
		}
		return retBuf.toString();
	}

	public static TreeMap <String, String> stringToTreeMap (String in_p, String del_p, boolean upCaseKey_p) {
		TreeMap retList = new TreeMap();
		String[] params = in_p.split(del_p);
		int i=0;
		for (String param: params) {
			param = param.trim();
			int idx = param.indexOf("=");
			if (idx<0) {
				if (!param.equals("")) {
					retList.put(String.valueOf(i), param);
				}
			}
			else {
				String key = param.substring(0, idx).trim();
				if (upCaseKey_p) key = key.toUpperCase();
				param = param.substring(idx+1).trim();
				retList.put(key, param);
			}
		}
		
		return retList;
	}
	
	public static String[] splitString(String in_p, String del_p) {
		if (in_p==null) in_p = "";
		String[] ret = in_p.split(del_p);
		return ret;
	}
	

	public static String[] stringToList(String in_p, String delimiters_p) {
		if (StringUtil.isEmpty(in_p)) return new String[] {};
		
		String del = ";";
		int bestIdx = in_p.length();
		for (int i=0; i<delimiters_p.length(); i++) {
			String delimiter = String.valueOf(delimiters_p.charAt(i));
			int idx = in_p.indexOf(delimiter);
			if (idx>=0 && idx<bestIdx) {
				bestIdx = idx;
				del = delimiter;
			}
		}
		
		String [] retList = in_p.split("["+ del + "]");
		return retList;
	}
	
	public static List<String> findOverlap(String[] arr1_p, String[] arr2_p) {
		List<String> matchList = new java.util.ArrayList<String>();
		for (String a1: arr1_p) {
			for (String a2: arr2_p) {
				if (a1.equalsIgnoreCase(a2)) {
					matchList.add(a1);
					break;
				}
			}
		}
		return matchList;
	}
}
