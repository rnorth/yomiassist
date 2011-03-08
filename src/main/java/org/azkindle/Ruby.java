package org.azkindle;

import org.jdom.Element;
import org.jdom.Text;
import org.w3c.dom.Node;

import net.java.sen.dictionary.Token;

public class Ruby {

	private String writtenForm = null;
	private String reading = null;
	private String definition = null;

	public Ruby(String writtenForm, String reading, String definition) {
		this.writtenForm = writtenForm.toString();
		this.reading = reading;
		this.definition = definition;
	}

	public Ruby(String writtenForm, String definition) {
		this.writtenForm = writtenForm;
		this.definition = definition;
	}

	public Ruby(String writtenForm) {
		this.writtenForm = writtenForm;
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
		
		element.addContent(new Text(writtenForm));
		
		return element;
	}

	public String getWrittenForm() {
		return writtenForm;
	}

	public String getReading() {
		return reading;
	}

	public String getDefinition() {
		return definition;
	}
	
	
}