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
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Matcher;
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
			throw new IOException("File " + file + " already exist");

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

	/**
	 * Parse the system path whith a support for {@link Utils#WILDCARD}
	 * 
	 * @param path:
	 *            system path to parse
	 * @param destination
	 * @return
	 */
	public static List<java.io.File> parseSystemPath(final String path, final List<java.io.File> destination)
	{

		final String [] tokens = path.split(SEPARATOR_REGEX);
		searchSystemPath(null, tokens, 0, destination);

		return destination;

	}

	private static int searchSystemPath(java.io.File folder, final String [] tokens, final int index, final List<java.io.File> destination)
	{

		// for (final String token : tokens)
		for (int i = index; i < tokens.length; i++)
		{
			final String token = tokens[i];
			if (token.contains(Folder.WILDCARD))
			{
				if (i == 0)
				{// search all root drives
					for (final java.io.File root : java.io.File.listRoots())
						i = searchSystemPath(root, tokens, i + 1, destination);

				} else
				{

					final Pattern regex = Pattern.compile(token.toUpperCase(Environment.getLocale()).replace(Folder.WILDCARD, Folder.WILDCARD_REGEX));

					final String [] matches = folder.list(new FilenameFilter()
					{

						@Override
						public boolean accept(final java.io.File dir, String name)
						{
							name = name.toUpperCase(Environment.getLocale());
							final Matcher matcher = regex.matcher(name);

							return matcher.matches();
						}
					});

					// System.out.println(Arrays.toString(matches));
					for (final String match : matches)
					{

						final java.io.File file = new java.io.File(folder, match);
						if (file.isDirectory())
							i = searchSystemPath(file, tokens, i + 1, destination);
						else if (!destination.contains(file))
							destination.add(file);

					}

				}

			} else
			{
				final java.io.File file;
				if (i == 0)
				{
					file = new java.io.File(token);
				} else
					file = new java.io.File(folder, token);

				if (!file.exists())
					return i;
				if (file.isDirectory())
					folder = file;
				else if (!destination.contains(file))
					destination.add(file);

			}

		}

		return tokens.length;

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
			return "";
		// return "[Error while parsingCannot format null Throwable]";

		final Throwable cause = t.getCause();
		final String msg = cause == null ? null : formatException(cause);
		return formatException(t.getClass(), msg, t.toString(), t.getStackTrace());

	}

	private static String formatException(final Class<?> exceptionClass, final String cause, final String msg, final StackTraceElement [] exceptionStack)
	{
		final StringBuilder builder = new StringBuilder();

		// if (exceptionClass != null)
		// builder.append(exceptionClass.getCanonicalName());

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

}
