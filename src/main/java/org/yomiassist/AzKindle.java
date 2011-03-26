package org.yomiassist;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URISyntaxException;
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
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.google.common.io.Resources;
import com.ibm.icu.text.Transliterator;

public class AzKindle {

	private static StringTagger stringTagger;
	private static Edict edictDictionary;
	private static Namespace xhtmlNs = Namespace.getNamespace("xhtml", "http://www.w3.org/1999/xhtml");

	private static final Logger LOGGER = LoggerFactory.getLogger(AzKindle.class);
	
	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {

		final Options options = parseCommandLineArgs(args);

		try {
			KnownWords.loadKnownWords(options);

			edictDictionary = new Edict();

			prepareGosenTagger();

			Document doc = loadSourceFile(options);
			Element mainTextNode = getMainTextNode(doc);
			String title = getSourceDocumentTitle(doc);
			String author = getSourceDocumentAuthor(doc);

			LOGGER.info("About to process book {} by {}", title, author);
			
			List<Ruby> allRuby = new ArrayList<Ruby>();
			final Map<String, Ruby> forcedRubies = new HashMap<String, Ruby>();
			processSourceFile(mainTextNode, allRuby, forcedRubies);

			writeOutputFile(options, title, author, allRuby);
			
			LOGGER.info("Processing complete; {} ruby words identified with {} forced ruby readings", allRuby.size(), forcedRubies.size());
			
		} catch (Exception e) {
			LOGGER.error("An unexpected error occurred during processing: {}", e.getMessage());
			throw e;
		}
	}

	private static Options parseCommandLineArgs(String[] args) {
		final Options options = new Options();
		final CmdLineParser parser = new CmdLineParser(options);
		try {
			parser.parseArgument(args);

		} catch (CmdLineException e) {
			System.err.println(e.getMessage());
			System.err.println("java -jar yomiassist.jar [options...]");
			parser.printUsage(System.err);
			System.exit(-1);
		}
		return options;
	}

	private static Document loadSourceFile(final Options options) throws JDOMException, IOException {
		final SAXBuilder saxBuilder = new SAXBuilder(false);
		saxBuilder.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
		Document doc = saxBuilder.build(options.inputFile);
		return doc;
	}

	private static Element getMainTextNode(Document doc) throws JDOMException {
		return getNodeByXpath("//xhtml:div[@class='main_text']", doc);
	}

	private static String getSourceDocumentAuthor(Document doc) throws JDOMException {
		return getNodeByXpath("//xhtml:h2[@class='author']", doc).getText();
	}

	private static String getSourceDocumentTitle(Document doc) throws JDOMException {
		return getNodeByXpath("//xhtml:h1[@class='title']", doc).getText();
	}

	private static void processSourceFile(Element mainTextNode, List<Ruby> allRuby, Map<String, Ruby> forcedRubies) throws IOException {

		List childNodes = mainTextNode.getContent();
		Iterator iterator = childNodes.iterator();

		while (iterator.hasNext()) {
			Object nextChild = iterator.next();

			if (nextChild instanceof Text) {
				final Text textNode = (Text) nextChild;
				allRuby.addAll(analyzeText(textNode.getText(), forcedRubies));
			} else if (nextChild instanceof Element) {
				final Element elementNode = (Element) nextChild;

				if (elementNode.getName().equals("ruby")) {
					final Ruby forcedRuby = copyForcedRuby(elementNode);
					forcedRubies.put(forcedRuby.getWrittenForm(), forcedRuby);
					allRuby.add(forcedRuby);

				} else {
					allRuby.addAll(analyzeText(elementNode.getText(), forcedRubies));
				}

			}
		}
	}

	private static void writeOutputFile(final Options options, String title, String author, List<Ruby> allRuby) throws IOException {

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

	private static void prepareGosenTagger() {
			File gosenConfigFile = new File("dictionaries/gosen/dictionary.xml");
			String configurationFilename = gosenConfigFile.getAbsolutePath();
			stringTagger = SenFactory.getStringTagger(configurationFilename);
	}

	private static Ruby copyForcedRuby(Element elementNode) {
		String reading = elementNode.getChild("rt", xhtmlNs).getText();
		String writtenForm = elementNode.getChild("rb", xhtmlNs).getText();

		String definition = edictDictionary.lookup(writtenForm);

		LOGGER.debug("Copying an existing ruby: {} with reading: {}", writtenForm, reading);

		return new Ruby(writtenForm, reading, definition);
	}
	
	private static Element getNodeByXpath(String xpathQuery, Document context) throws JDOMException {
			XPath xpath = XPath.newInstance(xpathQuery);
			
			xpath.addNamespace(xhtmlNs);
			return (Element) xpath.selectSingleNode(context);
	}

	private static List<Ruby> analyzeText(String textToAnalyze, Map<String, Ruby> forcedRubies) throws IOException {
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

			if (KnownWords.knownWords.contains(writtenForm)) {
				LOGGER.debug("Written form recognised as known: {}", writtenForm);
				ruby.setKnownDefinition(true);
				ruby.setKnownReading(true);
			}

			rubyFound.add(ruby);
		}

		return rubyFound;
	}

}
