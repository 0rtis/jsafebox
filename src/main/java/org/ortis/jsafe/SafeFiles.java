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

package org.ortis.jsafe;

import java.util.Collection;
import java.util.regex.Pattern;

/**
 * Utility class for {@link SafeFile} handling
 * 
 * @author Ortis <br>
 *         2018 Apr 26 8:01:30 PM <br>
 */
public abstract class SafeFiles
{

	/**
	 * Search {@link SafeFile} with context
	 * 
	 * @param path
	 * @param current
	 * @param root
	 * @return
	 * @throws Exception
	 */
	public static SafeFile get(final String path, final Folder current, final Folder root) throws Exception
	{

		if (path.trim().equals(Folder.REGEX_DELIMITER))
			return root;

		final String [] tokens = path.split(Folder.REGEX_DELIMITER);

		if (tokens.length == 0)
			return null;

		final String [] comparableTokens = path.toUpperCase(Environment.getLocale()).split(Folder.REGEX_DELIMITER);

		if (comparableTokens[0].trim().equals(Folder.ROOT_NAME))
			return root.get(tokens, 1, tokens.length);
		else if (tokens[0].trim().equals("."))
		{
			if (tokens.length == 1)
				return current;
			// relative to current folder
			return current.get(tokens, 1, tokens.length);

		} else if (tokens[0].trim().equals(".."))
		{
			return current.getParent();
		} else
			// relative to current folder
			return current.get(tokens, 0, tokens.length);

	}

	/**
	 * Search all match of a path. To be used in case the path contains {@link Utils#WILDCARD}
	 * 
	 * @param path
	 * @param current
	 * @param root
	 * @param destination
	 * @return
	 * @throws Exception
	 */
	public static <D extends Collection<SafeFile>> D match(final String path, final Folder current, final Folder root, final D destination) throws Exception
	{

		if (!path.trim().contains(Folder.WILDCARD))
		{
			final SafeFile sf = get(path, current, root);

			if (sf != null)
				destination.add(sf);
		} else
		{

			final String [] tokens = path.split(Folder.REGEX_DELIMITER);

			if (tokens.length == 0)
				return destination;

			final String [] comparableTokens = path.toUpperCase(Environment.getLocale()).split(Folder.REGEX_DELIMITER);

			if (comparableTokens[0].trim().equals(Folder.ROOT_NAME))
				searchSafePath(root, comparableTokens, 1, destination);
			else if (tokens[0].trim().equals("."))
			{
				if (tokens.length == 1)
					destination.add(current);
				else
					// relative to current folder
					searchSafePath(current, comparableTokens, 1, destination);

			} else if (tokens[0].trim().equals(".."))
			{
				searchSafePath(current.getParent(), comparableTokens, 1, destination);
			} else
				// relative to current folder
				searchSafePath(current, comparableTokens, 0, destination);

		}

		return destination;

	}

	private static <D extends Collection<SafeFile>> void searchSafePath(final Folder folder, final String [] tokens, final int index, final D destination)
	{

		final String token = tokens[index];
		if (token.contains(Folder.WILDCARD))
		{

			for (final SafeFile sf : folder.listFiles())
			{

				final Pattern regex = Pattern.compile(token.toUpperCase(Environment.getLocale()).replace(Folder.WILDCARD, Folder.WILDCARD_REGEX));

				if (regex.matcher(sf.getComparableName()).matches() && !destination.contains(sf))
				{
					if (index == tokens.length - 1)
						destination.add(sf);
					else if (sf.isFolder())
						searchSafePath(((Folder) sf), tokens, index + 1, destination);

				}
			}

		} else
		{

			for (final SafeFile sf : folder.listFiles())

				if (sf.getComparableName().equals(token) && !destination.contains(sf))
				{
					if (index == tokens.length - 1)
						destination.add(sf);
					else if (sf.isFolder())
						searchSafePath(((Folder) sf), tokens, index + 1, destination);
				}

		}

	}

	
	/**
	 * Create all {@link Folder} required for the path
	 * 
	 * @param path:
	 *            the path to create
	 * @param blockPath:
	 *            true is the path belongs to a {@link Block}
	 * @param current
	 * @param root
	 * @throws Exception
	 */
	public static void mkdir(final String path, final boolean blockPath, final Folder current, final Folder root) throws Exception
	{

		final String [] tokens = path.split(Folder.REGEX_DELIMITER);

		if (tokens.length == 0)
			return;

		final String [] comparableTokens = path.toUpperCase(Environment.getLocale()).split(Folder.REGEX_DELIMITER);

		if (comparableTokens[0].trim().equals(Folder.ROOT_NAME))
		{
			root.mkdir(tokens, 1, blockPath);

		} else if (tokens[0].trim().equals("."))
		{
			// relative to current folder
			current.mkdir(tokens, 1, blockPath);

		} else
			// relative to current folder
			current.mkdir(tokens, 0, blockPath);

	}

	public static String getName(final String path)
	{

		final String [] tokens = path.split(Folder.REGEX_DELIMITER);

		if (tokens.length < 2)
			return null;

		return tokens[tokens.length - 1];

	}

	/**
	 * Remove forbidden <code>char</code> from the path and replace them with {@link Environment#getSubstitute()}
	 * 
	 * @param path:
	 *            the path to sanitize
	 * @return
	 */
	public static String sanitize(final String path)
	{

		final StringBuilder sb = new StringBuilder(path);

		final Character replacement = Environment.getSubstitute();

		c: for (int i = 0; i < sb.length(); i++)
		{
			if (sb.charAt(i) == Folder.DELIMITER)
				continue;

			if (sb.charAt(i) == java.io.File.separatorChar)
			{
				sb.setCharAt(i, Folder.DELIMITER);
				continue;
			}

			for (final char c : Environment.getForbidenChars())
				if (sb.charAt(i) == c)
				{
					if (replacement == null)
						sb.deleteCharAt(i--);
					else
						sb.setCharAt(i, replacement);
					continue c;
				}
		}
		return sb.toString();

	}

}
