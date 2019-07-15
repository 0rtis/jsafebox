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

import io.ortis.jsafebox.gui.Settings;
import io.ortis.jsafebox.Safe;

import java.awt.*;
import java.util.logging.Logger;

/**
 * @author Ortis
 */
public class OpenSafeboxTask extends AbstractGUITask
{
	private final String safeboxPath;
	private final char[] pwd;

	private Safe safe;

	public OpenSafeboxTask(final String safeboxPath, final char[] pwd, final Logger log)
	{
		super("Opening safebox", "Success", "Safebox successfully opened !", log);

		this.safeboxPath = safeboxPath;
		this.pwd = pwd;
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
			log.info("Reading buffer size");
			final int bufferSize = Settings.getSettings().getInteger(Settings.SAFE_BUFFER_LENGTH_KEY);

			log.info("Opening safebox");
			this.safe = Safe.open(safeboxPath, pwd, bufferSize, log);

		} finally
		{

		}
	}

	public Safe getSafe()
	{
		return safe;
	}
}
