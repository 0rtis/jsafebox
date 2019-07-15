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


import io.ortis.jsafebox.OS;
import io.ortis.jsafebox.gui.theme.fonts.*;
import io.ortis.jsafebox.gui.theme.ui.MojoUITheme;
import io.ortis.jsafebox.gui.theme.ui.NocturneUITheme;
import io.ortis.jsafebox.gui.theme.ui.SunlightUITheme;
import io.ortis.jsafebox.gui.theme.ui.UITheme;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.PropertiesConfigurationLayout;
import org.apache.commons.configuration2.ex.ConfigurationException;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyListener;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.*;

/**
 * @author Ortis
 */
public class Settings
{
	public static final String SETTINGS_FILE_NAME = "jsafebox.properties";

	public static final String GUI_THEME_SAFE = "gui.theme.safe";
	public static final String GUI_THEME_UI = "gui.theme.ui";
	public static final String GUI_THEME_FONT = "gui.ui.font";

	private static final String GUI_MENU_TREELAZYLOADING_KEY = "gui.menu.treelazyloading";
	private static final String GUI_MENU_AUTOSAVE_KEY = "gui.menu.autosave";
	private static final String GUI_MENU_PREVIEW_KEY = "gui.menu.preview";
	private static final String GUI_MENU_AUTOHASHCHECK_KEY = "gui.menu.autohashcheck";

	public static final String MIMES_MAP = "safe.file.mimes";
	public static final String TYPE_MAP = "safe.file.types";
	public static final String TYPE_UNKNOWN_MAX_LENGTH = "safe.file.types.unknown.maxLength";

	public static final String SAFE_FILE_LIST_KEY = "safe.files";
	public static final String SAFE_BUFFER_LENGTH_KEY = "safe.buffer.length";

	private static Settings instance;
	private static File defaultDirectory = new File(".");

	private enum Theme
	{
		Mojo, Nocturne, Sunlight;

