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

import java.util.Collection;
import java.util.regex.Pattern;

public abstract class SafeFiles
{

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

	public static <D extends Collection<SafeFile>> D match(final String path, final Folder current, final Folder root, final D destination) throws Exception
	{

		if (!path.trim().contains(Utils.WILDCARD))
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
				searchSafePath(current.getParent(), comparableTokens, 0, destination);

		}

		return destination;

	}

	private static <D extends Collection<SafeFile>> int searchSafePath(final Folder folder, final String [] tokens, final int index, final D destination)
	{

		// for (final String token : tokens)
		for (int i = index; i < tokens.length; i++)
		{
			final String token = tokens[i];
			if (token.contains(Utils.WILDCARD))
			{

				for (final SafeFile sf : folder.listFiles())
				{

					final Pattern regex = Pattern.compile(token.toUpperCase(Environment.getLocale()).replace(Utils.WILDCARD, Utils.WILDCARD_REGEX));

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

		return tokens.length;

	}

	public static void mkdir(final String path, final boolean isFfilePath, final Folder current, final Folder root) throws Exception
	{

		final String [] tokens = path.split(Folder.REGEX_DELIMITER);

		if (tokens.length == 0)
			return;

		final String [] comparableTokens = path.toUpperCase(Environment.getLocale()).split(Folder.REGEX_DELIMITER);

		if (comparableTokens[0].trim().equals(Folder.ROOT_NAME))
		{
			root.mkdir(tokens, 1, isFfilePath);

		} else if (tokens[0].trim().equals("."))
		{
			// relative to current folder
			root.mkdir(tokens, 1, isFfilePath);

		} else
			// relative to current folder
			root.mkdir(tokens, 0, isFfilePath);

	}

	public static String sanitize(final String path)
	{

		final StringBuilder sb = new StringBuilder(path);

		final Character replacement = Environment.getForbidenSubstitute();

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
