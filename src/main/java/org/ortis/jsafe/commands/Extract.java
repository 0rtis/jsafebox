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

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.logging.Logger;

import org.ortis.jsafe.Environment;
import org.ortis.jsafe.Folder;
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
@Command(description = "Extract file from the safe", name = "extract", mixinStandardHelpOptions = true, version = Bootstrap.VERSION, showDefaultValues = true)
public class Extract implements Callable<Void>
{

	@Option(names = { "-pw", "-pwd", "--password" }, description = "Password")
	private String password;

	@Option(names = { "-b", "--buffer" }, description = "Read buffer size")
	private int bufferSize = 1024;

	@Parameters(index = "0", description = "System path of safe file")
	private String safeFile;

	@Parameters(index = "1", arity = "2...*", description = "Paths of safe's source files followed by the system's destination folder")
	private String [] paths;

	@Override
	public Void call() throws Exception
	{

		final Logger log = Environment.getLogger();

		try
		{

			final String destinationPath = this.paths[this.paths.length - 1];
			final File destinationFolder = new File(destinationPath);

			if (!destinationFolder.exists())
			{
				log.severe("Destination folder '" + destinationFolder + "' does not exists");
				return null;
			}

			if (!destinationFolder.isDirectory())
			{
				log.severe("Destination '" + destinationFolder + "' is not a directory");
				return null;
			}

			try (final Safe safe = Utils.open(this.safeFile, this.password.toCharArray(), this.bufferSize, log))
			{

				final Set<SafeFile> matches = new LinkedHashSet<>();

				for (int i = 0; i < this.paths.length - 1; i++)
					SafeFiles.match(this.paths[i], safe.getRootFolder(), safe.getRootFolder(), matches);

				final Map<String, List<SafeFile>> names = new LinkedHashMap<>();
				for (final SafeFile safeFile : matches)
				{
					if (!safeFile.isBlock())
					{
						log.warning("Skipping extracation of " + safeFile + " (not a block)");
						continue;
					}
					List<SafeFile> safeFiles = names.get(safeFile.getComparableName());
					if (safeFiles == null)
					{
						safeFiles = new ArrayList<>();
						names.put(safeFile.getComparableName(), safeFiles);
					}

					safeFiles.add(safeFile);

				}

				for (final Map.Entry<String, List<SafeFile>> safeFiles : names.entrySet())
					if (safeFiles.getValue().size() > 1)
					{

						for (final SafeFile safeFile : safeFiles.getValue())
						{

							final File systemFile = new File(destinationFolder, safeFile.getName() + "_" + SafeFiles.sanitize(safeFile.getPath()).replace(Folder.DELIMITER, '-'));
							log.info("Extracting " + safeFile + " to " + systemFile);
							final FileOutputStream fos = new FileOutputStream(systemFile);
							safe.extract(safeFile.getPath(), fos);
							fos.close();
						}

					} else
					{

						final SafeFile safeFile = safeFiles.getValue().get(0);
						final File systemFile = new File(destinationFolder, safeFile.getName());
						log.info("Extracting " + safeFile + " to " + systemFile);
						final FileOutputStream fos = new FileOutputStream(systemFile);
						safe.extract(safeFile.getPath(), fos);
						fos.close();
					}
			}

		} catch (final Exception e)
		{
			log.severe(Utils.formatException(e));
		}

		return null;
	}

}
