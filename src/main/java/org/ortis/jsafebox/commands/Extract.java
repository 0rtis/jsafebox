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
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;
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
import org.ortis.jsafebox.task.Task;
import org.ortis.jsafebox.task.TaskListener;
import org.ortis.jsafebox.task.TaskProbe;
import org.ortis.jsafebox.task.TaskProbeAdapter;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

/**
 * Extract a {@link Block} to local drive
 * 
 * @author Ortis <br>
 *         2018 Apr 26 8:16:54 PM <br>
 */
@Command(description = "Extract file from the safe", name = "extract", mixinStandardHelpOptions = true, version = Safe.VERSION, showDefaultValues = true)
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

			try (final Safe safe = Safe.open(this.safeFile, this.password.toCharArray(), this.bufferSize, log))
			{

				final Set<SafeFile> matches = new LinkedHashSet<>();

				for (int i = 0; i < this.paths.length - 1; i++)
					SafeFiles.match(this.paths[i], safe.getRootFolder(), safe.getRootFolder(), matches);

				if (matches.isEmpty())
				{
					log.info("No file found");
					return null;
				}

				final TaskProbeAdapter adapater = new TaskProbeAdapter();
				adapater.addListener(new TaskListener()
				{

					@Override
					public void onTerminated(Task task)
					{
					}

					@Override
					public void onProgress(Task task, double progress)
					{
					}

					@Override
					public void onMessage(final Task task, final String message)
					{
						log.info(message);
					}

					@Override
					public void onException(Task task, Exception exception)
					{
					}

					@Override
					public void onCancelled(Task task)
					{
					}

					@Override
					public void onCancellationRequested(Task task)
					{
					}
				});

				for (final SafeFile safeFile : matches)
				{

					final File systemFile = new File(destinationFolder, safeFile.getName());
					log.info("Extracting " + safeFile + " to " + systemFile);
					extract(safe, safeFile, destinationFolder, adapater);

				}
			}

		} catch (final Exception e)
		{
			log.severe(Utils.formatException(e));
		}

		return null;
	}

	public static void extract(final Safe safe, final SafeFile safeFile, final File directory, TaskProbe probe) throws Exception
	{

		try
		{
			if (probe == null)
				probe = TaskProbe.DULL_PROBE;

			if (probe.isCancelRequested())
				throw new CancellationException();

			probe.fireProgress(Double.NaN);

			if (!directory.isDirectory())
				throw new Exception("Destination must be a directory");

			if (safeFile.isFolder())
			{

				final Folder folder = (Folder) safeFile;
				final File targetDirectory = new File(directory, safeFile.getName());
				if (!targetDirectory.exists())
				{
					probe.fireMessage("Creating directory " + targetDirectory.getAbsolutePath());
					if (!targetDirectory.mkdirs())
						throw new IOException("Could not create directory " + targetDirectory);
				}

				for (final SafeFile sf : folder.listFiles())
					extract(safe, sf, targetDirectory, probe);

			} else
			{

				final File file = new File(directory, safeFile.getName());
				probe.fireMessage("Extracting block " + safeFile.getName() + " to " + file.getAbsolutePath());
				final FileOutputStream fos = new FileOutputStream(file);
				safe.extract(safeFile.getPath(), fos);
				fos.close();
			}
		} catch (final CancellationException e)
		{
			probe.fireCanceled();
			throw e;
		} catch (final Exception e)
		{
			throw e;
		} finally
		{
			probe.fireTerminated();
		}

	}
}
