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
package org.ortis.jsafe.gui;

import java.text.DecimalFormat;

import javax.swing.JLabel;

public class StatusUpdater implements Runnable
{
	private static final DecimalFormat FORMAT = new DecimalFormat("#.00");
	private final JLabel label;

	private final Runtime runtime;

	public StatusUpdater(final JLabel label)
	{
		this.label = label;
		this.runtime = Runtime.getRuntime();

	}

	@Override
	public void run()
	{

		while (!Thread.interrupted())
		{

			try
			{
				update();
				Thread.sleep(1000);

			} catch (final Exception e)
			{

			}

		}

	}

	private void update()
	{

		final long maxMemory = runtime.maxMemory() / 1000;
		final long allocatedMemory = runtime.totalMemory() / 1000;
		//final long freeMemory = runtime.freeMemory() / 1000;

		final double used = 100d * allocatedMemory / maxMemory;
		this.label.setText("  Memory usage: " + allocatedMemory + "/" + maxMemory + " Kb - " + FORMAT.format(used) + " %");
		this.label.setToolTipText(this.label.getText());

	}

	public void start()
	{
		final Thread thread = new Thread(this);

		thread.setName(StatusUpdater.class.getSimpleName());
		thread.setDaemon(true);
		thread.start();
	}

}
