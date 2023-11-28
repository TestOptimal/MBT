package com.testoptimal.server.security;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import com.testoptimal.server.config.Config;

/**
 *
 * @author yxl01
 *
 */
@Component
public class TOAuthProvider implements AuthenticationProvider {
	private static Logger logger = LoggerFactory.getLogger(TOAuthProvider.class);
	
    @Override
    public Authentication authenticate(Authentication authentication) {
        String name = authentication.getName();
        String password = authentication.getCredentials().toString();
        ArrayList<GrantedAuthority> groups = new ArrayList<>();
        if (!UserMgr.getInstance().authenticate(name, password)) {
        	return null;
        }
        
        if (name.equalsIgnoreCase(Config.getProperty("License.Email", ""))) {
             groups.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        }
        else {
             groups.add(new SimpleGrantedAuthority("ROLE_USER"));
        }
        UsernamePasswordAuthenticationToken user = new UsernamePasswordAuthenticationToken(name, password, groups);
        return user;
    }

    // to receive name/password auth request
    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }
}

 