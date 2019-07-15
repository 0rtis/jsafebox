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
import io.ortis.jsafebox.commands.Extract;
import io.ortis.jsafebox.task.Task;
import io.ortis.jsafebox.task.TaskListener;
import io.ortis.jsafebox.task.TaskProbeAdapter;

import javax.xml.bind.DatatypeConverter;
import java.awt.*;
import java.io.File;
import java.util.logging.Logger;

/**
 * @author Ortis
 */
public class HashTask extends AbstractGUITask implements TaskListener
{

	private final Safe safe;

private String hash;
	public HashTask(  final Safe safe,  final Logger log)
	{
		super("Computing integrity hash", "Success", "Integrity hash has been successfully computed !", log);
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
			log.info("Computing integrity hash");
			final TaskProbeAdapter adapter = new TaskProbeAdapter();
			adapter.addListener(this);

			System.gc();
			final byte [] hash = safe.computeHash(adapter);
			System.gc();

			this.hash= DatatypeConverter.printHexBinary(hash);

		}finally
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

	public String getHash()
	{
		return hash;
	}
}
