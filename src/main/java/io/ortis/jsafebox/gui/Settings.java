package io.ortis.jsafebox.gui;


import io.ortis.jsafebox.gui.styles.fonts.*;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.PropertiesConfigurationLayout;
import org.apache.commons.configuration2.ex.ConfigurationException;
import io.ortis.jsafebox.OS;
import io.ortis.jsafebox.gui.styles.colors.ColorTheme;
import io.ortis.jsafebox.gui.styles.colors.DarkColorTheme;
import io.ortis.jsafebox.gui.styles.colors.LegacyColorTheme;
import org.ortis.jsafebox.gui.styles.fonts.*;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * @author Ortis
 */
public class Settings
{
	public static final String SETTINGS_FILE_NAME = "jsafebox.properties";
	public static final String LABEL_VERSION = "2.4-0 beta";
	public static final String GUI_COLOR_THEME = "gui.theme.color";
	public static final String GUI_FONT_THEME = "gui.theme.font";
	public static final String SAFE_FILE_LIST_KEY = "safe.files";
	public static final String SAFE_BUFFER_LENGTH_KEY = "safe.buffer.length";

	private final static DecimalFormat MOCHIMO_FORMAT = new DecimalFormat("0.000000000");
	private static Settings instance;

	private static File defaultDirectory = new File(".");

	private enum Theme
	{
		Legacy, Dark;

		public static Theme of(final String label)
		{
			if(label == null)
				return null;

			for(final Theme theme : Theme.values())
				if(theme.name().equals(label))
					return theme;

			return null;
		}
	}

	private final Path settingFilePath;
	private final PropertiesConfiguration config;
	private final PropertiesConfigurationLayout configLayout;
	private final List<Image> frameIcons;
	private final List<Image> progressIcons;
	private final Image successIcon;
	private final OS osType;
	private final String htmlHelp;
	private final Theme theme;
	private final ColorTheme colorTheme;
	private final FontTheme fontTheme;

	private Settings(final Path settingFilePath) throws IOException, ConfigurationException
	{
		this.settingFilePath = settingFilePath;
		this.config = new PropertiesConfiguration();
		this.configLayout = new PropertiesConfigurationLayout();

		if(this.settingFilePath != null && Files.exists(this.settingFilePath))
			try(final FileInputStream fis = new FileInputStream(settingFilePath.toFile()))
			{
				this.configLayout.load(this.config, new InputStreamReader(fis));
			}


		final String os = System.getProperty("os.name").toUpperCase(Locale.ENGLISH);
		if(os.contains("WINDOWS"))
			this.osType = OS.Windows;
		else if(os.contains("MAC OS"))
			this.osType = OS.Mac;
		else if(os.contains("LINUX") || os.contains("UNIX"))
			this.osType = OS.Linux;
		else
			this.osType = OS.Unknown;


		final List<Image> icons = new ArrayList<>();
		icons.add(Toolkit.getDefaultToolkit().getImage(Settings.class.getResource("/img/frame-icons/safe-filled-16.png")));
		icons.add(Toolkit.getDefaultToolkit().getImage(Settings.class.getResource("/img/frame-icons/safe-filled-32.png")));
		icons.add(Toolkit.getDefaultToolkit().getImage(Settings.class.getResource("/img/frame-icons/safe-filled-64.png")));
		icons.add(Toolkit.getDefaultToolkit().getImage(Settings.class.getResource("/img/frame-icons/safe-filled-100.png")));
		this.frameIcons = Collections.unmodifiableList(icons);


		final Theme propTheme = Theme.of(getProperty(GUI_COLOR_THEME));
		if(propTheme == null)
		{
			setProperty(GUI_COLOR_THEME, Theme.Legacy.name());
			this.theme = Theme.Legacy;
		}
		else
			this.theme = propTheme;

		FontTheme validFont = FontTheme.of(getProperty(GUI_FONT_THEME));

		if(validFont == null)
		{
			final List<FontTheme> fontThemes = new ArrayList<>();
			fontThemes.add(new WindowsFont());
			fontThemes.add(new UbuntuFont());
			fontThemes.add(new SafeFont());

			for(final FontTheme fontTheme : fontThemes)
				if(fontTheme.isSupported())
				{
					validFont = fontTheme;
					break;
				}
		}

		if(validFont == null)
			this.fontTheme = new NullFont();
		else
			this.fontTheme = validFont;

		System.out.println("Font theme " + this.fontTheme.getClass().getSimpleName());
		System.out.println("Theme " + this.theme);
		final List<Image> progressIcons = new ArrayList<>();

		switch(this.theme)
		{

			case Dark:
				this.colorTheme = new DarkColorTheme(this.osType);
				progressIcons.add(Toolkit.getDefaultToolkit().getImage(Settings.class.getResource("/img/sand-timer-white-24.png")));
				this.successIcon = Toolkit.getDefaultToolkit().getImage(Settings.class.getResource("/img/ok-24.png"));
				break;

			case Legacy:
			default:

				this.colorTheme = new LegacyColorTheme(this.osType);
				progressIcons.add(Toolkit.getDefaultToolkit().getImage(Settings.class.getResource("/img/sand-timer-24.png")));
				this.successIcon = Toolkit.getDefaultToolkit().getImage(Settings.class.getResource("/img/ok-24.png"));

				break;


		}


		this.progressIcons = Collections.unmodifiableList(progressIcons);


		try(final InputStream is = getClass().getClassLoader().getResourceAsStream("html/help.html"))
		{
			final ByteArrayOutputStream baos = new ByteArrayOutputStream();
			int b;
			while((b = is.read()) > -1)
				baos.write(b);

			this.htmlHelp = new String(baos.toByteArray(), StandardCharsets.UTF_8);
		}

	}

