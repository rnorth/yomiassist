/**
 * Copyright 2011 Richard North
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.yomiassist.sourcedata;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.yomiassist.Options;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.google.common.io.LineProcessor;

public class KnownWords {

	private Set<String> knownWords;

	public void load(Options options) throws IOException {
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

	public Set<String> getKnownWords() {
		return knownWords;
	}

}
