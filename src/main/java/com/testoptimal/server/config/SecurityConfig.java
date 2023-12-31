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

package com.testoptimal.server.config;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;

import com.testoptimal.server.security.TOAuthProvider;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * https://stackoverflow.com/questions/33739359/combining-basic-authentication-and-form-login-for-the-same-rest-api
 * 
 * @author yxl01
 *
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {
	private static Logger logger = LoggerFactory.getLogger(SecurityConfig.class);

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity.headers(headers -> headers.frameOptions().sameOrigin())
		        .logout(logout -> logout
		                .logoutUrl("/logout")
		                .invalidateHttpSession(true)
		                .deleteCookies("JSESSIONID"))
                .authorizeHttpRequests(requests -> requests
                        .requestMatchers("/api/v1/sys/sysinfo").permitAll()
                		.requestMatchers("/api/v1/sys/**").authenticated()
                		.requestMatchers("/api/v1/model/**").authenticated()
                		.requestMatchers("/api/v1/runtime/**").authenticated()
                		.requestMatchers("/api/v1/stats/**").authenticated()
                		.requestMatchers("/api/v1/file/**").authenticated()
                        .requestMatchers("/api/v1/client/**").authenticated()
                        .requestMatchers("/api/v1/alm/**").authenticated()
                        .anyRequest().permitAll())
                .httpBasic(basic -> basic.authenticationEntryPoint(new AuthenticationEntryPoint() {
                    @Override
                    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {
//	                    String requestedBy = request.getHeader("X-Requested-By");
                        HttpServletResponse httpResponse = (HttpServletResponse) response;
                        if (request.getRequestURI().startsWith("/api/v") || request.getRequestURI().startsWith("/error")) {
                            httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN, authException.getMessage());
                        } else {
//                       	httpResponse.setHeader("WWW-Authenticate", null);
//                       	httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Please login at " + Application.genURL(Config.getHostName(), Application.getPort()));
                            httpResponse.setHeader("Location", "/IDE_Login.html");
                            httpResponse.setStatus(302);
                        }
                    }
                }))
                .csrf(csrf -> csrf.disable());
//               .headers().cacheControl();

        logger.info("Security enabled");
		 return  httpSecurity.build();
    }
	
    @Autowired
    private TOAuthProvider authProvider;

    @Bean
    AuthenticationManager authManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authenticationManagerBuilder = 
            http.getSharedObject(AuthenticationManagerBuilder.class);
        authenticationManagerBuilder.authenticationProvider(authProvider);
        return authenticationManagerBuilder.build();
    }
    
//    @Override
//    protected void configure(HttpSecurity http) throws Exception {
//	   	 http.headers().frameOptions().sameOrigin()
//		 	.and()
//			 	.logout()
//			 	.logoutUrl("/logout")
//			 	.invalidateHttpSession(true)
//			 	.deleteCookies("JSESSIONID")
//		 	.and()
//				.authorizeRequests()
//		        .antMatchers("/api/v1/sys/config").permitAll()
//		        .antMatchers("/api/v1/sec/**").permitAll()
//		        .antMatchers("/api/v1/demo/**").permitAll()
//	            .antMatchers("/favicon.ico").permitAll()
//	            .antMatchers("/packages/**").permitAll()
//	            .antMatchers("/webjars/**").permitAll()
//	            .antMatchers("/**/*.js").permitAll()
//	            .antMatchers("/**/*.css").permitAll()
//	            .antMatchers("/img/**").permitAll()
//	            .antMatchers("/ide").permitAll()
//		        .antMatchers("/IDE_Login.html").permitAll()
//		        .antMatchers("/*.html").permitAll()
//	            .antMatchers("/DemoApp/**").permitAll()
//		 		.antMatchers("/**").authenticated()
//		     .and()
//		        .httpBasic().authenticationEntryPoint(new AuthenticationEntryPoint() {
//	                @Override
//	                public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
////	                    String requestedBy = request.getHeader("X-Requested-By");
//                        HttpServletResponse httpResponse = (HttpServletResponse) response;
//	                	if (request.getRequestURI().startsWith("/api/v") || request.getRequestURI().startsWith("/error")) {
//                        	httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN, authException.getMessage());
//	                	}
//	                	else {
////                        	httpResponse.setHeader("WWW-Authenticate", null);
////                        	httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Please login at " + Application.genURL(Config.getHostName(), Application.getPort()));
//	                		httpResponse.setHeader("Location", "/IDE_Login.html");
//	                		httpResponse.setStatus(302);
//	                	}
//	                }
//	            })
//		     .and()
//		         .csrf().disable();
////                .headers().cacheControl();
//    	 logger.info("Security enabled");
//    }

//    @Bean
//    public AuthenticationManager customAuthenticationManager() throws Exception {
//        return authenticationManager();
//    }
}