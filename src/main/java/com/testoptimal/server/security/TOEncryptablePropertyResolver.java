package com.testoptimal.server.security;

import org.jasypt.encryption.pbe.PooledPBEStringEncryptor;
import org.jasypt.encryption.pbe.config.SimpleStringPBEConfig;

import com.ulisesbocchio.jasyptspringboot.EncryptablePropertyResolver;

public class TOEncryptablePropertyResolver implements EncryptablePropertyResolver {
    private final PooledPBEStringEncryptor encryptor;

    public TOEncryptablePropertyResolver(char[] password) {
        this.encryptor = new PooledPBEStringEncryptor();
        SimpleStringPBEConfig config = new SimpleStringPBEConfig();
        config.setPasswordCharArray(password);
        config.setAlgorithm("PBEWITHHMACSHA512ANDAES_256");
        config.setKeyObtentionIterations("1000");
        config.setPoolSize(1);
        config.setProviderName("SunJCE");
        config.setSaltGeneratorClassName("org.jasypt.salt.RandomSaltGenerator");
//        config.setIvGeneratorClassName("org.jasypt.iv.RandomIvGenerator");
        config.setStringOutputType("base64");
        encryptor.setConfig(config);
    }

    @Override
    public String resolvePropertyValue(String value) {
        if (value != null && value.startsWith("{cipher}")) {
            return encryptor.decrypt(value.substring("{cipher}".length()));
        }
        return value;
    }
}
