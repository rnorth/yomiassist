package org.yomiassist;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

public class Yomiassist {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws YomiassistProcessingException {
	
		final Options options = Yomiassist.parseCommandLineArgs(args);
	
		new YomiassistProcessor().process(options);
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

}
