package com.testoptimal.util;


import java.awt.Color;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;

import javax.imageio.ImageIO;

import com.testoptimal.server.config.Config;
import com.testoptimal.util.misc.SysLogger;
 
/**
 * 
 * Some notes of using any of the Robot function:
 *     The issue with "type()" is that it is just emulating keyboard events 
 *     through the JVM. It essentially will emulate the effect of the user 
 *     just typing on the keyboard. But it is important to remember that 
 *     keyboard events will be received by the application window and UI 
 *     element that are in focus.
 *     So operations such as "$findObject()" use the UIA features to get the 
 *     Windows handles reference to the UI element, so we can use that handle 
 *     to retrieve the element properties or to directly trigger UI actions.
 *     
 *     *However* things such as keyboard events are not direct UI actions, 
 *     that are general system actions that the Windows OS will direct to 
 *     the last item that is in focus by the UI engine.
 *     
 *     Hence what you need to do, is run an operation such as $setFocus(). 
 *     $setFocus() will actually trigger the event to come to the top of the 
 *     Z-order, equivalent to if the user clicks on it with the mouse (or 
 *     ALT-TAB to the UI element). With that item now in focus, then that 
 *     element has first option to consume any user actions such as key 
 *     strokes.
 * @author yxl01
 *
 */
public class RobotUtil {
 
    private static Robot robot;
    private static Properties keyConfig = new Properties();
    private static Map<String, Integer> vkMap = new java.util.HashMap<String,Integer>();
    
    static {
    	try {
    		robot = new Robot();
            robot.delay(500); // wait to detect interrupted exception
            robot.setAutoDelay(2);
            robot.setAutoWaitForIdle(true);
    	}
    	catch (Exception e) {
    		SysLogger.logError("Error initializing Robot", e);
    	}
    	String keyFile = Config.getProperty("keyboard.mapfile");
    	if (StringUtil.isEmpty(keyFile)) {
    		keyFile = "Keys.properties";
    	}
		String keyMapFile = Config.getConfigPath() + keyFile;
    	try {
	    	File f = new File(keyMapFile);
	    	FileInputStream fs = new FileInputStream(f);
			keyConfig.load(fs);
    	}
    	catch (Exception e) {
    		SysLogger.logError("Unable to initialize key map from file: " + keyMapFile, e);
    	}
    	
    	if (!keyConfig.contains("\\")) {
        	keyConfig.put("\\", "VK_BACK_SLASH");
    	}
    	
		for (int i=0; i < KeyEvent.CHAR_UNDEFINED; i++) {
			String keyText = KeyEvent.getKeyText(i);
			if (keyText==null || keyText.startsWith("Unknown keyCode:")) {
				continue;
			}
			else {
				keyText = "VK_" + keyText.toUpperCase().replace(" ","_");
				vkMap.put(keyText, i);
			}
		}
		vkMap.put("CTRL", KeyEvent.VK_CONTROL);
    }

     
    public static void enter() {
      robot.keyPress(KeyEvent.VK_ENTER);
      robot.keyRelease(KeyEvent.VK_ENTER);
    }

    private static int lookupKey (String keyStr) throws Exception {
    	String keyString = keyStr.toUpperCase().trim();
    	if (keyString.startsWith("[") && keyString.endsWith("]")) {
    		keyString = keyString.substring(1, keyString.length()-1);
    	}
    	int idx = keyString.indexOf(".");
    	if (idx>=0) keyString = keyString.substring(idx+1);
    	if (!keyString.startsWith("VK_")) {
    		keyString = "VK_" + keyString;
    	}
		Integer aKey = vkMap.get(keyString);
    	if (aKey==null) throw new Exception ("Invalid key " + keyString);
    	return aKey;
    }

    /**
     * 
     * @param keyList VK_xxx where xxx is upper case of the key name
     * @throws Exception
     */
    public static void sendKey (String ... keyList) throws Exception {
    	for (String keyString: keyList) {
    		int aKey = lookupKey(keyString);
        	robot.keyPress(aKey);
        	robot.keyRelease(aKey);
    	}
    	robot.waitForIdle();
    }
 
    /**
     * 
     * @param keyList VK_xxx where xxx is upper case of the key name
     * @throws Exception
     */
    public static void pressKey (String ... keyList) throws Exception {
    	for (String keyString: keyList) {
    		int aKey = lookupKey(keyString);
	    	robot.keyPress(aKey);
    	}
    	robot.waitForIdle();
    }
 
    /**
     * 
     * @param keyList VK_xxx where xxx is upper case of the key name
     * @throws Exception
     */
    public static void releaseKey (String ... keyList) throws Exception {
    	for (String keyString: keyList) {
    		int aKey = lookupKey(keyString);
        	robot.keyRelease(aKey);
    	}
    	robot.waitForIdle();
    }


    public static void type(String characters) throws Exception {
        int length = characters.length();
        for (int i = 0; i < length; i++) {
			char character = characters.charAt(i);
			typeChar(character);
        }
    }

