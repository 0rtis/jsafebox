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

package io.ortis.jsafebox;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Representation of a directory of {@link SafeFile} inside the {@link Safe}
 * 
 * @author Ortis <br>
 *         2018 Apr 26 7:53:33 PM <br>
 */
public class Folder implements SafeFile
{

	private final Comparator<SafeFile> SAFE_FILE_COMPARATOR = new Comparator<SafeFile>()
	{

		@Override
		public int compare(final SafeFile sf1, final SafeFile sf2)
		{

			if (sf1.isFolder() && !sf2.isFolder())
				return -1;

			if (sf2.isFolder() && !sf1.isFolder())
				return 1;

			return sf1.getName().compareTo(sf2.getName());
		}
	};

	public final static String ROOT_NAME = "";
	public final static char DELIMITER = '/';
	public final static String REGEX_DELIMITER = "/";
	public final static String WILDCARD = "*";
	public final static String WILDCARD_REGEX = ".*";

	private final String name;
	private final String comparableName;

	private final Folder parent;
	private final String path;
	private final String comparablePath;
	private final String [] comparableTokens;

	private final List<Folder> folders;
	private final List<Block> blocks;
	private final List<SafeFile> files;
	private final List<SafeFile> roFiles;

	public Folder(final Folder parent, final String name)
	{

		this.parent = parent;
		this.name = name;
		this.comparableName = this.name.toUpperCase(Environment.getLocale());

		this.folders = new ArrayList<>();
		this.blocks = new ArrayList<>();
		this.files = new ArrayList<>();
		this.roFiles = Collections.unmodifiableList(this.files);

		final List<Folder> above = new ArrayList<>();

		Folder current = this;
		while (current != null)
		{
			above.add(current);
			current = current.getParent();
		}

		final StringBuilder sb = new StringBuilder();
		for (int i = above.size() - 1; i >= 0; i--)
			sb.append(above.get(i).getName() + DELIMITER);

		sb.delete(sb.length() - 1, sb.length());
		this.path = sb.toString();
		this.comparablePath = this.path.toUpperCase(Environment.getLocale());
		this.comparableTokens = this.comparablePath.split(REGEX_DELIMITER);

	}

	/**
	 * Add a {@link Block} to the {@link Folder}
	 * 
	 * @param block
	 * @throws Exception
	 */
	public void add(final Block block) throws Exception
	{

		final String [] tokens = block.getComparablePath().split(REGEX_DELIMITER);

		if (tokens.length - 1 != this.comparableTokens.length)
			throw new Exception("Block path '" + block.getPath() + " does not match folder path '" + this.path + "'");

		for (int i = 0; i < this.comparableTokens.length; i++)
			if (!this.comparableTokens[i].equals(tokens[i]))
				throw new Exception("Block path '" + block.getPath() + " does not match folder path '" + this.path + "'");

		final String name = block.getProperties().get(Block.NAME_LABEL);

		for (final Block b : this.blocks)

			if (b.getProperties().get(Block.NAME_LABEL).equals(name))
				throw new Exception("Block " + block + " already exist");

		this.blocks.add(block);

		this.files.add(block);
		this.files.sort(SAFE_FILE_COMPARATOR);
	}

	/**
	 * Search the {@link SafeFile} that match the tokenized path
	 * 
	 * @param tokens:
	 *            tokens of the path
	 * @param start:
	 *            tokens start index
	 * @param end:
	 *            tokens end index
	 * @return
	 */
	public SafeFile get(final String [] tokens, final int start, final int end)
	{
		if (start >= tokens.length || start >= end)
			return null;

		return unsafeGet(tokens, start, end);
	}

	private SafeFile unsafeGet(final String [] tokens, final int start, final int end)
	{

		if (start >= tokens.length || start >= end)
			return this;

		if (start == tokens.length - 1)
		{
			final String comparableToken = tokens[start].toUpperCase(Environment.getLocale());
			for (final Folder folder : this.folders)
				if (comparableToken.equals(folder.getComparableName()))
					return folder;

			for (final Block block : this.blocks)
				if (comparableToken.equals(block.getComparableName()))
					return block;

			return null;
		}

		final String name = tokens[start].toUpperCase(Environment.getLocale());

		for (final Folder folder : this.folders)
			if (name.equals(folder.getComparableName()))
				return folder.unsafeGet(tokens, start + 1, end);

		return null;
	}

	/**
	 * Return the {@link SafeFile} that match the name
	 * 
	 * @param name:
	 *            name of the {@link SafeFile}
	 * @return
	 * @throws Exception
	 */
	public SafeFile getChild(String name) throws Exception
	{
		name = name.toUpperCase(Environment.getLocale());

		for (final SafeFile sf : this.files)
			if (sf.getComparableName().equals(name))
				return sf;

		return null;
	}

