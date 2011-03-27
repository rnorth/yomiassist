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
package org.yomiassist;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.java.sen.SenFactory;
import net.java.sen.StringTagger;
import net.java.sen.dictionary.Token;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.Text;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;
import org.jdom.xpath.XPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yomiassist.exception.YomiassistProcessingException;
import org.yomiassist.sourcedata.Edict;
import org.yomiassist.sourcedata.KnownWords;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.ibm.icu.text.Transliterator;

public class YomiassistProcessor {

	private static StringTagger stringTagger;
	private static Namespace xhtmlNs = Namespace.getNamespace("xhtml", "http://www.w3.org/1999/xhtml");

	private static final Logger LOGGER = LoggerFactory.getLogger(YomiassistProcessor.class);
	
	public void process(Options options) throws YomiassistProcessingException {
		try {
			KnownWords knownWords = new KnownWords();
			knownWords.load(options);

			Edict edictDictionary = new Edict();

			prepareGosenTagger();

			Document doc = loadSourceFile(options);
			Element mainTextNode = getMainTextNode(doc);
			String title = getSourceDocumentTitle(doc);
			String author = getSourceDocumentAuthor(doc);

			LOGGER.info("About to process book {} by {}", title, author);
			
			List<Ruby> allRuby = new ArrayList<Ruby>();
			final Map<String, Ruby> forcedRubies = new HashMap<String, Ruby>();
			processSourceFile(mainTextNode, allRuby, forcedRubies, edictDictionary, knownWords);

			writeOutputFile(options, title, author, allRuby);
			
			LOGGER.info("Processing complete; {} ruby words identified with {} forced ruby readings", allRuby.size(), forcedRubies.size());
			
		} catch (Exception e) {
			LOGGER.error("An unexpected error occurred during processing: {}", e.getMessage());
			throw new YomiassistProcessingException(e);
		}
	}

	private Document loadSourceFile(final Options options) throws JDOMException, IOException {
		final SAXBuilder saxBuilder = new SAXBuilder(false);
		saxBuilder.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
		Document doc = saxBuilder.build(options.inputFile);
		return doc;
	}

	private Element getMainTextNode(Document doc) throws JDOMException {
		return getNodeByXpath("//xhtml:div[@class='main_text']", doc);
	}

	private String getSourceDocumentAuthor(Document doc) throws JDOMException {
		return getNodeByXpath("//xhtml:h2[@class='author']", doc).getText();
	}

	private String getSourceDocumentTitle(Document doc) throws JDOMException {
		return getNodeByXpath("//xhtml:h1[@class='title']", doc).getText();
	}

	private void processSourceFile(Element mainTextNode, List<Ruby> allRuby, Map<String, Ruby> forcedRubies, Edict edictDictionary, KnownWords knownWords) throws IOException {

		List<?> childNodes = mainTextNode.getContent();
		Iterator<?> iterator = childNodes.iterator();

		while (iterator.hasNext()) {
			Object nextChild = iterator.next();

			if (nextChild instanceof Text) {
				final Text textNode = (Text) nextChild;
				allRuby.addAll(analyzeText(textNode.getText(), forcedRubies, edictDictionary, knownWords));
			} else if (nextChild instanceof Element) {
				final Element elementNode = (Element) nextChild;

				if (elementNode.getName().equals("ruby")) {
					final Ruby forcedRuby = copyForcedRuby(elementNode, edictDictionary);
					forcedRubies.put(forcedRuby.getWrittenForm(), forcedRuby);
					allRuby.add(forcedRuby);

				} else {
					allRuby.addAll(analyzeText(elementNode.getText(), forcedRubies, edictDictionary, knownWords));
				}

			}
		}
	}

	private void writeOutputFile(final Options options, String title, String author, List<Ruby> allRuby) throws IOException {

		options.outputFile.delete();
		options.outputFile.createNewFile();

		String headerContents = new String(Files.toByteArray(new File("templates/header.txt")));
		headerContents = headerContents.replaceAll("%title%", title);
		headerContents = headerContents.replaceAll("%author%", author);
		Files.append(headerContents, options.outputFile, Charsets.UTF_8);

		XMLOutputter xmlOutputter = new XMLOutputter();
		for (Ruby r : allRuby) {
			Files.append(xmlOutputter.outputString(r.toNode()) + '\n', options.outputFile, Charsets.UTF_8);
		}

		Files.append(new String(Files.toByteArray(new File("templates/footer.txt"))), options.outputFile, Charsets.UTF_8);
	}

	private void prepareGosenTagger() {
			File gosenConfigFile = new File("dictionaries/gosen/dictionary.xml");
			String configurationFilename = gosenConfigFile.getAbsolutePath();
			stringTagger = SenFactory.getStringTagger(configurationFilename);
	}

	private Ruby copyForcedRuby(Element elementNode, Edict edictDictionary) {
		String reading = elementNode.getChild("rt", xhtmlNs).getText();
		String writtenForm = elementNode.getChild("rb", xhtmlNs).getText();

		String definition = edictDictionary.lookup(writtenForm);

		LOGGER.debug("Copying an existing ruby: {} with reading: {}", writtenForm, reading);

		return new Ruby(writtenForm, reading, definition);
	}
	
	private Element getNodeByXpath(String xpathQuery, Document context) throws JDOMException {
			XPath xpath = XPath.newInstance(xpathQuery);
			
			xpath.addNamespace(xhtmlNs);
			return (Element) xpath.selectSingleNode(context);
	}

	private List<Ruby> analyzeText(String textToAnalyze, Map<String, Ruby> forcedRubies, Edict edictDictionary, KnownWords knownWords) throws IOException {
		List<Token> analysis = stringTagger.analyze(textToAnalyze);

		Transliterator tx = Transliterator.getInstance("Katakana-Hiragana");
		List<Ruby> rubyFound = new ArrayList<Ruby>();

		for (Token token : analysis) {

			String writtenForm = token.toString();
			Ruby ruby;
			final boolean hasReadings = token.getMorpheme().getReadings().size() > 0;
			final boolean previouslyForcedRuby = forcedRubies.containsKey(writtenForm);

			if (previouslyForcedRuby) {
				ruby = forcedRubies.get(writtenForm);
			} else if (hasReadings) {
				String katakanaReading = token.getMorpheme().getReadings().get(0);
				String reading = tx.transform(katakanaReading);

				String baseForm = token.getMorpheme().getBasicForm();
				String definition = edictDictionary.lookup(baseForm);

				if (!writtenForm.equals(reading) && !writtenForm.equals(katakanaReading)) {
					ruby = new Ruby(writtenForm, reading, definition);
				} else {
					ruby = new Ruby(writtenForm, definition);
				}
			} else {
				ruby = new Ruby(writtenForm);
			}

			if (knownWords.getKnownWords().contains(writtenForm)) {
				LOGGER.debug("Written form recognised as known: {}", writtenForm);
				ruby.setKnownDefinition(true);
				ruby.setKnownReading(true);
			}

			rubyFound.add(ruby);
		}

		return rubyFound;
	}

}
