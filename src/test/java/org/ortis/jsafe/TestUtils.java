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
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TestUtils
{

	private static final char [] CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123446789".toCharArray();

	private static Logger log;

	private final static Random RANDOM = new Random();

	static
	{
		log = Logger.getAnonymousLogger();
		log.setLevel(Level.ALL);
		java.util.logging.ConsoleHandler handler = new java.util.logging.ConsoleHandler();
		handler.setLevel(Level.ALL);
		log.addHandler(handler);

	}

	public static Logger getLog()
	{
		return log;
	}

	public static String randomString(final Random random, final int length)
	{
		final StringBuilder sb = new StringBuilder();
		for (int i = 0; i < length; i++)
			sb.append(CHARS[random.nextInt(CHARS.length)]);

		return sb.toString();

	}

	public static Random getRandom()
	{
		return RANDOM;
	}

	public static File mkdir() throws Exception
	{

		File folder = null;
		for (int i = 0; i < 100; i++)
		{

			folder = new File(TestUtils.randomString(RANDOM, 10) + ".safe");
			if (!folder.exists())
				break;

		}

		if (folder.exists())
			throw new Exception("Could not find non existing folder");

		log.fine("Creating folder " + folder.getAbsolutePath());

		if (!folder.mkdir())
			throw new Exception("Could not create folder " + folder.getAbsolutePath());

		return folder;
	}

	public static void delete(final File file) throws Exception
	{
		if (file == null)
			return;

		if (file.isDirectory())
			for (final File f : file.listFiles())
				delete(f);

		if (file.isDirectory())
			log.fine("Deleting folder " + file);
		else
			log.fine("Deleting file " + file);

		if (!file.delete())
			throw new Exception("Could not delete file " + file.getAbsolutePath());

	}
}
