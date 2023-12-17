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

import java.security.spec.KeySpec;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESedeKeySpec;

import org.apache.commons.codec.binary.Base64;

public class Cypher {
    private SecretKey ky = null;
    private Cipher c = null;
    public String encrypt(String originalMsg_p) throws Exception {
		c.init(Cipher.ENCRYPT_MODE, ky);
		byte[] utf8 = originalMsg_p.getBytes("UTF8");
		byte [] enc1 = c.doFinal(utf8);
		byte [] encKey = Base64.encodeBase64(enc1);
		return new String(encKey);
	}
    
	public String decrypt(String encryptedMsg_p) throws Exception {
		byte[] enc1 = encryptedMsg_p.getBytes("UTF8");
		byte[] enc3 = Base64.decodeBase64(enc1);
	    c.init(Cipher.DECRYPT_MODE, ky);
		String encValue= new String(c.doFinal(enc3));
		return encValue;
	}
	
    public Cypher (String keyString) {
		byte[] enc = keyString.getBytes();
		KeySpec ks = null;
		SecretKeyFactory kf = null;
		try {
			ks = new DESedeKeySpec(enc);
			kf = SecretKeyFactory.getInstance("DESede");
			ky = kf.generateSecret(ks);
			c = Cipher.getInstance("DESede"); 
		}
		catch (Exception e) {
			System.out.println("License key error: " + e.getMessage());
		}
    
	}		
}