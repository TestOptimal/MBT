package com.testoptimal.util;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.List;
import java.util.Map;

public class SqlUtil {

	private static String getColumnValue(ResultSet rs_p, int colNum_p, ResultSetMetaData meta_p) throws Exception  {
		int type = meta_p.getColumnType(colNum_p);
		if (type==java.sql.Types.INTEGER) {
			int aInt = rs_p.getInt(colNum_p);
			if (rs_p.wasNull()) return "";
			else return String.valueOf(aInt);
		}
		else if (type==java.sql.Types.FLOAT) {
			float aFloat = rs_p.getFloat(colNum_p);
			if (rs_p.wasNull()) return "";
			else return String.valueOf(aFloat);
		}
		else if (type==java.sql.Types.DOUBLE) {
			double aDouble = rs_p.getDouble(colNum_p);
			if (rs_p.wasNull()) return "";
			else return String.valueOf(aDouble);
		}
		else if (type==java.sql.Types.VARCHAR || type==java.sql.Types.CHAR) {
			String aString = rs_p.getString(colNum_p);
			if (rs_p.wasNull()) return "";
			else return aString;
		}
		else if (type==java.sql.Types.DATE) {
			java.sql.Date aDate = rs_p.getDate(colNum_p);
			if (rs_p.wasNull()) return "";
			else return aDate.toString();
		}
		else if (type==java.sql.Types.TIME) {
			java.sql.Time aTime = rs_p.getTime(colNum_p);
			if (rs_p.wasNull()) return "";
			else return aTime.toString();
		}
		else if (type==java.sql.Types.TIMESTAMP) {
			java.sql.Timestamp aTimestamp = rs_p.getTimestamp(colNum_p);
			if (rs_p.wasNull()) return "";
			else return aTimestamp.toString();
		}
		else {
			String aString = rs_p.getString(colNum_p);
			if (rs_p.wasNull()) return "";
			else return aString;
		}
	}
	
	public static String toXML(ResultSet rs_p) throws Exception {
		if (rs_p==null) return "";
		StringBuffer retBuf = new StringBuffer("<rows>");
		ResultSetMetaData metaData = null;

		while (rs_p.next()) {
			if (metaData==null) metaData = rs_p.getMetaData ();
			retBuf.append("<row>");
			for (int i=1; i<=metaData.getColumnCount(); i++) {
				String columnName = metaData.getColumnName(i);
				retBuf.append("<").append(columnName).append(">")
					  .append(getColumnValue(rs_p, i, metaData))
					  .append("</").append(columnName).append(">");
			}
			retBuf.append("</row>");
		}
		
		retBuf.append("</rows>");
		return retBuf.toString();
	}
	

	public static List<Map<String,String>> toListMap(ResultSet rs_p) throws Exception {
		List<Map<String,String>> retList = new java.util.ArrayList<Map<String,String>>();
		if (rs_p!=null) {
			ResultSetMetaData metaData = rs_p.getMetaData ();
			while (rs_p.next()) {
				Map<String,String> row = new java.util.HashMap<String,String>();
				for (int i=1; i<=metaData.getColumnCount(); i++) {
					String columnName = metaData.getColumnName(i);
					row.put(columnName, getColumnValue(rs_p, i, metaData));
				}
				retList.add(row);
			}
		}
		return retList;
	}
	
	/**
	 * returns result set in a csv format without header line.
	 * @param rs_p
	 * @param colDel_p
	 * @param rowDel_p
	 * @return
	 * @throws Exception
	 */
	public static String toCSV(ResultSet rs_p, String colDel_p, String rowDel_p, boolean columnLabel_p) throws Exception {
		if (rs_p==null) return "";
		StringBuffer retBuf = new StringBuffer("");
		ResultSetMetaData metaData = null;
		colDel_p = SqlUtil.resolveDelimiter(colDel_p);
		rowDel_p = SqlUtil.resolveDelimiter(rowDel_p);
		int rowCount = 0;
		
		while (rs_p.next()) {
			if (metaData==null) {
				metaData = rs_p.getMetaData ();
				if (columnLabel_p) {
					for (int i=1; i<=metaData.getColumnCount(); i++) {
						String columnName = metaData.getColumnName(i);
						if (i>1) retBuf.append(colDel_p);
						retBuf.append(columnName);
					}
					retBuf.append(rowDel_p);
				}
			}
			
			if (rowCount>0) {
				retBuf.append(rowDel_p);
			}
			for (int i=1; i<=metaData.getColumnCount(); i++) {
//				String columnName = metaData.getColumnName(i);
				if (i>1) {
					retBuf.append(colDel_p);
				}
				retBuf.append(getColumnValue(rs_p, i, metaData));
			}
			rowCount++;
		}
		return retBuf.toString();
		
	}
	
	public static List<String[]> toDataSetArray(ResultSet rs_p) throws Exception {
		List<String[]> retList = new java.util.ArrayList<String[]>();
		if (rs_p==null) return retList;
		ResultSetMetaData metaData = rs_p.getMetaData();
		int colNum = metaData.getColumnCount();
		String columnList [] = new String[colNum];
		metaData = rs_p.getMetaData ();
		for (int i=0; i<columnList.length; i++) {
			columnList[i] = metaData.getColumnLabel(i+1);
		}
		retList.add(columnList);
		
		while (rs_p.next()) {
			String [] rowFieldList = new String[colNum];
			for (int i=0; i<columnList.length; i++) {
				rowFieldList[i] = getColumnValue(rs_p, i+1, metaData);
			}
			retList.add(rowFieldList);
		}
		return retList;
	}

	private static String resolveDelimiter(String del_p) {
		if (del_p==null) del_p = "\t";
		if (del_p.equals("[tab]")) return "\t";
		if (del_p.equals("[newline]")) return "\n";
		if (del_p.equals("[comma]")) return ",";
		if (del_p.equals("[quot]")) return "\"";
		if (del_p.equals("[apos]")) return "'";
		return del_p;
	}
}
