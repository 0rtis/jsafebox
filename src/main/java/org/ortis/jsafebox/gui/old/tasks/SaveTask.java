/*******************************************************************************
 * Copyright 2018 Ortis (cao.ortis.org@gmail.com)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package org.ortis.jsafebox.gui.old.tasks;

import javax.swing.SwingWorker;

import org.ortis.jsafebox.Safe;
import org.ortis.jsafebox.gui.old.SafeExplorer;
import org.ortis.jsafebox.task.MultipartTask;
import org.ortis.jsafebox.task.TaskProbeAdapter;

public class SaveTask extends MultipartTask implements GuiTask
{

	private final SafeExplorer safeExplorer;
	private final Safe safe;

	public SaveTask(final SafeExplorer safeExplorer)
	{

		this.safeExplorer = safeExplorer;
		this.safe = this.safeExplorer.getSafe();
	}

	public void start()
	{
		final TaskProbeAdapter adapter = new TaskProbeAdapter();
		this.fireMessage("Saving safe...");
		this.fireProgress(0);
		new SwingWorker<Void, String>()
		{
			private Safe newSafe = null;

			@Override
			protected Void doInBackground() throws Exception
			{

				try
				{

					SaveTask.this.setSubTask(adapter);
					newSafe = safe.save(adapter);

				} catch (final Exception e)
				{
					//safe.discardChanges();
					return null;

				} finally
				{
					SaveTask.this.setSubTask(null);
				}

				return null;
			}

			@Override
			protected void done()
			{

				if (SaveTask.this.getException() == null)
				{
					SaveTask.this.fireProgress(1);
					SaveTask.this.fireMessage("Updating tree...");

					if (!SaveTask.this.isCancelled() && SaveTask.this.getException() == null)
						safeExplorer.setSafe(newSafe);

				}

				SaveTask.this.fireTerminated();
			}

		}.execute();

	}

}
