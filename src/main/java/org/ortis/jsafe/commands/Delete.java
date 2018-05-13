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

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.logging.Logger;

import org.ortis.jsafe.Environment;
import org.ortis.jsafe.Folder;
import org.ortis.jsafe.Safe;
import org.ortis.jsafe.SafeFile;
import org.ortis.jsafe.SafeFiles;
import org.ortis.jsafe.Utils;
import org.ortis.jsafe.task.Task;
import org.ortis.jsafe.task.TaskListener;
import org.ortis.jsafe.task.TaskProbe;
import org.ortis.jsafe.task.TaskProbeAdapter;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

/**
 * Add file to the {@link Safe}
 * 
 * @author Ortis <br>
 *         2018 Apr 26 8:17:40 PM <br>
 */
@Command(description = "Delete file", name = "rm", mixinStandardHelpOptions = true, version = Bootstrap.VERSION, showDefaultValues = true)
public class Delete implements Callable<Void>
{

	@Option(names = { "-pw", "-pwd", "--password" }, required = true, description = "Password")
	private String password;

	@Option(names = { "-f", "--force" }, description = "Force delete")
	private boolean force;

	@Option(names = { "-b", "--buffer" }, description = "Read buffer size")
	private int bufferSize = 1024;

	@Parameters(index = "0", description = "System path of safe file")
	private String safeFile;

	@Parameters(index = "1", arity = "1...*", description = "Safe's paths to delete")
	private String [] paths;

	@Override
	public Void call() throws Exception
	{

		final Logger log = Environment.getLogger();

		try (final Safe safe = Utils.open(this.safeFile, this.password.toCharArray(), this.bufferSize, log))
		{

			final java.util.Set<SafeFile> safeFiles = new LinkedHashSet<>();

			for (final String path : this.paths)
			{
				log.fine("Lookup " + path + "...");
				SafeFiles.match(path, safe.getRootFolder(), safe.getRootFolder(), safeFiles);
			}

			if (safeFiles.isEmpty())
			{
				log.info("No file found");
				return null;
			}

			final TaskProbeAdapter adapter = new TaskProbeAdapter();

			adapter.addListener(new TaskListener()
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

			for (final SafeFile safeFile : safeFiles)
			{

				if (safeFile.isBlock())
					delete(safe, safeFile, adapter);
				else if (safeFile.isFolder())
				{
					final Folder folder = (Folder) safeFile;

					if (!folder.listFiles().isEmpty())
					{
						if (this.force)
							delete(safe, safeFile, adapter);
						else
							log.severe("Cannot delete non empty folder '" + folder + "'");
					}

				}
			}

			log.info("Writting safe file...");
			safe.save().close();
			log.info("Done");

		} catch (final Exception e)
		{
			log.severe(Utils.formatException(e));
		}

		return null;
	}

	public static void delete(final Safe safe, final SafeFile safeFile, TaskProbe probe) throws Exception
	{
		try
		{
			if (probe == null)
				probe = TaskProbe.DULL_PROBE;

			if (probe.isCancelRequested())
				throw new CancellationException();

			probe.fireProgress(Double.NaN);

			if (safeFile.isBlock())
			{
				probe.fireMessage("Deleting block " + safeFile);
				safe.delete(safeFile.getPath());
			} else
			{

				final Folder folder = (Folder) safeFile;
				final List<SafeFile> safeFiles = new ArrayList<>(folder.listFiles());
				for (final SafeFile sf : safeFiles)
					delete(safe, sf, probe);

				folder.getParent().remove(folder.getName());
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
