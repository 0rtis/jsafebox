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

package io.ortis.jsafebox.gui.old;

import java.text.DecimalFormat;

import javax.swing.JLabel;

public class StatusUpdater implements Runnable
{
	
	private static final DecimalFormat RATIO_FORMAT = new DecimalFormat("0.00");
	
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

		final long base = 1000;
		final long used = (runtime.totalMemory() - runtime.freeMemory()) / base;
		final long free = runtime.freeMemory() / base;
		final long max = runtime.maxMemory() / base;

		final long total = free + used;
		final double ratio = 100 * ((double) used) / total;

		this.label.setText("  Memory:  " + SafeExplorer.MEMORY_FORMAT.format(used) + " Kb used  " + SafeExplorer.MEMORY_FORMAT.format(free) + " Kb free  " + SafeExplorer.MEMORY_FORMAT.format(max) + " Kb available - "
				+ RATIO_FORMAT.format(ratio) + " %");
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
