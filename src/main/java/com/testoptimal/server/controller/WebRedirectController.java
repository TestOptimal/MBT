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