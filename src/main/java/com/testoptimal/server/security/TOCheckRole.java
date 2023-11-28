package com.testoptimal.server.security;

import java.io.Serializable;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

// @PreAuthorize("hasRole('ADMIN3') and hasPermission('hasAccess','ADMIN')")
@Component
public class TOCheckRole implements PermissionEvaluator {
   @Override
   public boolean hasPermission(Authentication authentication, Object accessType, Object permission) {
      if (authentication != null && accessType instanceof String) {
         if ("hasAccess".equalsIgnoreCase(String.valueOf(accessType))) {
        	 String role = String.valueOf(permission);
        	 return authentication.getName().equalsIgnoreCase("anonymousUser") || 
        		 authentication.getAuthorities().stream().anyMatch(a -> 
      		 		a.getAuthority().equalsIgnoreCase("ROLE_" + role)
      		 	);
         }
         return false;
      }
      return false;
   }

   @Override
   public boolean hasPermission(Authentication authentication, Serializable serializable, String targetType, Object permission) {
      return false;
   }
}