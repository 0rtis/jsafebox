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

package org.ortis.jsafebox.gui;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.swing.filechooser.FileSystemView;

public class Configuration extends Properties
{

	private static final long serialVersionUID = 1L;

	private static final String SAFE_FILE_LIST_KEY = "safe.files";
	private static final String AUTOSAVE_KEY = "gui.autosave";
	private static final String PREVIEW_KEY = "gui.preview";
	private static final String AUTOHASH_CHECK_KEY = "gui.autohashcheck";

	private static final String EXTRACT_DIRECTORY_KEY = "extract.directory";

	public void addSafeFilePath(final String path)
	{

		final List<String> paths = new ArrayList<>();
		final StringBuilder sb = new StringBuilder();
		sb.append(path);
		paths.add(path.toUpperCase());

		for (final String p : getSafeFilePaths())
		{
			if (paths.contains(p.toUpperCase()))
				continue;

			sb.append(";" + p);
			paths.add(p.toUpperCase());
		}

		setProperty(SAFE_FILE_LIST_KEY, sb.toString());
	}

	public List<String> getSafeFilePaths()
	{
		final String value = getProperty(SAFE_FILE_LIST_KEY);

		final List<String> paths = new ArrayList<>();
		if (value != null)
		{
			final String [] values = value.split(";");
			for (final String v : values)
				paths.add(v);
		}

		return paths;
	}

	public void setExtractDirectory(final File directory)
	{
		setProperty(EXTRACT_DIRECTORY_KEY, directory.getAbsoluteFile().getAbsolutePath());
	}

	public File getExtractDirectory()
	{
		final String value = getProperty(EXTRACT_DIRECTORY_KEY);

		if (value == null)
		{
			final File defaultDirectory = FileSystemView.getFileSystemView().getHomeDirectory();
			setExtractDirectory(defaultDirectory);
			return defaultDirectory;
		}

		return new File(value);
	}

	public void setAutoSave(final boolean autoSave)
	{
		setProperty(AUTOSAVE_KEY, Boolean.toString(autoSave));
	}

	public boolean getAutoSave()
	{
		final String value = getProperty(AUTOSAVE_KEY);

		if (value == null)
		{
			setAutoSave(true);
			return true;
		}

		return Boolean.parseBoolean(value);
	}

	public void setPreview(final boolean preview)
	{
		setProperty(PREVIEW_KEY, Boolean.toString(preview));
	}

	public boolean getPreview()
	{
		final String value = getProperty(PREVIEW_KEY);

		if (value == null)
		{
			setPreview(true);
			return true;
		}

		return Boolean.parseBoolean(value);
	}

	public void setAutoHashCheck(final boolean autoHashCheck)
	{
		setProperty(AUTOHASH_CHECK_KEY, Boolean.toString(autoHashCheck));
	}

	public boolean getAutoHashCheck()
	{
		final String value = getProperty(AUTOHASH_CHECK_KEY);

		if (value == null)
		{
			setAutoHashCheck(true);
			return true;
		}

		return Boolean.parseBoolean(value);
	}
}
