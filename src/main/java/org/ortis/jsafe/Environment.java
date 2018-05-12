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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility class that provide helper to interact with the environment
 * 
 * @author Ortis <br>
 *         2018 Apr 26 7:52:34 PM <br>
 */
public abstract class Environment
{

	private static final List<Character> FORBIDEN_CHARS;
	private static Logger log;
	private final static Object logSync = new Object();
	static
	{

		final List<Character> chars = new ArrayList<>();
		chars.add('\\');
		chars.add('/');
		chars.add(':');
		chars.add('*');
		chars.add('?');
		chars.add('"');
		chars.add('<');
		chars.add('>');
		chars.add('|');
		chars.add('\u0000');
		chars.add('\n');
		chars.add('\t');

		chars.remove((Character) Folder.DELIMITER);

		FORBIDEN_CHARS = Collections.unmodifiableList(chars);

		final Logger log = Logger.getLogger("JSafe");
		log.setUseParentHandlers(false);

		log.addHandler(new CLIHandler());
		log.setLevel(Level.INFO);

		setLogger(log);
	}

	public static void setLogger(final Logger log)
	{
		synchronized (logSync)
		{
			Environment.log = log;
		}
	}

	public static Logger getLogger()
	{
		synchronized (logSync)
		{

			return log;
		}
	}

	public static Locale getLocale()
	{
		return Locale.getDefault();
	}

	public static List<Character> getForbidenChars()
	{
		return FORBIDEN_CHARS;
	}

	public static Character getSubstitute()
	{
		return '_';
	}

}
