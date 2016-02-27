package com.robertnorthard.dtbs.server.layer.utils.encryption;

import com.robertnorthard.dtbs.server.configuration.ConfigService;
import java.security.Key;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Read and decrypt encrypted property files.
 * 
 * @author robertnorthard
 */
public class EncryptedProperties extends Properties {
    
    private static final Logger LOGGER = Logger.getLogger(
            EncryptedProperties.class.getName());
    
    private Key key;
    private final Properties keystoreProperties;
    private final Properties encryptedProperties;
    
    /**
     * Constructor for testing/dependency injection.
     * 
     * @param key key to decrypt properties with.
     * @param encryptedProperties the encrypted properties.
     */
    public EncryptedProperties(Key key, Properties encryptedProperties){
        this.key = key;
        this.keystoreProperties = null;
        this.encryptedProperties = encryptedProperties;
    }
    
    /**
     * Default constructor for class.
     * 
     * @param encryptedProperties
     */
    public EncryptedProperties(Properties encryptedProperties){
        this.keystoreProperties = ConfigService.getConfig("secret.properties");
        
        this.key = SymmetricEncryptionUtil.loadKeyFromKeyStore(
                keystoreProperties.getProperty("dtbs.keystore.path"),
                keystoreProperties.getProperty("dtbs.keystore.password"),
                keystoreProperties.getProperty("dtbs.keystore.properties.secret.key"), 
                keystoreProperties.getProperty("dtbs.keystore.properties.secret.password"),
                keystoreProperties.getProperty("dtbs.keystore.storetype"));
        
        this.encryptedProperties = encryptedProperties;
    }
    
    /**
     * Get a key form properties file and if encrypted decrypt.
     * Encrypted properties come in the following format:
     *  key = E{base64}
     * 
     * @param key key of property to look for
     * @return the value associated with the property.
     */
    public String getKey(String key){
        
        String value = this.encryptedProperties.getProperty(key);
        
        if(value != null && value.startsWith("E{") && value.endsWith("}")){
            
            value = value.replace("E{", "").replace("}", "");
            value = this.decryptProperty(value);
            
            LOGGER.log(Level.INFO, "encryptedProperties#getKey - property decrypted - $$$$$$ ");
        }
        
        return value;
    }
    
    /**
     * Decrypt the specified property.
     * 
     * @param value the property to decrypt.
     * @return the decrypted value.
     */
    private String decryptProperty(String value){
        
        if(this.key == null) {
            throw new IllegalStateException("Key not laoded.");
        }
        
        return SymmetricEncryptionUtil.decrypt(value, "AES", key);
    }
    
    @Override
    public String getProperty(String key) {
        return this.getKey(key);
    }
}
