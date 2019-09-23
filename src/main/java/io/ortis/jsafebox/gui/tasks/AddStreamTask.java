/*
 *  Copyright 2019 Ortis (ortis@ortis.io)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package io.ortis.jsafebox.gui.tasks;

import io.ortis.jsafebox.Folder;
import io.ortis.jsafebox.Safe;
import io.ortis.jsafebox.SafeFile;
import io.ortis.jsafebox.cli.Add;
import io.ortis.jsafebox.task.Task;
import io.ortis.jsafebox.task.TaskListener;
import io.ortis.jsafebox.task.TaskProbeAdapter;

import java.io.InputStream;
import java.util.logging.Logger;

/**
 * @author Ortis
 */
public class AddStreamTask extends AbstractGUITask implements TaskListener
{
	private final InputStream sourceInputStream;
	private final String sourceName;
	private final Folder folder;
	private final Safe safe;

	private SafeFile added = null;

	public AddStreamTask(final InputStream sourceInputStream, final String sourceName, final Folder folder, final Safe safe, final Logger log)
	{
		super("Copying", "Success", "Data has been successfully copied !", log);

		this.sourceInputStream = sourceInputStream;
		this.sourceName = sourceName;
		this.folder = folder;
		this.safe = safe;
	}

	@Override
	public boolean skipResultOnSuccess()
	{
		return true;
	}

	@Override
	public void task() throws Exception
	{
		try
		{
			log.info("Initializing transfer");
			final TaskProbeAdapter adapter = new TaskProbeAdapter();
			adapter.addListener(this);

			log.info("Copying " + sourceName);
			this.added = Add.add(sourceInputStream, this.sourceName, null, safe, folder, adapter);
		} catch(final Exception e)
		{
			safe.discardChanges();
			throw e;
		} finally
		{

		}
	}

	public SafeFile getAdded()
	{
		return added;
	}

	@Override
	public void onProgress(final Task task, final double progress)
	{

	}

	@Override
	public void onMessage(final Task task, final String message)
	{
		log.info(message);
	}

	@Override
	public void onException(final Task task, final Exception exception)
	{

	}

	@Override
	public void onTerminated(final Task task)
	{

	}

	@Override
	public void onCancellationRequested(final Task task)
	{

	}

	@Override
	public void onCancelled(final Task task)
	{

	}
}
