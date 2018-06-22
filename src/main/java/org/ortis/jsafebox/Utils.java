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

package org.ortis.jsafebox;

import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Utility class
 * 
 * @author Ortis <br>
 *         2018 Apr 26 8:06:47 PM <br>
 */
public class Utils
{

	public final static String SEPARATOR_REGEX = "[/|" + Pattern.quote(java.io.File.separator) + "]";

	private final static String SYSTEM_PATH_DELIMITER_REGEX = Pattern.quote(File.separator) + "|" + Pattern.quote("/") + "|" + Pattern.quote("\\");

	public static byte [] passwordToBytes(final char [] chars)
	{
		final CharBuffer charBuffer = CharBuffer.wrap(chars);
		final ByteBuffer byteBuffer = StandardCharsets.UTF_8.encode(charBuffer);
		final byte [] bytes = Arrays.copyOfRange(byteBuffer.array(), byteBuffer.position(), byteBuffer.limit());
		Arrays.fill(charBuffer.array(), '\u0000'); // clear sensitive data
		Arrays.fill(byteBuffer.array(), (byte) 0); // clear sensitive data
		return bytes;
	}

	public static List<java.io.File> parseSystemPath(String query, final List<java.io.File> destination) throws IOException
	{
		final String [] tokens = query.split(SYSTEM_PATH_DELIMITER_REGEX);

		Path baseDirectory = null;

		if (tokens[0].equals(".") || tokens[0].equals(".."))// Relative path
		{
			baseDirectory = new File(tokens[0]).toPath();

			final StringBuilder sb = new StringBuilder();
			for (int i = 1; i < tokens.length; i++)
				if (sb.length() == 0)
					sb.append(tokens[i]);
				else
					sb.append(File.separator + tokens[i]);

			query = "**" + File.separator + sb.toString();

		} else // Absolute path
		{

			final String comparableToken = tokens[0].toUpperCase();
			for (final File root : File.listRoots())
				if (root.getAbsolutePath().toUpperCase().equals(comparableToken))
				{
					// perfect match
					baseDirectory = root.toPath();
					break;

				}

			if (baseDirectory == null)
				for (final File root : File.listRoots())
				{
					String rootPath = root.getAbsolutePath().toUpperCase();
					rootPath = rootPath.substring(0, rootPath.length() - 1);
					if (rootPath.equals(comparableToken))
					{
						baseDirectory = root.toPath();
						break;
					}

				}
		}

		if (baseDirectory == null)
			throw new IOException("Could not locate base directory '" + tokens[0] + "'");

		Path path = baseDirectory;
		for (int i = 1; i < tokens.length; i++)
		{
			try
			{
				path = Paths.get(path.toString(), tokens[i]);
			} catch (final Exception e)
			{
				// Here, we have reach a special character and the start point for the search is
				// in path
			}
		}

		final String escapedQuery = query.replace("\\", "\\\\");// PathMatcher does not escape backslash properly. Need to do the escape manually for Windows OS path handling. This might be a bug of Java implementation.
		// Need to check on Oracle bug report database.

		final PathMatcher pathMatcher = FileSystems.getDefault().getPathMatcher("glob:" + escapedQuery);
		Files.walkFileTree(path, new FileVisitor<Path>()
		{
			@Override
			public FileVisitResult postVisitDirectory(final Path dir, final IOException exc) throws IOException
			{
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attrs) throws IOException
			{
				if (pathMatcher.matches(dir))
				{
					destination.add(dir.toFile());
					return FileVisitResult.SKIP_SUBTREE;
				}

				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException
			{
				if (pathMatcher.matches(file))
					destination.add(file.toFile());

				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult visitFileFailed(final Path file, final IOException exc) throws IOException
			{
				return FileVisitResult.CONTINUE;
			}
		});

		return destination;

	}

	/**
	 * Return the MIME type of a file
	 * 
	 * @param file
	 * @return
	 */
	public static String getMIMEType(final java.io.File file)
	{
		final String name = file.getName().toUpperCase();
		final String [] buffer = name.split("\\.");
		final String extenstion;

		if (buffer.length == 0)
			extenstion = "";
		else
			extenstion = buffer[buffer.length - 1];

		switch (extenstion)
		{
			case "TXT":
				/**
				 * Log
				 */
			case "LOG":
				/**
				 * Source Code
				 */
			case "JAVA":
			case "PROPERTIES":
				// C/C++
			case "H":
			case "C":
			case "CPP":
				// Python
			case "PY":
				// Javascript
			case "JS":
				// .bat
			case "BAT":
				// Bash
			case "SH":
				/**
				 * Conf
				 */
				// Yalm
			case "YML":
			case "YAML":
				// Mark Down
			case "MD":
				return "text/plain";

			case "CSV":
				return "text/csv";

			case "HTM":
			case "HTML":
				return "text/html";

			case "JPG":
			case "JPEG":
				return "image/jpg";

			case "PNG":
				return "image/png";

			case "BM":
			case "BMP":
				return "image/bmp";

			case "PDF":
				return "application/pdf";

			case "AVI":
				return "video/x-msvideo";

			case "MPEG":
				return "video/mpeg";

			case "MP4":
				return "video/mp4";

			case "MKV":
				return "video/x-matroska";

			case "MP3":
				return "audio/mpeg";

			default:
				return "application/octet-stream";
		}
	}

	/**
	 * Format the exception message
	 * 
	 * @param t
	 * @return
	 */
	public static String formatException(final Throwable t)
	{
		if (t == null)
			return null;

		final Throwable cause = t.getCause();
		final String msg = cause == null ? null : formatException(cause);
		return formatException(t.getClass(), msg, t.toString(), t.getStackTrace());

	}

	private static String formatException(final Class<?> exceptionClass, final String cause, final String msg, final StackTraceElement [] exceptionStack)
	{
		final StringBuilder builder = new StringBuilder();

		if (msg != null)
			builder.append(msg);

		if (exceptionStack != null)
		{
			builder.append(System.lineSeparator());
			for (int i = 0; i < exceptionStack.length; i++)
			{
				final String stackElement = exceptionStack[i].toString();

				builder.append(stackElement + System.lineSeparator());
			}
		}

		if (cause != null)
			builder.append("Caused by " + cause);

		return builder.toString();
	}

	/**
	 * Remove forbidden <code>char</code> from the path and replace them with <code>substitute</code>
	 * 
	 * @param path:
	 *            the path to sanitize
	 * @param delimiter:
	 *            delimiter of the path
	 * @param substitute:
	 *            replacement char
	 * @return
	 */
	public static String sanitize(final String path, final Character delimiter, final Character substitute)
	{
		final String [] tokens = path.split(Pattern.quote(Character.toString(delimiter)));

		final StringBuilder sb = new StringBuilder();
		for (int i = 0; i < tokens.length; i++)
		{
			if (i < tokens.length - 1)
				sb.append(sanitizeToken(tokens[i], substitute)).append(delimiter);
			else
				sb.append(sanitizeToken(tokens[i], substitute));

		}

		return sb.toString();
	}

	public static String sanitizeToken(final String token, final Character substitute)
	{

		final StringBuilder sb = new StringBuilder(token);

		final Character replacement = substitute;

		c: for (int i = 0; i < sb.length(); i++)
		{

			if (sb.charAt(i) == java.io.File.separatorChar || sb.charAt(i) == Folder.DELIMITER)
			{
				if (replacement == null)
					sb.deleteCharAt(i--);
				else
					sb.setCharAt(i, replacement);
				continue c;
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

	public static boolean isHeadless()
	{
		if (GraphicsEnvironment.isHeadless())
			return true;

		try
		{
			GraphicsDevice [] screenDevices = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();
			return screenDevices == null || screenDevices.length == 0;
		} catch (HeadlessException e)
		{
			return true;
		}
	}

}
