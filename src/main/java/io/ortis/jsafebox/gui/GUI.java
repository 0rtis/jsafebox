package io.ortis.jsafebox.gui;

import io.ortis.jsafebox.Utils;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.time.LocalDateTime;
import java.util.function.Supplier;
import java.util.logging.Logger;

/**
 * @author Ortis
 */
public abstract class GUI
{

	private static Settings settings;
	private static Logger log;
	private static boolean started;


	public static synchronized Settings getSettings()
	{
		if(GUI.settings == null)
			throw new RuntimeException("Settings not set");

		return GUI.settings;
	}

	public static synchronized void setSettings(final Settings settings)
	{
		GUI.settings = settings;
	}

	public static synchronized Logger getLogger()
	{

		if(GUI.log == null)
			throw new RuntimeException("Logger not set");

		return GUI.log;
	}

	public static synchronized void setLogger(final Logger log)
	{
		GUI.log = log;
	}

	public static synchronized void save()
	{
		log.info("Saving settings");
		try
		{
			GUI.settings.save();
		} catch(final Exception e)
		{
			log.severe("Error while saving settings - " + Utils.formatException(e));
		}

	}

	public static synchronized void start(final Supplier<LocalDateTime> clock) throws Exception
	{
		if(started)
			throw new IllegalStateException("Already started");

		try
		{

			final Settings settings = Settings.getSettings();
			GUI.setSettings(settings);


			System.out.println("Initializing gui context");

			final Logger log = GUI.getLogger();
			log.info("Setting look & feel");
			try
			{
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			} catch(final Exception e)
			{
				log.warning("Error while setting look & feel - " + Utils.formatException(e));
			}

			final JFrame loginFrame = new LoginFrame();
			loginFrame.setLocationRelativeTo(null);
			loginFrame.setVisible(true);

			GUI.started = true;
		} catch(final Exception e)
		{
			System.err.println(Utils.formatException(e));
			exit(-1);
		}

	}

	public static void exit(final int code)
	{
		save();
		try
		{
			log.info("Closing safebox");
			System.out.println("Closing safebox");

		} catch(final Exception e)
		{
			log.severe("Error while closing safebox - " + Utils.formatException(e));
			System.err.println("Error while closing safebox - " + Utils.formatException(e));
		}

		log.info("Exiting");
		System.out.println("Exiting");
		System.exit(code);

	}

	public static void putInClipboard(String data)
	{
		if(data == null)
			data = "";

		final StringSelection stringSelection = new StringSelection(data);
		final Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		clipboard.setContents(stringSelection, null);
	}
}
