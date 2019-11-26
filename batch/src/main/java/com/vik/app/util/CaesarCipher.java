package com.vik.app.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Caesar Cipher is an encryption algorithm in which each alphabet present
 * in plain text is replaced by alphabet some fixed number of positions down to it.
 */
public class CaesarCipher {
    private static final Logger logger = LoggerFactory.getLogger(CaesarCipher.class);
    private int key;

    /**
     * The constructor passes an integer as the key necessary for encryption and
     * decryption.
     *
     * @param key type integer
     */
    public CaesarCipher(int key) {
        this.key = key;
    }

    /**
     * Returns a string which is the result of encryption.
     *
     * @param plainText type String
     * @return type String
     */
    public String encrypt(String plainText) {
        String encryptedMessage = "";
        char ch;
        for (int i = 0; i < plainText.length(); ++i) {
            ch = plainText.charAt(i);

            if (ch >= 'a' && ch <= 'z') {
                ch = (char) (ch + key);

                if (ch > 'z') {
                    ch = (char) (ch - 'z' + 'a' - 1);
                }

                encryptedMessage += ch;
            } else if (ch >= 'A' && ch <= 'Z') {
                ch = (char) (ch + key);

                if (ch > 'Z') {
                    ch = (char) (ch - 'Z' + 'A' - 1);
                }

                encryptedMessage += ch;
            } else {
                encryptedMessage += ch;
            }
        }
        logger.info("Encrypted string is::[" + encryptedMessage + "]");
        return encryptedMessage;
    }
}
