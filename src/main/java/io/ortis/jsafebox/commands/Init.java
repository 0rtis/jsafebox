/*
 *  Copyright 2019 Ortis (ortis@ortis.io)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package io.ortis.jsafebox.commands;

import java.io.File;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.logging.Logger;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import io.ortis.jsafebox.Environment;
import io.ortis.jsafebox.Safe;
import io.ortis.jsafebox.Utils;

import io.ortis.jsafebox.Version;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

/**
 * Create a new {@link Safe}
 * 
 * @author Ortis <br>
 *         2018 Apr 26 8:16:17 PM <br>
 */
@Command(description = "Init a new safe", name = "init", mixinStandardHelpOptions = true, version = Version.VERSION, showDefaultValues = true)
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
		innerHeader.put(Safe.ENCRYPTION_IV_LENGTH_LABEL, Integer.toString(16));

		final SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
		final byte [] salt = new byte[16];
		random.nextBytes(salt);

		innerHeader.put(Safe.PBKDF2_SALT_LABEL, Safe.GSON.toJson(salt));
		innerHeader.put(Safe.PBKDF2_ITERATION_LABEL, Integer.toString(Safe.PBKDF2_ITERATION));

		PBEKeySpec spec = new PBEKeySpec(password, salt, Safe.PBKDF2_ITERATION, 128);
		SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
		final byte [] key = skf.generateSecret(spec).getEncoded();

		Safe.create(file, key, innerHeader, innerProperties, bufferSize).close();
	}

}
