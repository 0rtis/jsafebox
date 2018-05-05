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

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Utility class
 * 
 * @author Ortis <br>
 *         2018 Apr 26 8:06:47 PM <br>
 */
public class Utils
{

	public final static String SEPARATOR_REGEX = "[/|" + Pattern.quote(java.io.File.separator) + "]";

	public static byte [] passwordToBytes(final char [] chars)
	{
		final CharBuffer charBuffer = CharBuffer.wrap(chars);
		final ByteBuffer byteBuffer = Charset.forName(Safe.UTF8).encode(charBuffer);
		final byte [] bytes = Arrays.copyOfRange(byteBuffer.array(), byteBuffer.position(), byteBuffer.limit());
		Arrays.fill(charBuffer.array(), '\u0000'); // clear sensitive data
		Arrays.fill(byteBuffer.array(), (byte) 0); // clear sensitive data
		return bytes;
	}

	/**
	 * Open a {@link Safe}
	 * 
	 * @param safeFilePath:
	 *            system path to the safe file
	 * @param password:
	 *            the encryption password
	 * @param bufferSize:size
	 *            of the <code>byte</code> buffer to be used in IO operation
	 * @param log
	 * @return
	 * @throws Exception
	 */
	public static Safe open(final String safeFilePath, final char [] password, final int bufferSize, final Logger log) throws Exception
	{
		final File file = new File(safeFilePath);

		if (!file.exists())
			throw new IOException("Safe file " + file + " doest not exist");

		final Map<String, String> header = Safe.readHeader(file, bufferSize);

		final String encyption = header.get(Safe.ENCRYPTION_LABEL);
		if (encyption == null)
			throw new Exception("Could not read property '" + Safe.ENCRYPTION_LABEL + "' from header");

		log.fine("Encryption type " + encyption);
		final Cipher cipher = javax.crypto.Cipher.getInstance(encyption);

		if (!header.containsKey(Safe.KEY_ALGO_LABEL))
			throw new Exception("Could not read property '" + Safe.KEY_ALGO_LABEL + "' from header");

		log.fine("Key algorithm " + header.get(Safe.KEY_ALGO_LABEL));

		final MessageDigest md = MessageDigest.getInstance("SHA-256");
		final byte [] key = Arrays.copyOf(md.digest(md.digest(Utils.passwordToBytes(password))), 128 >> 3);
		final SecretKeySpec keySpec = new SecretKeySpec(key, header.get(Safe.KEY_ALGO_LABEL));

		final IvParameterSpec iv;

		if (header.containsKey(Safe.ENCRYPTION_IV_LABEL))
			iv = new IvParameterSpec(Safe.GSON.fromJson(header.get(Safe.ENCRYPTION_IV_LABEL), Safe.BYTE_ARRAY_TYPE));
		else
			iv = null;

		return new Safe(file, cipher, keySpec, iv, 1024);
	}

	private final static String SYSTEM_PATH_DELIMITER_REGEX = Pattern.quote(File.separator) + "|" + Pattern.quote("/") + "|" + Pattern.quote("\\");

	public static List<java.io.File> parseSystemPath(final String query, final List<java.io.File> destination) throws IOException
	{
		final String [] tokens = query.split(SYSTEM_PATH_DELIMITER_REGEX);

		Path baseDirectory = null;

		if (tokens[0].equals(".") || tokens[0].equals(".."))
		{
			baseDirectory = new File(tokens[0]).toPath();
		} else
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
			throw new IOException("Could not locate base directory " + tokens[0]);

		Path path = baseDirectory;
		for (int i = 1; i < tokens.length; i++)
		{

			try
			{
				path = Paths.get(path.toString(), tokens[i]);
			} catch (final Exception e)
			{
				// here, we have reach a special character and the start point for the search is
				// in path
			}
		}

		final String escapedQuery = query.replace("\\", "\\\\");// PathMatcher does not escape backslash properly. Need to do the escape manually for Windows OS path handling. This might be a bug of Java implentation.
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

		if (name.endsWith(".JPG") || name.endsWith(".JPEG"))
			return "image/jpg";
		else if (name.endsWith(".PNG"))
			return "image/png";
		else if (name.endsWith(".BM") || name.endsWith(".BMP"))
			return "image/bmp";
		else if (name.endsWith(".TXT"))
			return "text/plain";
		else if (name.endsWith(".PDF"))
			return "application/pdf";
		else if (name.endsWith(".AVI"))
			return "video/x-msvideo";
		else if (name.endsWith(".MPEG"))
			return "video/mpeg";
		else if (name.endsWith(".MP4"))
			return "video/mp4";
		else if (name.endsWith(".MKV"))
			return "video/x-matroska";
		else if (name.endsWith(".MP3"))
			return "audio/mpeg";
		else
			return "application/octet-stream";
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
				sb.append(sanitizeToken(tokens[i], substitute) + delimiter);
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
