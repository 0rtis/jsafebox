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

package io.ortis.jsafebox.gui;

import io.ortis.jsafebox.Utils;

import javax.swing.*;
import java.text.DecimalFormat;

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
		while(!Thread.interrupted())
		{
			try
			{
				update();
				Thread.sleep(3000);

			} catch(final Exception e)
			{

			}

		}
	}

	private void update()
	{
		final long used = runtime.totalMemory() - runtime.freeMemory();
		final long free = runtime.freeMemory();
		final long max = runtime.maxMemory();
		final long total = free + used;
		final double ratio = 100 * ((double) used) / total;

		final StringBuilder sb = new StringBuilder();
		sb.append("JVM memory: ");
		sb.append(Utils.humanReadableByteCount(used)).append("/").append(Utils.humanReadableByteCount(total)).append(", ");
		sb.append(Utils.humanReadableByteCount(max)).append(" max");

		this.label.setText(sb.toString());
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
