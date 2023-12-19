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