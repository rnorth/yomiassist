package org.azkindle;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Text;
import org.jdom.input.SAXBuilder;
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

		String textToAnalyze = "これは日本語のテスト。新幹線で東京へ行くつもりです。鍋じゃないなら行かない";

		Document doc = new SAXBuilder(false).build(new File("rashomon.html"));
		Element mainTextNode = (Element) XPath.selectNodes(doc.getRootElement(), "//div[@class='main_text']").get(0);

		List childNodes = mainTextNode.getContent();
		Iterator iterator = childNodes.iterator();
		while (iterator.hasNext()) {
			Object nextChild = iterator.next();

			if (nextChild instanceof Text) {
				analyzeText(((Text) nextChild).getText());
			} else if (nextChild instanceof Element) {
				analyzeText(((Element) nextChild).getText());
			}

		}

	}

	private static void analyzeText(String textToAnalyze) throws IOException {
		List<Token> analysis = stringTagger.analyze(textToAnalyze);

		Transliterator tx = Transliterator.getInstance("Katakana-Hiragana");

		for (Token token : analysis) {

			String ruby;
			if (token.getMorpheme().getReadings().size() > 0) {
				String katakanaReading = token.getMorpheme().getReadings().get(0);
				String hiraganaReading = tx.transform(katakanaReading);

				String baseForm = token.getMorpheme().getBasicForm();
				String definition = edictDictionary.lookup(baseForm);
				if (definition == null)
					definition = "";
				definition = definition.replace("'", "\"");

				if (!token.toString().equals(hiraganaReading) && !token.toString().equals(katakanaReading)) {
					ruby = "<ruby definition='" + definition + "'>" + token + "<rt>" + hiraganaReading + "</rt></ruby>";
				} else {
					ruby = "<ruby definition='" + definition + "'>" + token + "</ruby>";
				}
			} else {
				ruby = "<ruby>" + token + "</ruby>";
			}

			Files.append(ruby + '\n', outputFile, Charsets.UTF_8);
		}
	}

}
