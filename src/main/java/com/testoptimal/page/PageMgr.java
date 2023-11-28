package com.testoptimal.page;

import java.util.Map;

import com.testoptimal.exception.MBTAbort;
import com.testoptimal.plugin.MScriptInterface.IGNORE_INHERITED_METHOD;

/**
 * Manages page objects.  Use "$SYS.getPageMgr()" to obtain this object.
 * 
 *
 */
@IGNORE_INHERITED_METHOD
public class PageMgr {
	private Map<String, Page> allPageMap = new java.util.HashMap<String,Page>();

	/**
	 * finds and returns the page object
	 * @param name_p
	 * @return
	 */
	public Page page (String name_p) throws MBTAbort {
		Page page = this.allPageMap.get(name_p);
		if (page==null) {
			throw new MBTAbort("Page not found: " + name_p);
		}
		return page;
	}
	
	/**
	 * adds a page object and return the page created.
	 * 
	 * @param name_p
	 */
	public Page addPage (String name_p) throws MBTAbort {
		if (this.allPageMap.containsKey(name_p)) throw new MBTAbort ("Page " + name_p + " already exists");
		Page page = new Page(name_p);
		this.allPageMap.put(name_p, page);
		return page;
	}
	
	/**
	 * returns the count of page objects.
	 * 
	 * @return
	 */
	public int pageCount () {
		return this.allPageMap.size();
	}

	/**
	 * returns the Map array of all page objects.
	 * @return
	 */
	public Map<String, Page> getPageMap () {
		return this.allPageMap;
	}
}
