package com.testoptimal.util.misc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Base64;
import java.util.Scanner;

public class SerialNum {
	private static String sn = null;
	
	// doesn't work on Windows 10
	public static String getSerialNum() {
		if (sn==null) {
			try {
				String osName = System.getProperty("os.name").toLowerCase();
				System.out.println("os.name:" + osName);
				
				if (osName.startsWith("mac os x")) {
					sn = getSerialMac();
				}
				else {
					sn = getSerialWin();
				}
			}
			catch (Exception e) {
				return "???";
			}
		}
		return sn;
	}

	/**
	 * 
	 * wmic bios get SerialNumber
	 * 
	 * @return
	 * 
	 */
	private static String getSerialWin() {
		// wmic command for diskdrive id: wmic DISKDRIVE GET SerialNumber
		// wmic command for cpu id : wmic cpu get ProcessorId
		Process process = null;
		try {
			process = Runtime.getRuntime().exec(new String[] { "wmic", "baseboard", "get", "SerialNumber" });
			process.getOutputStream().close();
		}
		catch (IOException ex) {
			SysLogger.logInfo(ex.getMessage());
			System.out.println(ex.getMessage());
			process = null;
		}
		if (process==null) {
			try {
				process = Runtime.getRuntime().exec(new String[] { "wmic", "bios", "get", "SerialNumber" });
				// process = Runtime.getRuntime().exec(new String[] { "wmic", "DISKDRIVE",
				// "get", "SerialNumber" });
				// process = Runtime.getRuntime().exec(new String[] { "wmic", "cpu", "get",
				// process = Runtime.getRuntime().exec(new String[] { "wmic", "baseboard",
				// "get", "SerialNumber" });
				process.getOutputStream().close();
			} catch (IOException ex) {
				SysLogger.logInfo(ex.getMessage());
				System.out.println(ex.getMessage());
				process = null;
			}
		}
		
		if (process==null) {
			return "";
		}
		
		try (Scanner sc = new Scanner(process.getInputStream());) {
			String property = sc.next();
			String serial = sc.next();
//			TestOptimalServer.logInfo(property + ": " + serial);
			if (serial.trim().indexOf(" ")>0 || serial.startsWith("Type")) return "";
			else return serial;
		}
	}

	/**
	 * 
	 * ioreg -l | grep IOPlatformSerialNumber
	 * 
	 * @return
	 * 
	 */
	private static String getSerialMac() {
		ProcessBuilder pb = new ProcessBuilder("bash", "-c",
//           "ioreg -l | awk '/IOPlatformSerialNumber/ { print $4;}'");
			"ioreg -c IOPlatformExpertDevice -d 2 | awk -F\\\" '/IOPlatformSerialNumber/{print $(NF-1)}'");
		pb.redirectErrorStream(true);
		try {
			Process p = pb.start();
			String s = "";
			// read from the process's combined stdout & stderr
			BufferedReader stdout = new BufferedReader( new InputStreamReader(p.getInputStream()));
			while ((s = stdout.readLine()) != null) {
//				TestOptimalServer.logInfo("Serial Number: " + s);
				break;
			}
			p.getInputStream().close();
			p.getOutputStream().close();
			p.getErrorStream().close();
			return s;
		} catch (Exception ex) {
			SysLogger.logError("Unable to obtain Mac serial number: " + ex.getMessage());
			return "";
		}
	}
	
	public static String getMAC () {
		try {
			InetAddress ip = InetAddress.getLocalHost();
			NetworkInterface network = NetworkInterface.getByInetAddress(ip);
			byte[] mac = network.getHardwareAddress();
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < mac.length; i++) {
				sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : ""));		
			}
			return sb.toString();
		}
		catch (Exception e) {
			SysLogger.logError("Unable to obtain MAC Address: " + e.getMessage());
			return "";
		}
	}
	
	public static String getSysID (String hostName_p, int digits_p) throws Exception {
		String sysIdString = SerialNum.getSerialNum() + (SerialNum.getMAC().replace("-", "")) + hostName_p;
		sysIdString = sysIdString.substring(0, Math.min(digits_p, sysIdString.length()));
		StringBuilder outString = new StringBuilder();
		outString.append(sysIdString);
		String sysID = outString.reverse().toString();
		sysID = Base64.getEncoder().encodeToString(sysID.getBytes());
		return sysID;
	}
}