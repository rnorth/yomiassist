package org.azkindle;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.io.Files;
import com.google.common.io.LineProcessor;

public class Edict {

	private Map<String, String> dictionary = new HashMap<String, String>();
	private Pattern regexPattern = Pattern.compile("([^\\s]+)\\s?(\\[([^\\]]+)\\])?\\s?/(.+)");

	public Edict() {

		final File dictionaryFile = new File("dictionaries/edict/edict.txt");
		try {
			Files.readLines(dictionaryFile, Charset.forName("EUC-JP"), new LineProcessor<Boolean>() {

				public boolean processLine(String line) throws IOException {
					addDictionaryTerm(line);
					return true;
				}

				public Boolean getResult() {
					return true;
				}
			});

		} catch (FileNotFoundException e) {
			throw new DictionaryException("Could not locate dictionary file " + dictionaryFile, e);
		} catch (IOException e) {
			throw new DictionaryException("Could not load dictionary file " + dictionaryFile, e);
		}
	}

	private void addDictionaryTerm(String line) {

		Matcher matcher = regexPattern.matcher(line);
		if (matcher.matches()) {

			String writtenForm = matcher.group(1);
			String readingForm = matcher.group(3);
			String definition = matcher.group(4);

			if (readingForm == null) {
				readingForm = writtenForm;
			}

			dictionary.put(writtenForm, line);
		}
	}

	public String lookup(String writtenForm) {

		return dictionary.containsKey(writtenForm) ? dictionary.get(writtenForm) : "";
	}
}
