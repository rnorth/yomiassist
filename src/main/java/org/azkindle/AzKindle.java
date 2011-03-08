package org.azkindle;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Text;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;
import org.jdom.xpath.XPath;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.ibm.icu.text.Transliterator;

import net.java.sen.SenFactory;
import net.java.sen.StringTagger;
import net.java.sen.dictionary.Token;

public class AzKindle {

	private static String configurationFilename;
	private static StringTagger stringTagger;
	private static Edict edictDictionary;
	private static final File outputFile = new File("output.txt");
	private static final Map<String,Ruby> forcedRubies = new HashMap<String,Ruby>();

	/**
	 * @param args
	 * @throws IOException
	 * @throws JDOMException
	 */
	public static void main(String[] args) throws IOException, JDOMException {

		edictDictionary = new Edict();

		try {
			configurationFilename = new File(AzKindle.class.getResource("/gosen/dictionary.xml").toURI()).toString();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		stringTagger = SenFactory.getStringTagger(configurationFilename);

		Document doc = new SAXBuilder(false).build(new File("rashomon.html"));
		Element mainTextNode = (Element) XPath.selectNodes(doc.getRootElement(), "//div[@class='main_text']").get(0);

		List childNodes = mainTextNode.getContent();
		Iterator iterator = childNodes.iterator();


		List<Ruby> allRuby = new ArrayList<Ruby>();
		while (iterator.hasNext()) {
			Object nextChild = iterator.next();

			if (nextChild instanceof Text) {
				final Text textNode = (Text) nextChild;
				allRuby.addAll(analyzeText(textNode.getText()));
			} else if (nextChild instanceof Element) {
				final Element elementNode = (Element) nextChild;
				
				if (elementNode.getName().equals("ruby")) {
					final Ruby forcedRuby = copyForcedRuby(elementNode);
					forcedRubies.put(forcedRuby.getWrittenForm(), forcedRuby);
					allRuby.add(forcedRuby);
					
				} else {
					allRuby.addAll(analyzeText(elementNode.getText()));
				}
				
			}
		}

		XMLOutputter xmlOutputter = new XMLOutputter();
		for (Ruby r : allRuby) {
			Files.append(xmlOutputter.outputString(r.toNode()) + '\n', outputFile, Charsets.UTF_8);
		}
	}

	private static Ruby copyForcedRuby(Element elementNode) {
		String reading = elementNode.getChild("rt").getText();
		String writtenForm = elementNode.getChild("rb").getText();
		
		String definition = edictDictionary.lookup(writtenForm);
		
		System.out.println("Copying an existing ruby: " + writtenForm + " " + reading);
		
		return new Ruby(writtenForm, reading, definition);
	}

	private static List<Ruby> analyzeText(String textToAnalyze) throws IOException {
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

			rubyFound.add(ruby);
		}

		return rubyFound;
	}

}
