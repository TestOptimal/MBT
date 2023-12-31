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

package com.testoptimal.exec.plugin.page;

import java.util.Map;

import com.testoptimal.exec.mscript.MScriptInterface.IGNORE_INHERITED_METHOD;

import groovy.lang.Closure;

/**
 * Page Element.  An element has a name and a locator, and can contain 0 or many 
 * actions.
 * 
 * @author yxl01
 *
 */
@IGNORE_INHERITED_METHOD
public class Element {
	private String name;
	private Object locator;
	private Map<String, Closure<Object>> actions = new java.util.HashMap<>();
	
	/**
	 * 
	 * @param name
	 * @param locator
	 */
	public Element(String name, Object locator) {
		this.name = name;
		this.locator = locator;
	}
	
	public Object getLocator() {
		return this.locator;
	}
	
	/**
	 * adds an action to the element.
	 * 
	 * @param action
	 * @return this element for chaining adding additional actions
	 * @param func closure function
	 */
	public Element addAction (String action, Closure<Object> func) {
		this.actions.put(action, func);
		return this;
	}
	
	/**
	 * returns the element name.
	 */
	public String getName() {
		return this.name;
	}
	
	/**
	 * calls the element action.
	 * 
	 * @param action
	 * @param args
	 * @return
	 * @throws Exception
	 */
	public Object perform (String action, Object [] args) throws Exception {
		Closure<Object> func = this.actions.get(action);
		if (func == null) {
			throw new Exception ("Unknown action " + action + " on element " + this.name);
		}
		return func.call(this, args);
	}
	
	/**
	 * returns the Map of all actions for the element.
	 * @return
	 */
	public Map<String, Closure<Object>> getActionMap() {
		return this.actions;
	}
	
	/**
	 * wait until the action specified evaluates to boolean true or does
	 * not generate runtime error (like element not found) or until 
	 * times out.
	 * 
	 * @param action_p action code of this element
	 * @param timeoutMillis_p timeout in milliseconds
	 * @param params_p additional parameters for the action
	 * @return true if wait was successful, false it times out.
	 * @throws Exception
	 */
	public boolean waitUntil (String action_p, long timeoutMillis_p, Object ... params_p) throws Exception {
		long endMillis = System.currentTimeMillis() + timeoutMillis_p;
		while (System.currentTimeMillis() <= endMillis) {
			try {
				Object ret = this.perform(action_p, params_p);
				if (ret instanceof String) {
					if (((String) ret).equalsIgnoreCase("true")) {
						return true;
					}
				}
				else if (ret instanceof Integer) {
					if (((Integer) ret) > 0) {
						return true;
					}
				}
				else return true;
			}
			catch (Exception e) {
				//
				// e.printStackTrace();
			}
			Thread.sleep(50);
		}
		return false;
	}
}

