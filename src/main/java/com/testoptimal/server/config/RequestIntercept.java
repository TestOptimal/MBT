package com.testoptimal.server.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class RequestIntercept implements HandlerInterceptor {
	private static Logger logger = LoggerFactory.getLogger(RequestIntercept.class);

	
	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
		logger.info(request.getMethod() + ": " + request.getRequestURL().toString() 
				+ " from " + request.getRemoteAddr());
		return true;
	}
	
//	@Override
//	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
//		long startTime = (Long) request.getAttribute("startTime");
//		logger.info("Request URL::" + request.getRequestURL().toString() + ":: Time Taken="
//				+ (Instant.now().toEpochMilli() - startTime));
//	}
}
