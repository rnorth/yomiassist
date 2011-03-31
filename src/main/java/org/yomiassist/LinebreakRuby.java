package org.yomiassist;

import org.jdom.Element;

public class LinebreakRuby extends Ruby {

	public LinebreakRuby() {
		super();
	}
	
	@Override
	public Element toNode() {
		return new Element("br");
	}
}
