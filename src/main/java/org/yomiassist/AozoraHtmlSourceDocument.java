package org.yomiassist;

import java.io.IOException;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;
import org.jdom.xpath.XPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yomiassist.exception.YomiassistProcessingException;
import org.yomiassist.exception.YomiassistSetupException;
import org.yomiassist.sourcedata.Edict;

/**
 * Represents a source document which is an Aozora Bunko HTML-formatted ebook.
 * 
 * @author Richard North <rich.north+yomiassist@gmail.com>
 * 
 */
public class AozoraHtmlSourceDocument implements SourceDocument {

	private static final Logger LOGGER = LoggerFactory.getLogger(AozoraHtmlSourceDocument.class);

	private Document document;
	private static Namespace xhtmlNs = Namespace.getNamespace("xhtml", "http://www.w3.org/1999/xhtml");

	public AozoraHtmlSourceDocument(Options options) throws YomiassistSetupException, YomiassistProcessingException {
		final SAXBuilder saxBuilder = new SAXBuilder(false);
		saxBuilder.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
		try {
			this.document = saxBuilder.build(options.inputFile);
		} catch (JDOMException e) {
			throw new YomiassistProcessingException(e);
		} catch (IOException e) {
			throw new YomiassistSetupException(e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public Element getMainTextNode() throws YomiassistProcessingException {
		return getNodeByXpath("//xhtml:div[@class='main_text']", document);
	}

	/**
	 * {@inheritDoc}
	 */
	public String getSourceDocumentAuthor() throws YomiassistProcessingException {
		return getNodeByXpath("//xhtml:h2[@class='author']", document).getText();
	}

	/**
	 * {@inheritDoc}
	 */
	public String getSourceDocumentTitle() throws YomiassistProcessingException {
		return getNodeByXpath("//xhtml:h1[@class='title']", document).getText();
	}

	private Element getNodeByXpath(String xpathQuery, Document context) throws YomiassistProcessingException {
		try {
			XPath xpath = XPath.newInstance(xpathQuery);

			xpath.addNamespace(xhtmlNs);
			return (Element) xpath.selectSingleNode(context);
		} catch (JDOMException e) {
			throw new YomiassistProcessingException(e);
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	public Ruby copyForcedRuby(Element elementNode, Edict edictDictionary) {
		String reading = elementNode.getChild("rt", xhtmlNs).getText();
		String writtenForm = elementNode.getChild("rb", xhtmlNs).getText();

		String definition = edictDictionary.lookup(writtenForm);

		LOGGER.debug("Copying an existing ruby: {} with reading: {}", writtenForm, reading);

		return new Ruby(writtenForm, reading, definition);
	}
}