	public String getProperty(final String key)
	{
		return this.config.getString(key);
	}

	public void setProperty(final String key, final String value)
	{
		this.config.setProperty(key, value);
	}

	public void save() throws IOException, ConfigurationException
	{
		try(final FileOutputStream fos = new FileOutputStream(this.settingFilePath.toFile()))
		{
			this.configLayout.save(this.config, new OutputStreamWriter(fos));
		}
	}

	public void addSafeFilePath(final String path)
	{
		final List<String> paths = new ArrayList<>();
		final StringBuilder sb = new StringBuilder();
		sb.append(path);
		paths.add(path.toUpperCase());

		for(final String p : getSafeFilePaths())
		{
			if(paths.contains(p.toUpperCase()))
				continue;

			sb.append(";").append(p);
			paths.add(p.toUpperCase());
		}

		setProperty(SAFE_FILE_LIST_KEY, sb.toString());
	}

	public List<String> getSafeFilePaths()
	{
		final String value = getProperty(SAFE_FILE_LIST_KEY);

		final List<String> paths = new ArrayList<>();
		if(value != null)
		{
			final String[] values = value.split(";");
			for(final String v : values)
				paths.add(v);
		}

		return paths;
	}

	public Integer getInteger(final String key)
	{

		final String value = getProperty(key);

		if(value == null)
			return null;

		return Integer.parseInt(value);
	}

	public void saveInteger(final String key, final int i)
	{
		final String value = Integer.toString(i);
		setProperty(key, value);
	}

	public Duration getMillisecAsDuration(final String key)
	{
		final Integer value = getInteger(key);
		return Duration.ofMillis(value);
	}

	public Boolean getBoolean(final String key)
	{

		final String value = getProperty(key);

		if(value == null)
			return null;

		return Boolean.parseBoolean(value);
	}

	public void saveBoolean(final String key, final boolean b)
	{
		final String value = Boolean.toString(b);
		setProperty(key, value);
	}

	public List<Image> getFrameIcons()
	{
		return this.frameIcons;
	}

	public List<Image> getProgressIcons()
	{
		return progressIcons;
	}

	public Image getSuccessIcon()
	{
		return successIcon;
	}

	public OS getOsType()
	{
		return osType;
	}

	public String getHTMLHelp()
	{
		return htmlHelp;
	}

	public ColorTheme getColorTheme()
	{
		return this.colorTheme;
	}

	public FontTheme getFontTheme()
	{
		return this.fontTheme;
	}

	public void applyHeaderLabelStyle(final JLabel fieldLabel)
	{
		fieldLabel.setForeground(getColorTheme().getHeaderTextColor());
		fieldLabel.setFont(getFontTheme().getHeaderFont());
	}

	public void applyMetaDataFieldLabelStyle(final JLabel metaDataLabel)
	{
		metaDataLabel.setForeground(getColorTheme().getTextLabelColor());
		metaDataLabel.setFont(getFontTheme().getMetaDataFieldFont());
	}

	public void applyFieldLabelStyle(final JLabel fieldLabel)
	{
		fieldLabel.setForeground(getColorTheme().getTextLabelColor());
		fieldLabel.setFont(getFontTheme().getFieldFont());
	}

	public void applyFieldLabelClickableMouseOverStyle(final JLabel fieldLabel)
	{
		fieldLabel.setForeground(getColorTheme().getClickableColor());
		fieldLabel.setFont(getFontTheme().getFieldFont());
	}

	public void applyNumericalFieldLabelStyle(final JLabel fieldLabel)
	{
		fieldLabel.setForeground(getColorTheme().getTextLabelColor());
		fieldLabel.setFont(getFontTheme().getNumericalFieldFont());
	}

	public void applyBigNumericalFieldLabelStyle(final JLabel fieldLabel)
	{
		fieldLabel.setForeground(getColorTheme().getTextLabelColor());
		fieldLabel.setFont(getFontTheme().getBigNumericalFieldFont());
	}

	public void applySpinnerStyle(final JSpinner spinner)
	{
		spinner.setForeground(getColorTheme().getTextLabelColor());
		spinner.setFont(getFontTheme().getNumericalInputFieldFont());
	}

