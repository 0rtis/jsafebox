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

package io.ortis.jsafebox.commands;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.concurrent.Callable;
import java.util.logging.Logger;

import io.ortis.jsafebox.Environment;
import io.ortis.jsafebox.Safe;

import picocli.CommandLine;
import picocli.CommandLine.Command;

/**
 * Entry point of JSafe application. All command are subcommand of this one
 * 
 * @author Ortis <br>
 *         2018 Apr 26 8:14:45 PM <br>
 */
@Command(description = "Bootstrap", mixinStandardHelpOptions = true, version = Safe.VERSION, subcommands = { Init.class, Hash.class, List.class, Add.class, Delete.class, Cat.class, Extract.class,
		StartGui.class })
public class Bootstrap implements Callable<Void>
{

	@Override
	public Void call() throws Exception
	{
		displayMessage();
		return null;
	}

	public static void displayMessage() throws IOException
	{

		final Logger log = Environment.getLogger();
		final InputStream is = Bootstrap.class.getResourceAsStream("/ascii-art/jsafe-ascii-art");
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		int b;
		while ((b = is.read()) > -1)
			baos.write(b);
		final String art = new String(baos.toByteArray(), Charset.forName("UTF-8"));
		log.info(art);
	}

	/**
	 * Entry point of JSafe application
	 * 
	 * @param args
	 */
	public static void main(String [] args)
	{

		if (args.length == 0)
			try
			{
				new StartGui().call();
			} catch (final Exception e)
			{
				e.printStackTrace();
			}
		else
			CommandLine.call(new Bootstrap(), System.err, args);

	}

}
