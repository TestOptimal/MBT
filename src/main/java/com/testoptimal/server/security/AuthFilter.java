package com.testoptimal.server.security;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;

import com.testoptimal.server.config.Config;

@Component
public class AuthFilter extends GenericFilterBean {
	private static List<String> trustedIpList = null;

	@Override
	//	public void doFilter(final ServletRequest req, final ServletResponse res, final FilterChain chain) throws IOException, ServletException {
	public void doFilter(jakarta.servlet.ServletRequest request, jakarta.servlet.ServletResponse response,
			jakarta.servlet.FilterChain chain) throws IOException, jakarta.servlet.ServletException {
		if (trustedIpList==null) {
			trustedIpList = new java.util.ArrayList<>();
			try {
				String thisIP = InetAddress.getLocalHost().getHostAddress();
				trustedIpList.add(thisIP.substring(0, thisIP.lastIndexOf(".")));
				String ips = Config.getProperty("security.trusted.ip.addresses", "*");
				trustedIpList.addAll(Arrays.asList(ips.split(",")));
			}
			catch (Exception e) {
				//
			}
		}
		
//		final HttpServletRequest request = (HttpServletRequest) req;
//		final HttpServletResponse response = (HttpServletResponse) res;
		chain.doFilter(request, response);
//		HttpSession session = request.getSession(false);
//		String reqIP = request.getRemoteAddr();
//		if (session!=null) {
//			if (trustedIpList.stream().noneMatch(s -> reqIP.startsWith(s) || s.equals("*"))) {
//				throw new ServletException ("Invalid credential (2)");
//			}
////			String userid = request.getUserPrincipal().getName();
////			String httpSessionID = session.getId();
////			UserSessionMgr.registerUserSession(userid, httpSessionID);
//		}
	}
	
}
