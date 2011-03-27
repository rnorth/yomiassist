package org.yomiassist;

import org.jdom.Element;
import org.yomiassist.exception.YomiassistProcessingException;
import org.yomiassist.sourcedata.Edict;

/**
 * TODO: Refactor away from exposing JDOM Elements.
 * 
 * Represents a source document that is to be processed by Yomiassist.
 * 
 * @author Richard North <rich.north+yomiassist@gmail.com>
 * 
 */
public interface SourceDocument {

	/**
	 * @return the title of the document
	 * @throws YomiassistProcessingException
	 */
	String getSourceDocumentTitle() throws YomiassistProcessingException;

	/**
	 * @return the author of the document
	 * @throws YomiassistProcessingException
	 */
	String getSourceDocumentAuthor() throws YomiassistProcessingException;

	/**
	 * TODO: REFACTOR - the return from this should be completed abstracted away
	 * from JDOM.
	 * 
	 * @return the main text content node
	 * @throws YomiassistProcessingException
	 */
	Element getMainTextNode() throws YomiassistProcessingException;

	/**
	 * Get a Ruby intermediate copy of a specific document node. TODO: REFACTOR
	 * 
	 * @param elementNode
	 * @param edictDictionary
	 * @return
	 */
	Ruby copyForcedRuby(Element elementNode, Edict edictDictionary);

}
