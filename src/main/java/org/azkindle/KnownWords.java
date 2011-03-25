package org.azkindle;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.google.common.io.LineProcessor;

public class KnownWords {

	static Set<String> knownWords;

	static void loadKnownWords(Options options) throws IOException {
		File jlptN4Words = options.vocabularySourceFile;
		knownWords = Files.readLines(jlptN4Words, Charsets.UTF_8, new LineProcessor<Set<String>>() {
	
			Set<String> words = new HashSet<String>();
	
			public boolean processLine(String line) throws IOException {
				final boolean kanaOnly = line.indexOf('-') == 0;
				if (kanaOnly) {
					words.add(line.substring(1));
				} else {
					final String kanjiPortion = line.substring(0, line.indexOf('-'));
					String[] kanjiVariants = kanjiPortion.split("\\s");
					for (String variant : kanjiVariants) {
						words.add(kanjiPortion);
					}
				}
				return true;
			}
	
			public Set<String> getResult() {
				return words;
			}
		});
	}

}
