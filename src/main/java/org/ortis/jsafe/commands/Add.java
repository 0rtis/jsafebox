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
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.Callable;
import java.util.logging.Logger;

import org.ortis.jsafe.Block;
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
 * Add file to the {@link Safe}
 * 
 * @author Ortis <br>
 *         2018 Apr 26 8:17:40 PM <br>
 */
@Command(description = "Add file", name = "add", mixinStandardHelpOptions = true, version = Bootstrap.VERSION, showDefaultValues = true)
public class Add implements Callable<Void>
{

	@Option(names = { "-pw", "-pwd", "--password" }, required = true, description = "Password")
	private String password;

	@Option(names = { "-p", "-pp", "--property" }, arity = "2", description = "Encrypted property's key and value")
	private String [] properties;

	@Option(names = { "-m", "--mkdir" }, description = "Create destination folder if necessary")
	private boolean mkdir;

	@Option(names = { "-b", "--buffer" }, description = "Read buffer size")
	private int bufferSize = 1024;

	@Parameters(index = "0", description = "System path of safe file")
	private String safeFile;

	@Parameters(index = "1", arity = "2...*", description = "Paths of system's source files followed by the safe's destination folder")
	private String [] paths;

	@Override
	public Void call() throws Exception
	{

		final Logger log = Environment.getLogger();

		try (final Safe safe = Utils.open(this.safeFile, this.password.toCharArray(), this.bufferSize, log))
		{

			final String destination = this.paths[this.paths.length - 1];

			SafeFile file = SafeFiles.get(destination, safe.getRootFolder(), safe.getRootFolder());

			if (file == null)
				if (this.mkdir)
				{
					SafeFiles.mkdir(destination, false, safe.getRootFolder(), safe.getRootFolder());
					file = SafeFiles.get(destination, safe.getRootFolder(), safe.getRootFolder());
				} else
					throw new Exception("Destination folder '" + destination + "' does not exists");

			final Folder folder = (Folder) file;

			final Map<String, String> properties = new HashMap<>();
			if (this.properties != null)
				for (int i = 0; i < this.properties.length; i += 2)
				{
					final String key = this.properties[i];
					final String value = this.properties[i + 1];
					log.info("Adding property entry '" + key + "' -> '" + value+"'");
					properties.put(key, value);
				}

			final java.util.List<File> sources = new ArrayList<>();

			for (int i = 0; i < this.paths.length - 1; i++)
				Utils.parseSystemPath(this.paths[i], sources);

			for (final File source : sources)
			{
				if (!source.exists())
					throw new Exception("File '" + source + "' does not exist");

				if (!source.isFile())
					throw new Exception("Path '" + source + "' is not a file");

			}

			for (final File source : sources)
			{
				final Map<String, String> props = new TreeMap<>(properties);

				props.put(Block.PATH_LABEL, SafeFiles.sanitize(folder.getPath() + Folder.DELIMITER + source.getName()));

				props.put(Block.NAME_LABEL, source.getName());
				props.put("content-type", Utils.getMIMEType(source));
				log.info("Encrypting " + source + " to " + props.get(Block.PATH_LABEL));
				
				final FileInputStream fis = new FileInputStream(source);
				safe.add(props, fis);
				fis.close();
			}

			log.info("Writting safe file...");
			safe.save().close();;
			log.info("Done");

		} catch (final Exception e)
		{
			log.severe(Utils.formatException(e));
		}

		return null;
	}

}
