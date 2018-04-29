/*******************************************************************************
 * Copyright 2018 Ortis (cao.ortis.org@gmail.com)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under the License.
 ******************************************************************************/

package org.ortis.jsafe.commands;

import java.io.ByteArrayOutputStream;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.logging.Logger;

import org.ortis.jsafe.Environment;
import org.ortis.jsafe.Safe;
import org.ortis.jsafe.SafeFile;
import org.ortis.jsafe.SafeFiles;
import org.ortis.jsafe.Utils;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

/**
 * GUI starter
 * 
 * @author Ortis <br>
 *         2018 Apr 26 8:16:54 PM <br>
 */
@Command(description = "Outpt text file content", name = "cat", mixinStandardHelpOptions = true, version = Bootstrap.VERSION, showDefaultValues = true)
public class Cat implements Callable<Void>
{

	@Option(names = { "-pw", "-pwd", "--password" }, description = "Password")
	private String password;

	@Option(names = { "-b", "--buffer" }, description = "Read buffer size")
	private int bufferSize = 1024;

	@Parameters(index = "0", arity = "0...1", description = "File path of safe file")
	private String safeFile;

	@Parameters(index = "1", arity = "1...*", description = "Paths of safe's files to cat")
	private String [] paths;

	@Override
	public Void call() throws Exception
	{

		final Logger log = Environment.getLogger();

		try (final Safe safe = Utils.open(this.safeFile, this.password.toCharArray(), this.bufferSize, log))
		{

			final Set<SafeFile> matches = new LinkedHashSet<>();

			for (int i = 0; i < this.paths.length - 1; i++)
				SafeFiles.match(this.paths[i], safe.getRootFolder(), safe.getRootFolder(), matches);

			for (final SafeFile safeFile : matches)
			{

				if (!safeFile.isBlock())
				{
					log.warning(safeFile + " is not a block");

				} else
				{
					final ByteArrayOutputStream baos = new ByteArrayOutputStream();

					safe.extract(safeFile.getPath(), baos);
					final StringBuilder sb = new StringBuilder("\n");
					sb.append(new String(baos.toByteArray()));
					log.info(safeFile + " -> " + sb.toString());
				}

			}

		} catch (final Exception e)
		{
			log.severe(Utils.formatException(e));
		}

		return null;
	}

}
