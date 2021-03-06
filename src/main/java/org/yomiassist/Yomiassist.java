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

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.yomiassist.exception.YomiassistProcessingException;
import org.yomiassist.exception.YomiassistSetupException;

/**
 * Command line entry point for Yomiassist. Delegates main processing to
 * {@link YomiassistProcessor}.
 * 
 * @author Richard North <rich.north+yomiassist@gmail.com>
 * 
 */
public class Yomiassist {

	/**
	 * Execute Yomiassist from the command line.
	 * 
	 * Command line parameters are defined in {@link Options}.
	 * 
	 * @param args
	 * @throws YomiassistProcessingException
	 * @throws YomiassistSetupException 
	 */
	public static void main(String[] args) throws YomiassistProcessingException, YomiassistSetupException {

		final Options options = Yomiassist.parseCommandLineArgs(args);

		new YomiassistProcessor(options).process();
	}

	/**
	 * 
	 * @param args
	 *            String arguments as passed in to process
	 * @return A parsed and validated {@link Options} settings holder
	 */
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
