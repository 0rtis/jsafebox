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

import io.ortis.jsafebox.Safe;
import io.ortis.jsafebox.SafeFile;
import io.ortis.jsafebox.cli.Delete;
import io.ortis.jsafebox.task.Task;
import io.ortis.jsafebox.task.TaskListener;
import io.ortis.jsafebox.task.TaskProbeAdapter;

import java.util.logging.Logger;

/**
 * @author Ortis
 */
public class DeleteTask extends AbstractGUITask implements TaskListener
{

	private final SafeFile safeFile;
	private final Safe safe;

	public DeleteTask( final SafeFile safeFile, final Safe safe, final Logger log)
	{
		super("Deleting file", "Success", "File has been successfully deleted !", log);

		this.safeFile = safeFile;
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
			log.info("Deleting "+this.safeFile);
			final TaskProbeAdapter adapter = new TaskProbeAdapter();
			adapter.addListener(this);

			Delete.delete(this.safe, this.safeFile, adapter);

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
}
