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

package com.testoptimal.exec.page;

import java.util.Map;

import com.testoptimal.exec.exception.MBTException;
import com.testoptimal.exec.mscript.MScriptInterface.IGNORE_INHERITED_METHOD;

import groovy.lang.Closure;

/**
 * Page object.  Page contains a name and a list of elements.  Page can also
 * have 0 or many actions.  Use "$SYS.getPageMgr().addPage('page1')" 
 * to create a page and added to 
 * 
 * @author yxl01
 *
 */
@IGNORE_INHERITED_METHOD
public class Page {
	private String name;
	private Map<String, Element> elements = new java.util.HashMap<>();
	private Map<String, Closure<Object>> actions = new java.util.HashMap<>();
	
	public Page (String name_p) {
		this.name = name_p;
	}
	
	/**
	 * returns the page name.
	 * @return
	 */
	public String getName () {
		return this.name;
	}

	/**
	 * adds an action to the page.
	 * 
	 * @param action
	 * @param func closure function
	 * @return this page for chaining adding additional actions.
	 * @throws MBTException
	 */
	public Page addAction (String action, Closure<Object> func) throws MBTException {
		if (this.actions.containsKey(action)) throw new MBTException ("Action " + action + " already exists for element " + this.name);
		this.actions.put(action, func);
		return this;
	}

	/**
	 * calls the page action.
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
	 * adds an element to the page.
	 * 
	 * @param name
	 * @param locator
	 * @return
	 * @throws MBTException
	 */
	public Element addElement (String name, Object locator) throws MBTException {
		if (this.elements.containsKey(name)) throw new MBTException ("Element " + name + " already exists on page " + this.name);
		Element elem = new Element (name, locator);
		this.elements.put(name, elem);
		return elem;
	}
	
	/**
	 * returns the element for the name specified.
	 * 
	 * @param name
	 * @return
	 */
	public Element element (String name) {
		return this.elements.get(name);
	}
	
	/**
	 * returns the Map of all elements in the page.
	 * @return
	 */
	public Map<String, Element> getElementMap() {
		return this.elements;
	}

	/**
	 * returns the Map of all action functions
	 * 
	 * @return
	 */
	public Map<String, Closure<Object>> getActionMap() {
		return this.actions;
	}
}
	

