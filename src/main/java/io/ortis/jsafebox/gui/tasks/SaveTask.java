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
import io.ortis.jsafebox.commands.Add;
import io.ortis.jsafebox.task.Task;
import io.ortis.jsafebox.task.TaskListener;
import io.ortis.jsafebox.task.TaskProbeAdapter;

import java.awt.*;
import java.io.File;
import java.util.List;
import java.util.logging.Logger;

/**
 * @author Ortis
 */
public class SaveTask extends AbstractGUITask implements TaskListener
{
	private final Safe safe;

	private Safe newSafe;

	public SaveTask(final Safe safe, final Logger log)
	{
		super("Saving safe", "Success", "Safe file has been successfully saved !", log);

		this.safe = safe;
	}

	@Override
	public boolean skipResultOnSuccess()
	{
		return false;
	}

	@Override
	public void task() throws Exception
	{
		try
		{
			log.info("Writing safe file");
			final TaskProbeAdapter adapter = new TaskProbeAdapter();
			adapter.addListener(this);
			this.newSafe = safe.save(adapter);
		} catch(final Exception e)
		{
			safe.discardChanges();
			throw  e;
		} finally
		{

		}
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

	public Safe getNewSafe()
	{
		return newSafe;
	}
}
