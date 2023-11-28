package com.testoptimal.server.controller;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class WebRedirectController {
	private static String StarterPage = "IDE_Main.html";
	
	@RequestMapping(value = "/", method = RequestMethod.GET)
	public String defaultStarterPage() {
		return "redirect:IDE_Login.html";
	}
	
	private String getStarterPage() {
	   String url = StarterPage;
	   return "redirect:" + url;
	}
   
	@RequestMapping(value = "/swagger", method = RequestMethod.GET)
	public String swaggerPage() {
		return "redirect:swagger-ui.html";
	}
	@RequestMapping(value = "/ide", method = RequestMethod.GET)
	public String idePage() {
		return this.getStarterPage();
	}
	@RequestMapping(value = "/dashboard", method = RequestMethod.GET)
	public String dashboardPage() {
		return "redirect:Dashboard_Main.html";
	}
}