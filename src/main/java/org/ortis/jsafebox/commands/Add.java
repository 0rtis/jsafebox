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

package org.ortis.jsafebox.commands;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.logging.Logger;

import org.ortis.jsafebox.Block;
import org.ortis.jsafebox.Environment;
import org.ortis.jsafebox.Folder;
import org.ortis.jsafebox.Safe;
import org.ortis.jsafebox.SafeFile;
import org.ortis.jsafebox.SafeFiles;
import org.ortis.jsafebox.Utils;
import org.ortis.jsafebox.task.TaskProbeAdapter;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

/**
 * Add file to the {@link Safe}
 * 
 * @author Ortis <br>
 *         2018 Apr 26 8:17:40 PM <br>
 */
@Command(description = "Add file", name = "add", mixinStandardHelpOptions = true, version = Safe.VERSION, showDefaultValues = true)
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

		try (final Safe safe = Safe.open(this.safeFile, this.password.toCharArray(), this.bufferSize, log))
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
					log.info("Adding property entry '" + key + "' -> '" + value + "'");
					properties.put(key, value);
				}

			final java.util.List<File> sources = new ArrayList<>();

			for (int i = 0; i < this.paths.length - 1; i++)
				Utils.parseSystemPath(this.paths[i], sources);

			for (final File source : sources)
				if (!source.exists())
					throw new Exception("File '" + source + "' does not exist");

			for (final File source : sources)
			{
				final Map<String, String> props = new TreeMap<>(properties);
				log.info("Encrypting " + source);
				add(source, props, safe, folder, null);
			}

			log.info("Writting safebox file...");
			safe.save().close();
			log.info("Done");

		} catch (final Exception e)
		{
			log.severe(Utils.formatException(e));
		}

		return null;
	}

	public static void add(final File source, final Map<String, String> properties, final Safe safe, final Folder folder, TaskProbeAdapter adaper) throws CancellationException, Exception
	{
		if (adaper == null)
			adaper = new TaskProbeAdapter();

		try
		{
			if (source.isDirectory())
			{

				final String name = Utils.sanitizeToken(source.getName(), Environment.getSubstitute());
				final SafeFile sf = folder.getChild(name);
				final Folder currentFolder;
				if (sf == null)
				{
					folder.mkdir(name);
					currentFolder = (Folder) folder.getChild(name);
				} else if (sf.isFolder())
					currentFolder = (Folder) sf;
				else
					throw new Exception("Folder " + folder.getPath() + Folder.DELIMITER + name + " cannot be created. A block with the same path already exists");

				for (final File file : source.listFiles())
					add(file, properties, safe, currentFolder, adaper);

			} else
			{
				final Map<String, String> props = properties == null ? new LinkedHashMap<>() : new LinkedHashMap<>(properties);

				props.put(Block.PATH_LABEL, Utils.sanitize(folder.getPath() + Folder.DELIMITER + source.getName(), Folder.DELIMITER, Environment.getSubstitute()));
				props.put(Block.NAME_LABEL, Utils.sanitizeToken(source.getName(), Environment.getSubstitute()));
				props.put("content-type", Utils.getMIMEType(source));

				
				
				
				adaper.fireMessage("Encrypting " + source + " to " + props.get(Block.PATH_LABEL));

				final FileInputStream fis = new FileInputStream(source);
				safe.add(props, fis, adaper);
				fis.close();
			}
		} catch (final CancellationException e)
		{
			if (!adaper.isCancelled())
				adaper.fireCanceled();
			throw e;
		} catch (final Exception e)
		{
			if (adaper.getException() == null)
				adaper.fireException(e);
			throw e;
		}

	}
}
