package org.yomiassist.exception;


/**
 * Exception relating to a configuration issue in Yomiassist.
 * 
 * @author Richard North <rich.north+yomiassist@gmail.com>
 *
 */
public class YomiassistSetupException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 33954951879015988L;

	public YomiassistSetupException(Exception e) {
		super(e);
	}

}
