package org.yomiassist;

import java.util.List;
import java.util.Map;

import org.yomiassist.exception.YomiassistProcessingException;
import org.yomiassist.sourcedata.Edict;
import org.yomiassist.sourcedata.KnownWords;

/**
 * A service which can take plain text and convert it into a series of annotated
 * {@link Ruby} objects.
 * 
 * @author Richard North <rich.north+yomiassist@gmail.com>
 * 
 */
public interface TextAnalyzer {

	/**
	 * Analyzes a given piece of text and convert it into a series of annotated
	 * {@link Ruby} objects.
	 * 
	 * @param textToAnalyze
	 *            the text to be analyzed
	 * @param forcedRubies
	 *            any previously encountered 'forced' Ruby (kanji/reading) pairs
	 *            which should continue to be respected
	 * @param edictDictionary
	 *            a dictionary which will provide definitions of words
	 * @param knownWords
	 *            the words which should be considered known by the target
	 *            audience of the resultant ebook.
	 * @return
	 * @throws YomiassistProcessingException
	 */
	public List<Ruby> analyzeText(String textToAnalyze, Map<String, Ruby> forcedRubies, Edict edictDictionary, KnownWords knownWords)
			throws YomiassistProcessingException;

}
