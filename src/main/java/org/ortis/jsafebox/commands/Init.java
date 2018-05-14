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

package org.ortis.jsafebox.commands;

import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.logging.Logger;

import org.ortis.jsafebox.Environment;
import org.ortis.jsafebox.Safe;
import org.ortis.jsafebox.Utils;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

/**
 * Create a new {@link Safe}
 * 
 * @author Ortis <br>
 *         2018 Apr 26 8:16:17 PM <br>
 */
@Command(description = "Init a new safe", name = "init", mixinStandardHelpOptions = true, version = Bootstrap.VERSION, showDefaultValues = true)
public class Init implements Callable<Void>
{

	@Option(names = { "-H", "--header" }, arity = "2", description = "Clear text header key and value")
	private String [] headers;

	@Option(names = { "-p", "-pp", "--property" }, arity = "2", description = "Encrypted text header key and value")
	private String [] properties;

	@Option(names = { "-pw", "-pwd", "--password" }, required = true, description = "Password")
	private String password;

	@Option(names = { "-b", "--buffer" }, description = "Read buffer size")
	private int bufferSize = 1024;

	@Parameters(index = "0", description = "File path of safe file")
	private String filePath;

	@Override
	public Void call() throws Exception
	{
		final Logger log = Environment.getLogger();

		try
		{
			final File file = new File(this.filePath);

			if (file.exists())
				throw new IOException("File " + file + " already exist");

			final Map<String, String> header = new LinkedHashMap<>();
			if (this.headers != null)
				for (int i = 0; i < headers.length; i += 2)
				{
					final String key = this.headers[i];
					final String value = this.headers[i + 1];
					log.info("Adding header '" + key + "' -> " + value);
					header.put(key, value);
				}

			final Map<String, String> properties = new LinkedHashMap<>();
			if (this.properties != null)
				for (int i = 0; i < this.properties.length; i += 2)
				{
					final String key = this.properties[i];
					final String value = this.properties[i + 1];
					log.info("Adding property entry '" + key + "' -> " + value);
					properties.put(key, value);
				}

			log.info("Creating new safebox " + file.getAbsolutePath() + "...");
			init(file, password.toCharArray(), header, properties, this.bufferSize);
			log.info("Done");

		} catch (final Exception e)
		{
			log.severe(Utils.formatException(e));
		}

		return null;
	}

	public static void init(final File file, final char [] password, final Map<String, String> header, final Map<String, String> properties, final int bufferSize) throws Exception
	{

		final Map<String, String> innerProperties = new LinkedHashMap<>();
		if (properties != null)
			innerProperties.putAll(properties);

		final Map<String, String> innerHeader = new LinkedHashMap<>();
		if (header != null)
			innerHeader.putAll(header);

		innerHeader.put(Safe.PROTOCOL_SPEC_LABEL, Safe.PROTOCOL_SPEC);
		innerHeader.put(Safe.ENCRYPTION_LABEL, "AES/CBC/PKCS5Padding");
		innerHeader.put(Safe.KEY_ALGO_LABEL, "AES");

		final SecureRandom random = new SecureRandom();
		final byte [] iv = new byte[16];
		random.nextBytes(iv);
		innerHeader.put(Safe.ENCRYPTION_IV_LABEL, Safe.GSON.toJson(iv));

		final MessageDigest md = MessageDigest.getInstance("SHA-256");

		final byte [] key = Arrays.copyOf(md.digest(md.digest(Utils.passwordToBytes(password))), 128 >> 3);
		Safe.create(file, key, innerHeader, innerProperties, bufferSize).close();

	}

}