    private static void typeChar (char character) throws Exception {
    	String keySeq = keyConfig.getProperty(String.valueOf(character));
    	String[] seqList = keySeq.split(",");
    	int modKey = -1;
		String theKeyString = seqList[0];
    	if (seqList.length>1) {
    		String modKeyString = seqList[0];
    		theKeyString = seqList[1];
    		if (modKeyString.equalsIgnoreCase("VK_SHIFT")) {
    			modKey = KeyEvent.VK_SHIFT;
    		}
    		else if (modKeyString.equalsIgnoreCase("VK_ALT")) {
    			modKey = KeyEvent.VK_ALT;
    		}
    		else if (modKeyString.equalsIgnoreCase("VK_CTRL")) {
    			modKey = KeyEvent.VK_CONTROL;
    		}
    	}
		int aKey = lookupKey(theKeyString);
    	if (modKey>=0) {
    		robot.keyPress(modKey);
            robot.keyPress(aKey);
            robot.keyRelease(aKey);
            robot.keyRelease(modKey);
    	}
    	else {
            robot.keyPress(aKey);
            robot.keyRelease(aKey);
    	}
    	robot.waitForIdle();
    }
    

    /**
     * 
     * @param delayMillis # of millis between clicks
     * @param btnId 1 for left, 2 for middle, 3 for right mouse button
     * @param modKeys var argument list, VK_SHIFT, VK_CONTROL, VK_ALT, etc. 
     * @throws Exception
     */
    public static void doubleClick (int delayMillis, String btnOptions, String ... modKeys) throws Exception {
    	int btnMask = getMouseMask(btnOptions);
    	if (modKeys.length>0) {
    		pressKey(modKeys);
    	}
        robot.mousePress(btnMask);
        robot.mouseRelease(btnMask);
        robot.delay(delayMillis);
        robot.mousePress(btnMask);
        robot.mouseRelease(btnMask);
    	if (modKeys.length>0) {
    		releaseKey(modKeys);
    	}
    	robot.waitForIdle();
    }

    /**
     * 
     * @param btnId 1 for left, 2 for middle, 3 for right mouse button
     * @param modKeys var argument list, VK_SHIFT, VK_CONTROL, VK_ALT, etc. 
     * @throws Exception
     */
    public static void click (String btnOptions, String ... modKeys) throws Exception {
    	int btnMask = getMouseMask(btnOptions);
    	if (modKeys.length>0) {
    		pressKey(modKeys);
    	}
        robot.mousePress(btnMask);
        robot.mouseRelease(btnMask);
        if (modKeys.length>0) {
    		releaseKey(modKeys);
    	}
    	robot.waitForIdle();
    }

    public static void pressMouse (String btnOptions) {
    	int btnMask = getMouseMask(btnOptions);
    	robot.mousePress(btnMask);
    	robot.waitForIdle();
    }

    public static void releaseMouse (String btnOptions) {
    	int btnMask = getMouseMask(btnOptions);
    	robot.mouseRelease(btnMask);
    	robot.waitForIdle();
    }
    
	private static int getMouseMask (String btnOption_p) {
		if (StringUtil.isEmpty(btnOption_p)) return InputEvent.BUTTON1_MASK;
		btnOption_p = btnOption_p.trim().toUpperCase();
		int btnMask = 0;
		for (int i=0; i<btnOption_p.length(); i++) {
			char chr = btnOption_p.charAt(i);
			switch (chr) {
				case 'L':
					btnMask = btnMask | InputEvent.BUTTON1_MASK;
					break;
				case 'M':
					btnMask = btnMask | InputEvent.BUTTON2_MASK;
					break;
				case 'R':
					btnMask = btnMask | InputEvent.BUTTON3_MASK;
					break;
			}
		}
		if (btnMask==0) btnMask = InputEvent.BUTTON1_MASK;
		return btnMask;
	}

	
    /*
     * move mouse pointer to the specified screen position.
     */
    public static void moveMouseTo (int x, int y) {
    	robot.mouseMove(x, y);
    	robot.waitForIdle();
    }
    
	public static void dragDrop(int fromX_p, int fromY_p, int toX_p, int toY_p, String btnOption_p) {
		int btnMask = getMouseMask (btnOption_p);
		robot.mouseMove(fromX_p, fromY_p);
		robot.mousePress(btnMask);
		robot.mouseMove(toX_p, toY_p);
		robot.mouseRelease(btnMask);
		robot.waitForIdle();
	}

	public static Color getPixelColor(int x_p, int y_p) {
		return robot.getPixelColor(x_p, y_p);
	}

	public static void screenshot (int x_p, int y_p, int width_p, int height_p, String filePath_p, String fileType_p) throws IOException  {
		Rectangle screenRect = new Rectangle();
		screenRect.x = x_p;
		screenRect.y = y_p;
		screenRect.width = width_p;
		screenRect.height = height_p;
		
		BufferedImage img = robot.createScreenCapture(screenRect);
		if (!filePath_p.endsWith(fileType_p)) {
			filePath_p += "." + fileType_p;
		}
		File file = new File (filePath_p);
		ImageIO.write(img, fileType_p, file);
	}
	
	public static void screenshotFull (String filePath_p, String fileType_p) throws Exception {
		Rectangle rect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
		
		BufferedImage img = robot.createScreenCapture(rect);
		if (!filePath_p.endsWith(fileType_p)) {
			filePath_p += "." + fileType_p;
		}
		File file = new File (filePath_p);
		ImageIO.write(img, fileType_p, file);
	}

    /**
     * number of notches to move the mouse, negative to move backwards.
     * @param amt
     */
    public static void scrollMouse (int amt) {
    	robot.mouseWheel(amt);
    	robot.waitForIdle();
    }
    
    public static void main(String... args) throws Exception {
    	type("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ`1234567890-=~!@#$%^&*()_+[]\\{}|;':\",./<>?\n\t");
    }
 
    
}