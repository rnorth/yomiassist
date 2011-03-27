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

import org.jdom.Element;
import org.jdom.Text;

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