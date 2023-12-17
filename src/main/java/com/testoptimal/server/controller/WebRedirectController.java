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

package com.testoptimal.server.controller;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebRedirectController {
	private static String StarterPage = "IDE_Main.html";
	
	@GetMapping("/")
	public String defaultStarterPage() {
		return "redirect:IDE_Login.html";
	}
	
	private String getStarterPage() {
	   String url = StarterPage;
	   return "redirect:" + url;
	}
   
	@GetMapping("/swagger")
	public String swaggerPage() {
		return "redirect:swagger-ui.html";
	}
	@GetMapping("/ide")
	public String idePage() {
		return this.getStarterPage();
	}
}