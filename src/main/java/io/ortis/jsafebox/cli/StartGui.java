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

package io.ortis.jsafebox.cli;

import io.ortis.jsafebox.Version;
import io.ortis.jsafebox.gui.GUI;
import io.ortis.jsafebox.gui.Settings;
import io.ortis.jsafebox.log.LogService;
import io.ortis.jsafebox.log.LogServiceImpl;
import picocli.CommandLine.Command;

import javax.swing.*;
import java.time.LocalDateTime;
import java.util.concurrent.Callable;
import java.util.logging.Level;

/**
 * Start the GUI
 *
 * @author Ortis <br>
 * 2018 May 08 8:35:11 PM <br>
 */
@Command(description = "Start the GUI", name = "gui", mixinStandardHelpOptions = true, version = Version.VERSION, showDefaultValues = true)
public class StartGui implements Callable<Void>
{

	@Override
	public Void call() throws Exception
	{
		// LoginFrame.main(new String[0]);

		Settings.load();
		try
		{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch(final Exception e)
		{
		}

		final LogService logService = new LogServiceImpl();
		logService.addListener(record ->
		{
			if(record.getLevel().intValue() >= Level.SEVERE.intValue())
				System.err.println(logService.format(record));
		});

		GUI.setLogger(logService.getLogger("StartGui"));
		GUI.start(LocalDateTime::now);

		return null;
	}

}
