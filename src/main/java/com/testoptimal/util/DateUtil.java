package com.testoptimal.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import com.testoptimal.exec.exception.MBTException;


public class DateUtil {
	public static enum DateInterval {daily, weekly, biweekly,  monthly,};

	public static final long DayMillis = 86400000;
	
	public static String dateToString(java.util.Date dateObj_p, String format_p) {
		if (dateObj_p==null) return "";
		if (format_p.equalsIgnoreCase("epoch")) {
			return String.valueOf(dateObj_p.getTime());
		}
		DateFormat df = new SimpleDateFormat(format_p); 
		return df.format(dateObj_p);
	}

	private static void evalDate(java.util.Calendar calObj_p, String delta_p) throws MBTException {
		int deltaNum = 0;
		char unit = ' ';
		try {
			unit = delta_p.charAt(delta_p.length()-1);
			String delta = delta_p.substring(0,delta_p.length()-1);
			if (delta.charAt(0)=='+') deltaNum = Integer.parseInt(delta.substring(1));
			else if (delta.charAt(0)=='-') deltaNum = -Integer.parseInt(delta.substring(1));
			else deltaNum = Integer.parseInt(delta);
		}
		catch (Exception e) {
			throw new MBTException("datetime.delta: " + delta_p);
		}

		switch (unit) {
			case 'D':
			case 'd':
				calObj_p.add(Calendar.DATE, deltaNum);
				break;
			case 'M':
				calObj_p.add(Calendar.MONTH, deltaNum);
				break;
			case 'Y':
			case 'y':
				calObj_p.add(Calendar.YEAR, deltaNum);
				break;
			case 'W':
			case 'w':
				calObj_p.add(Calendar.DATE, deltaNum*7);
				break;
			case 'H':
			case 'h':
				calObj_p.add(Calendar.HOUR, deltaNum);
				break;
			case 'm':
				calObj_p.add(Calendar.MINUTE, deltaNum);
				break;
			case 'S':
			case 's':
				calObj_p.add(Calendar.SECOND, deltaNum);
				break;
			default:
				throw new MBTException("datetime.expr.invalid");
		}

	}
	
	public static Calendar getDate(String delta_p) throws MBTException {
		if (StringUtil.isEmpty(delta_p)) {
			delta_p = "0D";
		}
		
		if (delta_p.equalsIgnoreCase("today")) delta_p = "0D";
		else if (delta_p.equalsIgnoreCase("tomorrow")) delta_p = "+1D";
		else if (delta_p.equalsIgnoreCase("yesterday")) delta_p = "-1D";
		
		Calendar calObj = Calendar.getInstance();
		calObj.setTime(new java.util.Date());
		calObj.set(Calendar.HOUR_OF_DAY, 0);
		calObj.set(Calendar.MINUTE, 0);
		calObj.set(Calendar.SECOND, 0);
		calObj.set(Calendar.MILLISECOND, 0);
		
		evalDate(calObj, delta_p);
		return calObj;
	}
	
	public static String getDate(String delta_p, String dateFormat_p) throws MBTException {
		return dateToString(getDate(delta_p).getTime(), dateFormat_p);
	}

	
	public static Calendar getTime(String delta_p) throws MBTException {
		if (StringUtil.isEmpty(delta_p) || delta_p.equalsIgnoreCase("now")) {
			delta_p = "0H";
		}
		Calendar calObj = Calendar.getInstance();
		calObj.setTime(new java.util.Date());
		evalDate(calObj, delta_p);
		return calObj;
	}
	
	public static String getTime(String delta_p, String dateFormat_p) throws MBTException {
		return dateToString(getTime(delta_p).getTime(), dateFormat_p);

	}
	
//	public static void main (String[] args) throws MBTException {
//		String dformat = "MM/dd/yyyy";
//		String tformat = "HH:mm:ss";
//		
//		System.out.println("today:" + getDate("", dformat));
//		System.out.println("today:" + getDate("today", dformat));
//		System.out.println("yesterday:" + getDate("yesterday", dformat));
//		System.out.println("tomorrow:" + getDate("tomorrow", dformat));
//		System.out.println("tomorrow:" + getDate("+1D", dformat));
//		System.out.println("yesterday:" + getDate("-1D", dformat));
//		System.out.println("nextMonth:" + getDate("+1M", dformat));
//		System.out.println("lastMonth:" + getDate("-1M", dformat));
//		System.out.println("nextWeek:" + getDate("+1W", dformat));
//		System.out.println("lastWeek:" + getDate("-1W", dformat));
//		System.out.println("nextYear:" + getDate("+1Y", dformat));
//		System.out.println("lastYear:" + getDate("-1Y", dformat));
//		
//		System.out.println("now:" + getTime("", tformat));
//		System.out.println("now:" + getTime("now", tformat));
//		System.out.println("nextHour:" + getTime("+1H", tformat));
//		System.out.println("lastHour:" + getTime("-1H", tformat));
//		System.out.println("nextMinute:" + getTime("+1M", tformat));
//		System.out.println("lastMinute:" + getTime("-1M", tformat));
//		System.out.println("nextSecond:" + getTime("+1S", tformat));
//		System.out.println("lastSecond:" + getTime("-1S", tformat));
//	}
	
