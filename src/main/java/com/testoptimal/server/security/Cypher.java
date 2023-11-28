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
//        String[] keys = keyString.split(" ");
//        // old k[4] [2], new k[2], [4]
//        String key = keys[2] + keys[4] + keys[7] + keys[1] + keys[6] + keys[5] + keys[3] + keys[0];
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