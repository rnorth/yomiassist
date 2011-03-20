package org.azkindle;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.google.common.io.LineProcessor;
import com.ibm.icu.text.Transliterator;

public class AzKindle {

	private static String configurationFilename;
	private static StringTagger stringTagger;
	private static Edict edictDictionary;
	private static Set<String> knownWords;
	private static Namespace ns;
	private static final Map<String, Ruby> forcedRubies = new HashMap<String, Ruby>();

	/**
	 * @param args
	 * @throws IOException
	 * @throws JDOMException
	 */
	public static void main(String[] args) throws IOException, JDOMException {

		final Options options = parseCommandLineArgs(args);

		knownWords = loadKnownWords(options);

		edictDictionary = new Edict();

		prepareGosenTagger();

		Document doc = loadSourceFile(options);
		Element mainTextNode = getMainTextNode(doc);
		String title = getSourceDocumentTitle(doc);
		String author = getSourceDocumentAuthor(doc);

		List<Ruby> allRuby = new ArrayList<Ruby>();
		processSourceFile(mainTextNode, allRuby);

		writeOutputFile(options, title, author, allRuby);
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

	private static Set<String> loadKnownWords(Options options) throws IOException {
		File jlptN4Words = options.vocabularySourceFile;
		return Files.readLines(jlptN4Words, Charsets.UTF_8, new LineProcessor<Set<String>>() {

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

	private static Document loadSourceFile(final Options options) throws JDOMException, IOException {
		final SAXBuilder saxBuilder = new SAXBuilder(false);
		saxBuilder.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
		Document doc = saxBuilder.build(options.inputFile);
		return doc;
	}
	
	private static Element getNodeByXpath(String xpathQuery, Document context) throws JDOMException {
			XPath xpath = XPath.newInstance(xpathQuery);
			Namespace ns = Namespace.getNamespace("xhtml", "http://www.w3.org/1999/xhtml");
			xpath.addNamespace(ns);
			return (Element) xpath.selectSingleNode(context);
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

	private static void processSourceFile(Element mainTextNode, List<Ruby> allRuby) throws IOException {

		List childNodes = mainTextNode.getContent();
		Iterator iterator = childNodes.iterator();

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
	}

	private static void writeOutputFile(final Options options, String title, String author, List<Ruby> allRuby) throws IOException {

		options.outputFile.delete();
		options.outputFile.createNewFile();

		String headerContents = new String(Files.toByteArray(new File("header.txt")));
		headerContents = headerContents.replaceAll("%title%", title);
		headerContents = headerContents.replaceAll("%author%", author);
		Files.append(headerContents, options.outputFile, Charsets.UTF_8);

		XMLOutputter xmlOutputter = new XMLOutputter();
		for (Ruby r : allRuby) {
			Files.append(xmlOutputter.outputString(r.toNode()) + '\n', options.outputFile, Charsets.UTF_8);
		}

		Files.append(new String(Files.toByteArray(new File("footer.txt"))), options.outputFile, Charsets.UTF_8);
	}

	private static void prepareGosenTagger() {
		try {
			configurationFilename = new File(AzKindle.class.getResource("/gosen/dictionary.xml").toURI()).toString();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		stringTagger = SenFactory.getStringTagger(configurationFilename);
	}

	private static Ruby copyForcedRuby(Element elementNode) {
		String reading = elementNode.getChild("rt", ns).getText();
		String writtenForm = elementNode.getChild("rb", ns).getText();

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

			if (knownWords.contains(writtenForm)) {
				System.out.println("Written form recognised as known: " + writtenForm);
				ruby.setKnownDefinition(true);
				ruby.setKnownReading(true);
			}

			rubyFound.add(ruby);
		}

		return rubyFound;
	}

}
