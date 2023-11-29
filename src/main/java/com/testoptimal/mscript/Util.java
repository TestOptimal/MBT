package com.testoptimal.mscript;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.testoptimal.plugin.MScriptInterface.IGNORE_INHERITED_METHOD;
import com.testoptimal.server.Application;
import com.testoptimal.server.config.Config;
import com.testoptimal.server.config.Config.OS;
import com.testoptimal.util.DataGenUtil;
import com.testoptimal.util.StringUtil;

@IGNORE_INHERITED_METHOD
public class Util {
	private static Logger logger = LoggerFactory.getLogger(Util.class);
	private static Gson prettyGson = new GsonBuilder().setPrettyPrinting().create();
	private static Gson gson = new Gson();
	

//	private static String dateFormat = "MM/dd/yyyy"; 
//	private static String dateLocale = "US";
//	private static String dateTimeFormat = "MM/dd/yyyy HH:mm";
//	private static ScreenRecorder videoRecorder;

	


//	/**
//	 * send mail.  Requires javaMail setup in system config (smtp mail host).
//	 * @param toAddress to address
//	 * @param subject subject text
//	 * @param messageText message body text
//	 * @throws MBTException
//	 */
//	@MSCRIPT_METHOD
//	public static void sendMail(String toAddress, String subject, String messageText) 
//		throws MBTException {
//		try {
//			String fromAddress = License.getLicEmail();
//			MailUtil.sendMail(fromAddress, toAddress, subject, messageText);
//		}
//		catch (Exception e) {
//			throw new MBTException ("Failed to send mail: " + e.getMessage());
//		}
//	}

//	/**
//	 * send mail.  Requires javaMail setup in system config (smtp mail host).
//	 * @param fromAddress from address, if blank passed, it will use the license email address
//	 * @param toAddress to address
//	 * @param subject subject text
//	 * @param messageText message body text
//	 * @throws MBTException
//	 */
//	@MSCRIPT_METHOD
//	public static void sendMail(String fromAddress, String toAddress, String subject, String messageText) 
//		throws MBTException {
//		try {
//			MailUtil.sendMail(fromAddress, toAddress, subject, messageText);
//		}
//		catch (Exception e) {
//			throw new MBTException ("Failed to send mail: " + e.getMessage());
//		}
//	}
//	
//	

//
//	/**
//	 * 
//	 * returns true if two strings sound like each other.
//	 * 
//	 * <p>
//	 * Example: 
//	 * <pre>$sys.soundAlike ('behavior','behavior')</pre>
//	 * 
//	 * @param string1_p
//	 * @param string2_p 
//	 * @return true if two strings sound like the same, else false
//	 * @throws Exception
//	 */
//	public static boolean soundAlike (String string1_p, String string2_p) throws Exception {
//		return StringUtil.soundAlike(string1_p, string2_p);
//	}
//
//	/**
//	 * returns true if two strings sound alike with the degree of similarity specified.
//	 * 
//	 * <p>
//	 * Example: <pre>$soundAlike ('behavior','behaviour', '4')</pre>
//	 * 
//	 * @param string1_p
//	 * @param string2_p
//	 * @param conf_p between 0 and 4 with 0 match anything, 4 being sound exactly the same. invalid value is 
//	 * 		defaulted to 4 (exactly sound like)
//	 * @return true if two strings sound like the same with the conf_p specified, else false
//	 * @throws Exception
//	 */
//	public static boolean soundAlike (String string1_p, String string2_p, String conf_p) throws Exception {
//		int conf = StringUtil.parseInt(conf_p, 4);
//		return StringUtil.soundAlike(string1_p, string2_p, conf);
//	}

	/**
	 * returns true if two strings are spelled alike with up to specified degree of differences allowed.  
	 * 
	 * <p>
	 * Example: 
	 * <pre>$spellAlike('collaboration', 'collaberaton', '1')
	 *$spellAlike('collaboration', 'collaberaon', '0.1')</pre>
	 * 
	 * @param string1_p original string
	 * @param string2_p string to compare to
	 * @param conf_p max number of chars difference between the two strings (edit distance) allowed or
	 *      fraction of the number of chars of the shorter string.  For example 2 for maximum of 2 chars diff allowed
	 *      and 0.10 for 10% of the length of the shorter string rounded up.
	 * @return true if the difference of the two strings spell alike according to the degree of difference specified, false otherwise.
	 * @throws Exception
	 */
	public static boolean spellAlike (String string1_p, String string2_p, String conf_p) throws Exception {
		boolean ret;
		if (conf_p.contains(".")) {
			float conf = StringUtil.parseFloat(conf_p, 0.15f);
			ret = StringUtil.spellAlike(string1_p, string2_p, conf);
		}
		else {
			int conf = StringUtil.parseInt(conf_p, 0);
			ret = StringUtil.spellAlike(string1_p, string2_p, conf);
		}
		return ret;
	}

