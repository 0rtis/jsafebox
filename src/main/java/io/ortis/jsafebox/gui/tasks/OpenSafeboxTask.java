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
	private final Window parent;


	private Safe safe;

	public OpenSafeboxTask(final String safeboxPath, final char[] pwd, final Window parent, final Logger log)
	{
		super("Opening safebox", "Success", "Safebox successfully opened !", log);

		this.safeboxPath = safeboxPath;
		this.pwd = pwd;
		this.parent = parent;
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