	/**
	 * 
	 * @param dateString_p
	 * @param format_p
	 * @param local_p
	 * @return
	 * @throws Exception
	 */
	public static java.util.Date toDate (String dateString_p, String format_p, String local_p) throws Exception {
		if (StringUtil.isEmpty(local_p)) local_p = "US";
		Locale loc = new Locale(local_p);
		DateFormat df = new SimpleDateFormat(format_p, loc);
        java.util.Date result =  df.parse(dateString_p);
        return result;
	}
	
	public static int getPart(java.util.Date date_p, String partName_p) {
		if (date_p==null) return -1;
		Calendar cal = Calendar.getInstance();
		cal.setTime(date_p);
		if (partName_p.equalsIgnoreCase("year")) {
			return cal.get(Calendar.YEAR);
		}
		else if (partName_p.equalsIgnoreCase("month")) {
			return cal.get(Calendar.MONTH);
		}
		else if (partName_p.equalsIgnoreCase("day")) {
			return cal.get(Calendar.DAY_OF_MONTH);
		}
		else if (partName_p.equalsIgnoreCase("day_of_year")) {
			return cal.get(Calendar.DAY_OF_YEAR);
		}
		else if (partName_p.equalsIgnoreCase("hour")) {
			return cal.get(Calendar.HOUR_OF_DAY);
		}
		else if (partName_p.equalsIgnoreCase("minute")) {
			return cal.get(Calendar.MINUTE);
		}
		else if (partName_p.equalsIgnoreCase("second")) {
			return cal.get(Calendar.SECOND);
		}
		else if (partName_p.equalsIgnoreCase("week_of_month")) {
			return cal.get(Calendar.WEEK_OF_MONTH);
		}
		else if (partName_p.equalsIgnoreCase("week_of_year")) {
			return cal.get(Calendar.WEEK_OF_YEAR);
		}
		else return -1;
	}

	/**
	 * convert a time string to an integer (millitary time)
	 * @param timeString_p
	 * @return integer between 0 and 2359, -1 for invalid time
	 */
	public static int stringTimeToInt (String timeString_p) {
		timeString_p = timeString_p.toUpperCase().replace("M", "").replace(":", "").replace(" ", "");
		boolean pm = false;
		if (timeString_p.endsWith("A")) {
			timeString_p = timeString_p.substring(0,timeString_p.length()-1);
		}
		else if (timeString_p.endsWith("P")) {
			timeString_p = timeString_p.substring(0,timeString_p.length()-1);
			pm = true;
		}
		int tm = StringUtil.parseInt(timeString_p, -1);
		if (timeString_p.length()<3) tm *= 100; // handle 2pm
		int minute = tm % 100;
		if (tm<0 || tm>2400 || minute >= 60) return -1;
		if (tm==2400) return 0;
		if (pm && tm <1200) tm += 1200;
		return tm;
	}
	
	public static String numToTime (int timeNum_p) {
		int hour = timeNum_p/100;
		int minute = timeNum_p % 100;
		String amPM = "AM";
		if (hour==24) hour = 0;
		else if (hour >= 12) amPM = "PM";
		if (hour >= 13) hour -= 12;
		String hourS;
		if (hour<10) hourS = "0" + hour;
		else hourS = String.valueOf(hour);
		
		String minuteS;
		if (minute<10) minuteS = "0" + minute;
		else minuteS = String.valueOf(minute);
		return hourS + ":" + minuteS + " " + amPM;
	}
	
	/**
	 * converts numeric time (2359 for 23:59) to java time
	 * @param timeNum_p
	 */
	public static Date timeNumToDate (Date baseDate_p, int timeNum_p) {
		Calendar baseDate = Calendar.getInstance();
		baseDate.setTime(baseDate_p);
		int hour = timeNum_p/100;
		int min = timeNum_p%100;
		baseDate.set(Calendar.HOUR_OF_DAY, hour);
		baseDate.set(Calendar.MINUTE, min);
		return baseDate.getTime();
	}

	
	public static Date addDaysToDate (Date baseDate_p, int days_p) {
		return new Date(baseDate_p.getTime()+days_p*DayMillis);
	}
	
	public static Date addMonthsToDate (Date baseDate_p, int amount_p) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(baseDate_p);
		cal.add(Calendar.MONTH, amount_p);
		return cal.getTime();
	}
	
	// round down the date to the beginning of the date interval
	public static Date roundDate (Date date_p, DateInterval interval_p) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date_p);
	    switch (interval_p) {
		    case weekly:
		    	cal.add(Calendar.DATE, -(cal.get(Calendar.DAY_OF_WEEK) - Calendar.SUNDAY));
		    	break;
		    case biweekly:
		    	cal.add(Calendar.DATE, -(cal.get(Calendar.DAY_OF_WEEK )- Calendar.SUNDAY));
		    	if (cal.get(Calendar.WEEK_OF_YEAR)%2==0) {
		    		cal.add(Calendar.DATE, -7);
		    	}
		    	break;
		    case monthly:
		    	cal.set(Calendar.DAY_OF_MONTH, 1);
		    	break;
		    case daily:
	    	default:
		    	break;
	    }
	    return cal.getTime();
	}
	
	public static Date addInterval (Date date_p, DateInterval interval_p) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date_p);
	    switch (interval_p) {
		    case weekly:
		    	cal.add(Calendar.DATE, 7);
		    	break;
		    case biweekly:
		    	cal.add(Calendar.DATE, 14);
		    	break;
		    case monthly:
		    	cal.add(Calendar.MONTH, 1);
		    	break;
		    case daily:
	    	default:
		    	cal.add(Calendar.DATE, 1);
		    	break;
	    }
	    return cal.getTime();
	}
}

