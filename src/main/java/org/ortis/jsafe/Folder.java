/*******************************************************************************
 * Copyright 2018 Ortis (cao.ortis.org@gmail.com)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package org.ortis.jsafe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Folder implements SafeFile
{

	public final static String ROOT_NAME = "";
	public final static char DELIMITER = '/';
	public final static String REGEX_DELIMITER = "/";

	private final String name;
	private final String comparableName;
	private final Folder parent;
	private final String path;
	private final String comparablePath;

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

	}

	public void add(final Block block) throws Exception
	{

		final String name = block.getProperties().get(Block.NAME_LABEL);

		for (final Block b : this.blocks)

			if (b.getProperties().get(Block.NAME_LABEL).equals(name))
				throw new Exception("Block " + block + " already exist");

		this.blocks.add(block);
		this.files.add(block);
	}

	public Folder addFolder(final String name) throws Exception
	{
		final String comparableName = name.toUpperCase(Environment.getLocale());
		for (final Folder f : this.folders)
			if (f.getComparableName().equals(comparableName))
				throw new Exception("Folder " + name + " already exist");

		final Folder folder = new Folder(this, name);
		this.folders.add(folder);
		this.files.add(folder);
		return folder;
	}

	public  SafeFile get( final String [] tokens, final int start, final int end)
	{
		
	
			
		if (start >= tokens.length || start >= end)
			return null;
		
		return unsafeGet(tokens, start, end);
	}
	
	private  SafeFile unsafeGet( final String [] tokens, final int start, final int end)
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
				return folder.unsafeGet(tokens, start + 1, end );

		return null;
	}

	public SafeFile get(final String path) throws Exception
	{
		final String [] tokens = path.split(REGEX_DELIMITER);

		if (tokens.length == 0)
			return null;

		final int start;
		if (tokens[0].toUpperCase(Environment.getLocale()).equals(this.comparableName))
		{
			if (tokens.length == 1)
				return this;
			start = 1;
		} else
			start = 0;

		return unsafeGet(tokens, start, tokens.length );

	}

	

	public void mkdir(final String path, final boolean isFfilePath) throws Exception
	{

		final String [] tokens = path.split(REGEX_DELIMITER);

		mkdir(tokens, isFfilePath);
	}

	public void mkdir(final String [] pathTokens, final boolean isFfilePath) throws Exception
	{
		mkdir(pathTokens, 0, isFfilePath);
	}

	public void mkdir(final String [] pathTokens, final int from, final boolean isFfilePath) throws Exception
	{

		if (pathTokens.length == 0)
			throw new Exception("Empty path");

		final int size = isFfilePath ? pathTokens.length - 1 : pathTokens.length;
		Folder folder = this;
		final StringBuilder path = new StringBuilder();
		for (int i = from; i < size; i++)
		{
			if (path.length() == 0)
				path.append(pathTokens[i]);
			else
				path.append(Folder.DELIMITER + pathTokens[i]);

			SafeFile file = folder.get(pathTokens[i]);
			if (file == null)
				folder = folder.addFolder(pathTokens[i]);
			else if (file.isFolder())
				folder = (Folder) file;
			else
				throw new Exception("Block file " + path + " already exist");

		}
	}

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

	public String getName()
	{
		return name;
	}

	public String getComparableName()
	{
		return comparableName;
	}

	@Override
	public String toString()
	{

		return this.path;
	}
}
