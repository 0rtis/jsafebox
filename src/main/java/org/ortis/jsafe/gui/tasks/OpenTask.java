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

package org.ortis.jsafe.gui.tasks;

import java.io.File;

import javax.swing.SwingWorker;

import org.ortis.jsafe.Safe;
import org.ortis.jsafe.Utils;
import org.ortis.jsafe.gui.LoginFrame;
import org.ortis.jsafe.gui.SafeExplorer;
import org.ortis.jsafe.task.MultipartTask;

public class OpenTask extends MultipartTask implements GuiTask
{

	private final File safe;
	private char [] password;
	private final LoginFrame loginFrame;

	public OpenTask(final File safe, final char [] password, final LoginFrame loginFrame)
	{
		this.safe = safe;
		this.password = password;
		this.loginFrame = loginFrame;

	}

	public void start()
	{

		this.fireMessage("Opening safe...");
		this.fireProgress(Double.NaN);
		new SwingWorker<Safe, String>()
		{

			@Override
			protected Safe doInBackground() throws Exception
			{

				OpenTask.this.fireProgress(Double.NaN);

				try
				{
					final Safe safe = Utils.open(OpenTask.this.safe.getAbsolutePath(), password, 1024 * 8, null);
					return safe;

				} catch (final Exception e)
				{
					OpenTask.this.fireException(e);

				} finally
				{

				}

				return null;
			}

			@Override
			protected void done()
			{

				if (OpenTask.this.getException() == null && !OpenTask.this.isCancelled())
				{

					final SafeExplorer safeExplorer = new SafeExplorer(loginFrame.getConfiguration());

					try
					{
						safeExplorer.setSafe(get());
						loginFrame.getFrame().dispose();
						safeExplorer.getExplorerFrame().setVisible(true);

					} catch (final Exception e)
					{
						OpenTask.this.fireException(e);
					}
				}

				OpenTask.this.fireTerminated();

			}

		}.execute();

	}

}
