package org.azkindle;

import org.jdom.Element;
import org.w3c.dom.Node;

import net.java.sen.dictionary.Token;

public class Ruby {

	private String writtenForm = null;
	private String reading = null;
	private String definition = null;

	public Ruby(Token writtenForm, String reading, String definition) {
		this.writtenForm = writtenForm.toString();
		this.reading = reading;
		this.definition = definition;
	}

	public Ruby(Token writtenForm, String definition) {
		this.writtenForm = writtenForm.toString();
		this.definition = definition;
	}

	public Ruby(Token writtenForm) {
		this.writtenForm = writtenForm.toString();
	}

	public Element toNode() {
		Element element = new Element("ruby");
		
		if (definition != null) {
			element.setAttribute("definition", definition);
		}
		
		if (reading != null) {
			final Element rtChildNode = new Element("rt");
			rtChildNode.setText(reading);
			element.addContent(rtChildNode);
		}
		
		element.setText(writtenForm);
		
		return element;
	}
}