	public boolean remove(String name)
	{
		name = name.toUpperCase(Environment.getLocale());

		for (int i = 0; i < this.files.size(); i++)
		{
			final SafeFile safeFile = this.files.get(i);
			if (safeFile.getComparableName().equals(name))
			{
				this.files.remove(i);

				if (safeFile.isFolder())
				{
					for (int j = 0; j < this.folders.size(); j++)
						if (this.folders.get(j).getComparableName().equals(name))
						{
							this.folders.remove(j);
							break;
						}
				} else
					for (int j = 0; j < this.blocks.size(); j++)
						if (this.blocks.get(j).getComparableName().equals(name))
						{
							this.blocks.remove(j);
							break;
						}

				return true;
			}
		}

		return false;
	}

	/**
	 * Create a new child {@link Folder}
	 * 
	 * @param name
	 * @return
	 * @throws Exception
	 */
	public Folder mkdir(final String name) throws Exception
	{
		if (name.length() < 1)
			throw new Exception("Folder name length must be greater than 0");

		final String comparableName = name.toUpperCase(Environment.getLocale());
		for (final Folder f : this.folders)
			if (f.getComparableName().equals(comparableName))
				throw new Exception("Folder " + name + " already exist");

		final Folder folder = new Folder(this, name);
		this.folders.add(folder);
		this.files.add(folder);
		this.files.sort(SAFE_FILE_COMPARATOR);
		return folder;
	}

	/**
	 * Create all {@link Folder} required for the path
	 * 
	 * @param path:
	 *            path to create
	 * @param blockPath:
	 *            true if the path belongs to a {@link Block}
	 * @throws Exception
	 */
	public void mkdir(final String path, final boolean blockPath) throws Exception
	{

		final String [] tokens = path.split(REGEX_DELIMITER);

		mkdir(tokens, blockPath);
	}

	/**
	 * Create all {@link Folder} required for the tokenized path
	 * 
	 * @param pathTokens:
	 *            tokens of the path
	 * @param blockPath:
	 *            true if the path belongs to a {@link Block}
	 * @throws Exception
	 */
	public void mkdir(final String [] pathTokens, final boolean blockPath) throws Exception
	{
		mkdir(pathTokens, 0, blockPath);
	}

	/**
	 * Create all {@link Folder} required for the tokenized path
	 * 
	 * @param pathTokens:
	 *            tokens of the path
	 * @param from:
	 *            pathTokens start index
	 * @param blockPath:
	 *            true if the path belongs to a {@link Block}
	 * @throws Exception
	 */
	public void mkdir(final String [] pathTokens, final int from, final boolean blockPath) throws Exception
	{

		if (pathTokens.length == 0)
			throw new Exception("Empty path");

		final int size = blockPath ? pathTokens.length - 1 : pathTokens.length;
		Folder folder = this;
		for (int i = from; i < size; i++)
		{

			SafeFile file = folder.getChild(pathTokens[i]);
			if (file == null)
				folder = folder.mkdir(pathTokens[i]);
			else if (file.isFolder())
				folder = (Folder) file;
			else
				throw new Exception("Block file " + path + " already exist");

		}
	}

	/**
	 * Delete an empty {@link Folder}
	 * 
	 * @param name
	 * @return
	 * 
	 * 		public boolean rm(String name) throws Exception {
	 * 
	 *         name = name.toUpperCase(Environment.getLocale());
	 * 
	 *         for (int i = 0; i < this.folders.size(); i++) { final Folder folder = this.folders.get(i); if (folder.getComparableName().equals(name)) { if (!folder.listFiles().isEmpty()) throw new Exception("Folder " +
	 *         folder.getPath() + " is not empty");
	 * 
	 *         this.folders.remove(i); this.files.remove(folder); this.files.sort(SAFE_FILE_COMPARATOR); return true; }
	 * 
	 *         }
	 * 
	 *         return false; }
	 */

	/**
	 * Get all {@link SafeFile} within the {@link Folder}
	 * 
	 * @return
	 */
	public List<SafeFile> listFiles()
	{

		return this.roFiles;
	}

	@Override
	public boolean isBlock()
	{

		return false;
	}

	@Override
	public boolean isFolder()
	{

		return true;
	}

	/**
	 * Get the parent {@link Folder}
	 * 
	 * @return
	 */
	public Folder getParent()
	{
		return this.parent;
	}

	@Override
	public String getPath()
	{

		return this.path;
	}

	@Override
	public String getComparablePath()
	{

		return this.comparablePath;
	}

	/**
	 * Get the nwme of the {@link Folder}
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * Get comparable name of the {@link Folder}
	 */
	public String getComparableName()
	{
		return comparableName;
	}

	@Override
	public String toString()
	{

		return this.path;
	}

	@Override
	public int hashCode()
	{
		return this.comparablePath.hashCode();
	}

	@Override
	public boolean equals(final Object o)
	{
		if (o == this)
			return true;

		if (o instanceof Folder)
		{

			final Folder folder = (Folder) o;
			return folder.getComparablePath().equals(this.comparablePath);

		}

		return false;
	}
}
