package com.vik.app.processor;

import com.vik.app.util.CaesarCipher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;

/**
 * The Class EncryptProcessor
 */
public class EncryptProcessor implements ItemProcessor<String, String> {

private static final Logger logger = LoggerFactory.getLogger(EncryptProcessor.class);

	private Integer key;

	private CaesarCipher cipher;

	public EncryptProcessor(final Integer key) {
		this.cipher = new CaesarCipher(key);
	}

	@Override
	public String process(final String plainString) throws Exception {
		logger.info("Plain text string passed for encryption is ::[" + plainString);
		return cipher.encrypt(plainString);
	}
}