	public void applyCheckboxStyle(final JCheckBox checkbox)
	{
		checkbox.setForeground(getColorTheme().getTextLabelColor());
		checkbox.setFont(getFontTheme().getFieldFont());
		checkbox.setOpaque(false);
		checkbox.setBackground(getColorTheme().getBackgroundColor());
	}

	public void applyTextFieldStyle(final JTextField textField)
	{
		//JPasswordField field are not set with transparent background so they cannot used the standard text color. We use the default system look
		if(!(textField instanceof JPasswordField))
			textField.setForeground(getColorTheme().getTextFieldColor());


		textField.setCaretColor(getColorTheme().getTextFieldColor());
		textField.setFont(getFontTheme().getFieldFont());
	}

	public void applyTextAreaStyle(final JTextArea textArea)
	{
		textArea.setForeground(getColorTheme().getTextFieldColor());
		textArea.setFont(getFontTheme().getFieldFont());
	}

	public void applyRawTextLabelStyle(final JLabel rawTextLabel)
	{
		rawTextLabel.setForeground(getColorTheme().getTextLabelColor());
		rawTextLabel.setFont(getFontTheme().getRawTextFont());
	}

	public void applyFirstButtonStyle(final JLabel firstButtonLabel)
	{
		firstButtonLabel.setFont(getFontTheme().getFirstButtonFont());
		firstButtonLabel.setForeground(getColorTheme().getButtonTextColor());
		firstButtonLabel.setBackground(getColorTheme().getButtonFirstColor());
	}

	public void applyFirstButtonMouseOverStyle(final JLabel firstButtonLabel)
	{
		firstButtonLabel.setFont(getFontTheme().getFirstButtonFont());
		firstButtonLabel.setForeground(getColorTheme().getButtonTextColor());
		firstButtonLabel.setBackground(getColorTheme().getButtonFirstColorMouseOver());
	}

	public void applySecondButtonStyle(final JLabel secondButtonLabel)
	{
		secondButtonLabel.setFont(getFontTheme().getSecondButtonFont());
		secondButtonLabel.setForeground(getColorTheme().getButtonTextColor());
		secondButtonLabel.setBackground(getColorTheme().getButtonSecondColor());
	}

	public void applySecondButtonMouseOverStyle(final JLabel secondButtonLabel)
	{
		secondButtonLabel.setFont(getFontTheme().getSecondButtonFont());
		secondButtonLabel.setForeground(getColorTheme().getButtonTextColor());
		secondButtonLabel.setBackground(getColorTheme().getButtonSecondColorMouseOver());
	}

	public void applyThirdButtonStyle(final JLabel thirdButtonLabel)
	{
		thirdButtonLabel.setFont(getFontTheme().getThirdButtonFont());
		thirdButtonLabel.setForeground(getColorTheme().getButtonTextColor());
		thirdButtonLabel.setBackground(getColorTheme().getButtonThirdColor());
	}

	public void applyThirdButtonMouseOverStyle(final JLabel thirdButtonLabel)
	{
		thirdButtonLabel.setFont(getFontTheme().getThirdButtonFont());
		thirdButtonLabel.setForeground(getColorTheme().getButtonTextColor());
		thirdButtonLabel.setBackground(getColorTheme().getButtonThirdColorMouseOver());
	}

	public void setFontBold(final Component component)
	{
		component.setFont(component.getFont().deriveFont(Font.BOLD));
	}

	public synchronized static Settings getSettings()
	{
		return instance;
	}

	public synchronized static Settings load() throws IOException, ConfigurationException
	{
		if(instance == null)
		{
			final Path settingsFilePath = Paths.get(SETTINGS_FILE_NAME);
			if(!Files.exists(settingsFilePath))
			{// extracting default settings

				final InputStream is = Settings.class.getResourceAsStream("/" + SETTINGS_FILE_NAME);
				final ByteArrayOutputStream baos = new ByteArrayOutputStream();
				final byte[] buffer = new byte[1024];
				int read;

				while((read = is.read(buffer)) > 1)
					baos.write(buffer, 0, read);

				Files.write(settingsFilePath, baos.toByteArray());
			}

			if(!Files.exists(settingsFilePath))
				throw new IOException("Properties file not found");


			instance = new Settings(settingsFilePath);
		}
		else
			throw new IllegalStateException("Settings already set");

		return instance;
	}

	public static File getDefaultDirectory()
	{
		synchronized(Settings.class)
		{
			return defaultDirectory;
		}

	}

	public static void setDefaultDirectory(final String path)
	{
		if(path == null)
			return;

		setDefaultDirectory(new File(path));
	}

	public static void setDefaultDirectory(File directory)
	{
		if(directory == null)
			return;

		if(!directory.isDirectory())
			directory = directory.getParentFile();

		synchronized(Settings.class)
		{
			defaultDirectory = directory;
		}
	}


}
