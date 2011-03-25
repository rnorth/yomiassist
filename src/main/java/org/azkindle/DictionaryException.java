package org.azkindle;

import java.io.IOException;

public class DictionaryException extends RuntimeException {

	public DictionaryException(String string, Exception e) {
		super(string);
	}

}
