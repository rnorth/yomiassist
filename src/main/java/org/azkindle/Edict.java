package org.azkindle;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.google.common.io.LineProcessor;
import com.google.common.io.LineReader;

public class Edict {

	private Map<String,String> dictionary = new HashMap<String,String>();
	private Pattern regexPattern = Pattern.compile("([^\\s]+)\\s?(\\[([^\\]]+)\\])?\\s?/(.+)");
	
	public Edict() {
		
		try {
			File edictFile = new File(Edict.class.getResource("/edict/edict.txt").toURI());
			Boolean readLines = Files.readLines(edictFile, Charset.forName("EUC-JP"), new LineProcessor<Boolean>() {

				public boolean processLine(String line) throws IOException {
					addDictionaryTerm(line);
					return true;
				}

				public Boolean getResult() {
					return true;
				}
			});
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void addDictionaryTerm(String line) {
		
		Matcher matcher = regexPattern.matcher(line);
		if (matcher.matches()) {
			
			String writtenForm = matcher.group(1);
			String readingForm = matcher.group(3);
			String definition = matcher.group(4);
			
			if (readingForm==null) {
				readingForm = writtenForm;
			}
			
			dictionary.put(writtenForm, line);
		}
	}
	
	public String lookup(String writtenForm) {
		
		return dictionary.get(writtenForm);
	}
}