		public static Theme of(String label)
		{
			if(label == null)
				return null;

			label = label.toUpperCase(Locale.ENGLISH);

			for(final Theme theme : Theme.values())
				if(theme.name().toUpperCase(Locale.ENGLISH).equals(label))
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

	private final boolean safeTheme;
	private final Theme theme;
	private final UITheme uiTheme;
	private final FontTheme fontTheme;


	private final Map<String, List<String>> mimes;
	private final Map<FileType, List<String>> fileTypes;

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
			this.osType = OS.OSX;
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

		this.safeTheme = Optional.ofNullable(getBoolean(GUI_THEME_SAFE)).orElse(false);

		final Theme propTheme = Theme.of(getProperty(GUI_THEME_UI));
		if(propTheme == null)
		{
			setProperty(GUI_THEME_UI, Theme.Mojo.name());
			this.theme = Theme.Mojo;
		}
		else
			this.theme = propTheme;

		FontTheme validFont = FontTheme.of(getProperty(GUI_THEME_FONT));

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

		System.out.println("Font ui " + this.fontTheme.getClass().getSimpleName());
		System.out.println("Theme " + this.theme);
		final List<Image> progressIcons = new ArrayList<>();

		switch(this.theme)
		{
			case Nocturne:
				this.uiTheme = new NocturneUITheme(this.osType);
				progressIcons.add(Toolkit.getDefaultToolkit().getImage(Settings.class.getResource("/img/sand-timer-white-24.png")));
				this.successIcon = Toolkit.getDefaultToolkit().getImage(Settings.class.getResource("/img/ok-24.png"));
				break;

			case Mojo:
				this.uiTheme = new MojoUITheme(this.osType);
				progressIcons.add(Toolkit.getDefaultToolkit().getImage(Settings.class.getResource("/img/sand-timer-24.png")));
				this.successIcon = Toolkit.getDefaultToolkit().getImage(Settings.class.getResource("/img/ok-24.png"));
				break;

			default:
			case Sunlight:
				this.uiTheme = new SunlightUITheme(this.osType);
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

		this.mimes = new LinkedHashMap<>();
		for(final Map.Entry<String, List<String>> mime : getMapList(MIMES_MAP, new LinkedHashMap<>()).entrySet())
		{
			final List<String> extensions = new ArrayList<>();

			for(final String ext : mime.getValue())
				extensions.add(ext.toUpperCase(Locale.ENGLISH));

			this.mimes.put(mime.getKey(), extensions);
		}

		this.fileTypes = new LinkedHashMap<>();
		for(final Map.Entry<String, List<String>> type : getMapList(TYPE_MAP, new LinkedHashMap<>()).entrySet())
		{
			final List<String> mimes = new ArrayList<>();

			for(final String ext : type.getValue())
				mimes.add(ext.toUpperCase(Locale.ENGLISH));

			this.fileTypes.put(FileType.parse(type.getKey()), mimes);
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


	public Map<String, List<String>> getMapList(final String key, final Map<String, List<String>> destination)
	{
		final Map<String, String> map = getMap(key, new LinkedHashMap<>());
		for(final Map.Entry<String, String> entry : map.entrySet())
			destination.put(entry.getKey(), asList(entry.getValue(), ",", new ArrayList<>()));

		return destination;
	}

	public List<String> getList(final String key, final List<String> destination)
	{
		final String propertyString = getProperty(key);
		if(propertyString == null)
			return null;

		return asList(propertyString, ";", destination);
	}

	private List<String> asList(final String serial, final String regexSeprator, final List<String> destination)
	{
		for(final String entry : serial.split(regexSeprator))
			destination.add(entry);

		return destination;
	}

	public Map<String, String> getMap(final String key, final Map<String, String> destination)
	{
		final String value = getProperty(key);

		if(value == null)
			throw new IllegalArgumentException("Key '" + key + "' not found");

		String [] buffer;
		for(final String entry : value.split(";"))
		{
			buffer = entry.split("=");

			if(destination.containsKey(buffer[0]))
				throw new RuntimeException("Duplicate key " + buffer[0] + " in property " + key);

			destination.put(buffer[0], buffer[1]);
		}
		return destination;
	}


	public int maxUnknownFileTypeLengthDisplay()
	{
		return getInteger(TYPE_UNKNOWN_MAX_LENGTH);
	}

	public FileType getFileType(String mime)
	{
		if(mime == null)
			return null;

		mime = mime.toUpperCase(Locale.ENGLISH);

		for(final Map.Entry<FileType, List<String>> types : this.fileTypes.entrySet())
			for(final String m : types.getValue())
				if(m.equals(mime))
					return types.getKey();

		return FileType.Unknown;
	}


	public String getMime(String extension)
	{
		if(extension == null)
			return null;

		extension = extension.toUpperCase(Locale.ENGLISH);

		for(final Map.Entry<String, List<String>> extensions : this.mimes.entrySet())
			for(final String ext : extensions.getValue())
				if(ext.equals(extension))
					return extensions.getKey();

		return "application/octet-stream";
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

	public UITheme getUITheme()
	{
		return this.uiTheme;
	}

	public boolean isSafeTheme()
	{
		return safeTheme;
	}

	public FontTheme getFontTheme()
	{
		return this.fontTheme;
	}

	public void applyHeaderLabelStyle(final JLabel fieldLabel)
	{
		fieldLabel.setForeground(getUITheme().getHeaderTextColor());
		fieldLabel.setFont(getFontTheme().getHeaderFont());
	}

	public void applyMetaDataFieldLabelStyle(final JLabel metaDataLabel)
	{
		metaDataLabel.setForeground(getUITheme().getTextLabelColor());
		metaDataLabel.setFont(getFontTheme().getMetaDataFieldFont());
	}

	public void applyFieldLabelStyle(final JLabel fieldLabel)
	{
		fieldLabel.setForeground(getUITheme().getTextLabelColor());
		fieldLabel.setFont(getFontTheme().getFieldFont());
	}

	public void applyFieldLabelClickableMouseOverStyle(final JLabel fieldLabel)
	{
		fieldLabel.setForeground(getUITheme().getClickableColor());
		fieldLabel.setFont(getFontTheme().getFieldFont());
	}

	public void applyNumericalFieldLabelStyle(final JLabel fieldLabel)
	{
		fieldLabel.setForeground(getUITheme().getTextLabelColor());
		fieldLabel.setFont(getFontTheme().getNumericalFieldFont());
	}

	public void applyBigNumericalFieldLabelStyle(final JLabel fieldLabel)
	{
		fieldLabel.setForeground(getUITheme().getTextLabelColor());
		fieldLabel.setFont(getFontTheme().getBigNumericalFieldFont());
	}

	public void applySpinnerStyle(final JSpinner spinner)
	{
		spinner.setForeground(getUITheme().getTextLabelColor());
		spinner.setFont(getFontTheme().getNumericalInputFieldFont());
	}

	public void applyCheckboxStyle(final JCheckBox checkbox)
	{
		checkbox.setForeground(getUITheme().getTextLabelColor());
		checkbox.setFont(getFontTheme().getFieldFont());
		checkbox.setOpaque(false);
		checkbox.setBackground(getUITheme().getBackgroundColor());
	}

	public void applyTextFieldStyle(final JTextField textField, final boolean setForeground)
	{
		//JPasswordField field are not set with transparent background so they cannot used the standard text color. We use the default system look
		if(setForeground && !(textField instanceof JPasswordField))
		{
			textField.setForeground(getUITheme().getTextFieldColor());
			textField.setCaretColor(getUITheme().getTextFieldColor());
		}

		textField.setFont(getFontTheme().getFieldFont());
	}

	public void applyTextAreaStyle(final JTextArea textArea)
	{
		textArea.setForeground(getUITheme().getTextFieldColor());
		textArea.setFont(getFontTheme().getFieldFont());
	}

	public void applyRawTextLabelStyle(final JLabel rawTextLabel)
	{
		rawTextLabel.setForeground(getUITheme().getTextLabelColor());
		rawTextLabel.setFont(getFontTheme().getRawTextFont());
	}

	public void applyFirstButtonStyle(final JLabel firstButtonLabel)
	{
		firstButtonLabel.setFont(getFontTheme().getFirstButtonFont());
		firstButtonLabel.setForeground(getUITheme().getButtonTextColor());
		firstButtonLabel.setBackground(getUITheme().getButtonFirstColor());
	}

	public void applyFirstButtonMouseOverStyle(final JLabel firstButtonLabel)
	{
		firstButtonLabel.setFont(getFontTheme().getFirstButtonFont());
		firstButtonLabel.setForeground(getUITheme().getButtonTextColor());
		firstButtonLabel.setBackground(getUITheme().getButtonFirstColorMouseOver());
	}

	public void applySecondButtonStyle(final JLabel secondButtonLabel)
	{
		secondButtonLabel.setFont(getFontTheme().getSecondButtonFont());
		secondButtonLabel.setForeground(getUITheme().getButtonTextColor());
		secondButtonLabel.setBackground(getUITheme().getButtonSecondColor());
	}

	public void applySecondButtonMouseOverStyle(final JLabel secondButtonLabel)
	{
		secondButtonLabel.setFont(getFontTheme().getSecondButtonFont());
		secondButtonLabel.setForeground(getUITheme().getButtonTextColor());
		secondButtonLabel.setBackground(getUITheme().getButtonSecondColorMouseOver());
	}

	public void applyThirdButtonStyle(final JLabel thirdButtonLabel)
	{
		thirdButtonLabel.setFont(getFontTheme().getThirdButtonFont());
		thirdButtonLabel.setForeground(getUITheme().getButtonTextColor());
		thirdButtonLabel.setBackground(getUITheme().getButtonThirdColor());
	}

	public void applyThirdButtonMouseOverStyle(final JLabel thirdButtonLabel)
	{
		thirdButtonLabel.setFont(getFontTheme().getThirdButtonFont());
		thirdButtonLabel.setForeground(getUITheme().getButtonTextColor());
		thirdButtonLabel.setBackground(getUITheme().getButtonThirdColorMouseOver());
	}

	public void setFontBold(final Component component)
	{
		component.setFont(component.getFont().deriveFont(Font.BOLD));
	}

	public void setTreeLazyLoading(final boolean fullTree)
{
	setProperty(GUI_MENU_TREELAZYLOADING_KEY, Boolean.toString(fullTree));
}

	public boolean isTreeLazyLoading()
	{
		final String value = getProperty(GUI_MENU_TREELAZYLOADING_KEY);

		if (value == null)
		{
			setTreeLazyLoading(false);
			return false;
		}

		return Boolean.parseBoolean(value);
	}

	public void setAutoSave(final boolean autoSave)
	{
		setProperty(GUI_MENU_AUTOSAVE_KEY, Boolean.toString(autoSave));
	}

	public boolean isAutoSave()
	{
		final String value = getProperty(GUI_MENU_AUTOSAVE_KEY);

		if (value == null)
		{
			setAutoSave(true);
			return true;
		}

		return Boolean.parseBoolean(value);
	}

	public void setPreview(final boolean preview)
	{
		setProperty(GUI_MENU_PREVIEW_KEY, Boolean.toString(preview));
	}

	public boolean isPreview()
	{
		final String value = getProperty(GUI_MENU_PREVIEW_KEY);

		if (value == null)
		{
			setPreview(true);
			return true;
		}

		return Boolean.parseBoolean(value);
	}

	public void setAutoHashCheck(final boolean autoHashCheck)
	{
		setProperty(GUI_MENU_AUTOHASHCHECK_KEY, Boolean.toString(autoHashCheck));
	}

	public boolean isAutoHashCheck()
	{
		final String value = getProperty(GUI_MENU_AUTOHASHCHECK_KEY);

		if (value == null)
		{
			setAutoHashCheck(true);
			return true;
		}

		return Boolean.parseBoolean(value);
	}


	public static void addKeyListener(final KeyListener keyListener, final Container root)
	{
		root.addKeyListener(keyListener);

		for(final Component component : root.getComponents())
		{
			component.addKeyListener(keyListener);
			if(component instanceof Container)
				addKeyListener(keyListener, (Container) component);
		}
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
