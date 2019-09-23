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

import java.io.File;
import java.util.List;

import javax.swing.SwingWorker;

import io.ortis.jsafebox.Folder;
import io.ortis.jsafebox.Safe;
import io.ortis.jsafebox.cli.Add;
import io.ortis.jsafebox.gui.old.SafeExplorer;
import io.ortis.jsafebox.task.MultipartTask;
import io.ortis.jsafebox.task.TaskProbeAdapter;

public class AddTask extends MultipartTask implements GuiTask
{

	private final SafeExplorer safeExplorer;
	private final Safe safe;
	private final List<File> sources;
	private final Folder folder;

	public AddTask(final SafeExplorer safeExplorer, final List<File> sources, final Folder folder)
	{

		this.safeExplorer = safeExplorer;
		this.safe = this.safeExplorer.getSafe();
		this.sources = sources;
		this.folder = folder;
	}

	public void start()
	{

		this.fireMessage("Initalizing transfert...");
		this.fireProgress(0);
		new SwingWorker<Void, String>()
		{

			@Override
			protected Void doInBackground() throws Exception
			{

				AddTask.this.setSilentSubTask(true);

				final double step = 1d / sources.size();
				double progress = 0;

				for (final File source : sources)
				{
					AddTask.this.fireProgress(progress);
					AddTask.this.fireMessage("Encrypting " + source + "...");
					final TaskProbeAdapter adapter = new TaskProbeAdapter();
					AddTask.this.setSubTask(adapter);
					try
					{
						Add.add(source, null, safe, folder,null, adapter);

					} catch (final Exception e)
					{
						safe.discardChanges();

						if (AddTask.this.isCancelRequested())
						{
							AddTask.this.fireCanceled();
						}
						return null;

					} finally
					{
						AddTask.this.setSubTask(null);
					}

					progress += step;

				}

				return null;
			}

			@Override
			protected void done()
			{
				if (AddTask.this.getException() == null && !AddTask.this.isCancelled())
				{
					AddTask.this.fireProgress(1);
					safeExplorer.notifyModificationPending();

				}

				AddTask.this.fireTerminated();
			}

		}.execute();

	}

}
