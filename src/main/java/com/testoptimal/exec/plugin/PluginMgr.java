/**
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
 * 
 */
package com.testoptimal.exec.plugin;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.testoptimal.exec.ExecutionDirector;
import com.testoptimal.server.config.Config;

/**
 * 
 */
public class PluginMgr {
	private static Logger logger = LoggerFactory.getLogger(PluginMgr.class);
	private static Map<String, Constructor> pluginConsList = new java.util.HashMap<>();
	private static Map<String, Class> pluginClassList = new java.util.HashMap<>();

	public static void init () {
		String[] plugins = Config.getProperty("Plugins", "PAGE:com.testoptimal.exec.plugin.page.PageMgr,DATA:com.testoptimal.exec.plugin.DataSetMgr,RAND:com.testoptimal.exec.plugin.RandPlugin").split(",");
		Arrays.asList(plugins).stream().forEach (p -> {
			try {
				String[] plist = p.split(":");
		    	Class aClass = Class.forName(plist[1]);
		    	Constructor[] clist = aClass.getConstructors();
		    	Constructor constructor = aClass.getConstructor();
				pluginConsList.put(plist[0], constructor);
				pluginClassList.put(plist[0], aClass);
				logger.info("loaded plugin: " + p);
			}
			catch (Exception e) {
				logger.error("Unable to load plugin: " + p);
			}
		});
	}
	
	public static Map<String, Class> getPluginClassList () {
		return pluginClassList;
	}
	
	public static Object newPlugin(String pluginID_p, ExecutionDirector execDir_p) throws Exception {
		Constructor cons = pluginConsList.get(pluginID_p);
		if (cons==null) {
			throw new Exception ("Plugin not loaded: " + pluginID_p);
		}
				
		Object plugin =  cons.newInstance();
		Class aClass = pluginClassList.get(pluginID_p);
		try {
			Method m = aClass.getMethod("init", ExecutionDirector.class);
			m.invoke(plugin, execDir_p);
		}
		catch (NoSuchMethodException e) {
			// ok
		}
		return plugin;
	}
}