	/**
	 * 
	 * returns true if two strings are spelled alike with up to specified degree of differences allowed.  Two strings
	 * are considered spell alike if the number of chars in difference is less than 15% of max string length of
	 * thw two strings.
	 * 
	 * <p>
	 * Example: <pre>$spellAlike('collaboration', 'collaberaton')</pre>
	 * 
	 * @param string1_p original string
	 * @param string2_p string to compare to
	 * @return true if the difference of the two strings spell alike according to the degree of difference specified, false otherwise.
	 * @throws Exception
	 */
	public static boolean spellAlike (String string1_p, String string2_p) throws Exception {
		boolean ret = spellAlike(string1_p, string2_p, "0.15");
		return ret;
	}



//	/**
//	 * starts screen recording and save the recording to the name specified.
//	 * 
//	 * A subfolder is created with the named specified in <code>/video/</code> folder 
//	 * to store the recorded screenshot files.
//	 *  
//	 * If subfolder already exists, previously stored screenshot files will be
//	 * deleted automatically.
//	 * 
//	 * You may move default location of <code>/video/</code> to another disk.  By default,
//	 * it takes a screenshot of the window every 200 milliseconds.  You may override
//	 * this setting by changing <code>recording.interval.millis</code> in <code>/www/MbtSvr/config/config.properties</code>
//	 * file.
//	 * 
//	 * The system automatically removes the screenshots that have not changed since
//	 * the previous screenshot.
//	 * 
//	 * You can playback the screen recording using "File/Playback" menu.
//	 * 
//	 * 
//	 * Note that sreen recording will consume CPU, disk I/O and disk space. As the
//	 * result, it may affect the performance of your automation scripts.
//	 * 
//	 * @param recordingSetName
//	 */
//	@MSCRIPT_METHOD
//	public static void startScreenRecording (String recordingSetName) throws Exception {
//		if (videoRecorder!=null) {
//			videoRecorder.endRecording();
//			logger.info ("Started screen recording, setName: " + recordingSetName);
//		}
//		videoRecorder = new ScreenRecorder(ScreenRecorder.getVideoFolder(), recordingSetName);
//		videoRecorder.startRecording();
//	}
//
//	/**
//	 * stop screen recording.
//	 * 
//	 */
//	@MSCRIPT_METHOD
//	public static void stopScreenRecording () {
//		if (videoRecorder!=null) {
//			try {
//				videoRecorder.endRecording();
//			}
//			catch (Exception e) {
//				// ok
//			}
//			videoRecorder = null;
//		}
//	}
//
//	/**
//	 * change the screen recording interval.  This overrides the default (200 ms) and the customized setting 
//	 * <code>recording.interval.millis</code> in /config/config.properties.
//	 * @param millis - number of milliseconds
//	 */
//	@MSCRIPT_METHOD
//	public static void setRecordingInerval (String millis) {
//		if (videoRecorder!=null) {
//			try {
//				videoRecorder.setIntervalMS(Integer.parseInt(millis));
//			}
//			catch (Exception e) {
//				logger.warn("$setScreenRecordingInterval(" + millis + ") ignored - parameter millis must be an integer");
//			}
//			videoRecorder = null;
//		}
//		else {
//			logger.info("setScreenRecordingInterval(" + millis + ") was ignored, video has not been started.");
//		}
//	}
	
	
	/**
	 * returns the root file folder that contains models
	 * @return
	 */
	public static String getModelRoot () {
		return Config.getModelRoot();
	}
	
	/**
	 * returns the license email
	 * @return
	 */
	public static String getLicEmail() {
		return "License.getLicEmail()";
	}
	
	/**
	 * returns current TestOptimal server host name
	 * @return
	 */
	public static String getServerName() {
		return Config.getHostName();
	}
	
	/**
	 * returns OS name of this TestOptimal server.
	 * @return
	 */
	public static String getOsName() {
		OS os = Config.getOS();
		return os.name();
	}
	
	/**
	 * returns java version number.
	 * @return
	 */
	public static String getVersionJava() {
		String jv = Config.getJavaVersion();
		return jv;
	}
	
	/**
	 * returns version number of TestOptimal release build.
	 * @return
	 */
	public static String getVersionTO() {
		String jv = Config.versionDesc;
		return jv;
	}

	/**
	 * extracts the file name from full file path string.
	 * 
	 * @param filePath_p
	 * @return
	 */
	public static String extractFileName(String filePath_p) {
		if (StringUtil.isEmpty(filePath_p)) return "";
		int idx1 = filePath_p.lastIndexOf("\\");
		int idx2 = filePath_p.lastIndexOf("/");
		if (idx2 > idx1) idx1 = idx2;
		if (idx1<=0) return filePath_p;
		else return filePath_p.substring(idx1+1);
	}


	/**
	 * uppercase the first char of each word
	 * @param inString_p
	 * @return
	 */
	public static String camelCase(String inString_p) {
		if (inString_p==null) return null;
		return DataGenUtil.camelCase(inString_p);
	}
	
	/**
	 * returns the port number used by this TestOptimal server.
	 * @return
	 */
	public static int getPort() {
		return Application.getPort();
	}
	
	/**
	 * returns the server logger object, use it to log messages to ServerLog file.
	 * 
	 */
	public static Logger getServerLogger () {
		return logger;
	}
		
	/**
	 * returns json string for a java object
	 * @param obj_p
	 * @return json string
	 */
	public static String toJSON (Object obj_p) {
		return gson.toJson(obj_p);
	}
	
	/**
	 * returns formatted json string for a java object
	 * @param obj_p
	 * @return
	 */
	public static String toJSON2 (Object obj_p) {
		return prettyGson.toJson(obj_p);
	}
	
}
