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
package io.ortis.jsafebox.gui.old.tasks;

import javax.swing.SwingWorker;

import io.ortis.jsafebox.Safe;
import io.ortis.jsafebox.SafeFile;
import io.ortis.jsafebox.commands.Delete;
import io.ortis.jsafebox.gui.old.SafeExplorer;
import io.ortis.jsafebox.task.MultipartTask;
import io.ortis.jsafebox.task.TaskProbeAdapter;

public class DeleteTask extends MultipartTask implements GuiTask
{

	private final SafeExplorer safeExplorer;
	private final Safe safe;
	private final SafeFile safeFile;

	public DeleteTask(final SafeExplorer safeExplorer, final SafeFile safeFile)
	{

		this.safeExplorer = safeExplorer;
		this.safe = this.safeExplorer.getSafe();
		this.safeFile = safeFile;
	}

	public void start()
	{

		this.fireMessage("Deleting...");
		DeleteTask.this.fireProgress(Double.NaN);
		new SwingWorker<Void, String>()
		{

			@Override
			protected Void doInBackground() throws Exception
			{
				final TaskProbeAdapter adapter = new TaskProbeAdapter();
				DeleteTask.this.setSubTask(adapter);
				try
				{
					Delete.delete(safe, safeFile, adapter);

				} catch (final Exception e)
				{
					safe.discardChanges();

					if (DeleteTask.this.isCancelRequested())
					{
						DeleteTask.this.fireCanceled();
					}
					return null;

				} finally
				{
					DeleteTask.this.setSubTask(null);
				}

				return null;
			}

			@Override
			protected void done()
			{

				if (DeleteTask.this.getException() == null && !DeleteTask.this.isCancelled())
				{
					safeExplorer.notifyModificationPending();
				}

				DeleteTask.this.fireTerminated();

			}

		}.execute();

	}

}
