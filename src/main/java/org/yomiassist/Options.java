package org.yomiassist;

import java.io.File;

import org.kohsuke.args4j.Option;

public class Options {

	@Option(name="-v", usage="known vocabulary source file", required=true)
	public File vocabularySourceFile;
	
	@Option(name="-o", usage="output file", required=true)
	public File outputFile;
	
	@Option(name="-i", usage="input file", required=true)
	public File inputFile;
}
