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

import java.util.ArrayList;
import java.util.List;

import com.testoptimal.exec.exception.MBTAbort;
import com.testoptimal.exec.mscript.MScriptInterface.IGNORE_INHERITED_METHOD;

/**
 * Manages page objects.  Use "$PAGE" to obtain this object.
 * 
 *
 */
@IGNORE_INHERITED_METHOD
public class PageMgr {
	private List<Page> pageList = new ArrayList<Page>();


	/**
	 * returns page object for the page name specified.
	 * 
	 * @param pageName_p
	 */
	public Page getPage (String name_p) {
		return this.pageList.stream().filter(p -> p.getName().equals(name_p)).findFirst().orElse(null);
	}
	
	/**
	 * adds a page object and return the page created.
	 * 
	 * @param name_p
	 */
	public Page addPage (String name_p) throws MBTAbort {
		if (this.getPage(name_p)!=null) throw new MBTAbort ("Page " + name_p + " already exists");
		Page page = new Page(name_p);
		this.pageList.add(page);
		return page;
	}
	
	/**
	 * returns the list of all page objects.
	 * @return
	 */
	public List<Page> getPageList () {
		return this.pageList;
	}
}
