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

import java.io.File;

import org.kohsuke.args4j.Option;

/**
 * Configuration holder, used to pass configuration options in to
 * {@link YomiassistProcessor}. Annotated fields are processed by args4j.
 * 
 * @author Richard North <rich.north+yomiassist@gmail.com>
 * 
 */
public class Options {

	@Option(name = "-v", usage = "known vocabulary source file", required = true)
	public File vocabularySourceFile;

	@Option(name = "-o", usage = "output file", required = true)
	public File outputFile;

	@Option(name = "-i", usage = "input file", required = true)
	public File inputFile;
}
