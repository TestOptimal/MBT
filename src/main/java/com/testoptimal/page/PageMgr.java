package com.testoptimal.page;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.testoptimal.exception.MBTAbort;
import com.testoptimal.exec.mscript.MScriptInterface.IGNORE_INHERITED_METHOD;

/**
 * Manages page objects.  Use "$SYS.getPageMgr()" to obtain this object.
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
