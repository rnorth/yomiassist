package org.azkindle;

import org.jdom.Attribute;
import org.jdom.Element;
import org.jdom.Text;
import org.w3c.dom.Node;

import net.java.sen.dictionary.Token;

public class Ruby {

	private String writtenForm = null;
	private String reading = null;
	private String definition = null;
	private boolean knownDefinition;
	private boolean knownReading;

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
		
		element.addContent(new Text(writtenForm));
		
		if (definition != null && !knownDefinition) {
			element.setAttribute("definition", definition);
		}
		
		if (reading != null && !knownReading) {
			
			final Element rtChildNode = new Element("rt");
			rtChildNode.setText(reading);
			element.addContent(rtChildNode);
		}
		
		
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

	public void setKnownDefinition(boolean knownDefinition) {
		this.knownDefinition = knownDefinition;
	}

	public void setKnownReading(boolean knownReading) {
		this.knownReading = knownReading;
	}
	
}