package com.vik.batch;

import org.springframework.batch.item.ItemProcessor;

import com.vik.util.CaesarCipher;

public class EncryptProcessor implements ItemProcessor<String, String> {

	private Integer key;

	private CaesarCipher cipher;

	public EncryptProcessor(final Integer key) {
		this.cipher = new CaesarCipher(key);
	}

	@Override
	public String process(final String s) throws Exception {
		return cipher.encrypt(s);
	}
}
