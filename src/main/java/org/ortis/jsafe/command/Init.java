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
package org.ortis.jsafe.command;

import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.logging.Logger;

import org.ortis.jsafe.Environment;
import org.ortis.jsafe.Safe;
import org.ortis.jsafe.Utils;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(description = "Init a new safe", name = "init", mixinStandardHelpOptions = true, version = Bootstrap.VERSION, showDefaultValues = true)
public class Init implements Callable<Void>
{

	@Option(names = { "-f", "--force" }, description = "Force delete if the file already exist")
	private boolean force;

	@Option(names = { "-H", "--header" }, arity = "2", description = "Clear text header key and value")
	private String [] headers;

	@Option(names = { "-p", "--property" }, arity = "2", description = "Encrypted text header key and value")
	private String [] properties;

	@Option(names = { "-pw", "--password" }, required = true, description = "Password")
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

			file: if (file.exists())
			{
				if (this.force)
					if (file.delete())
						break file;
				throw new IOException("File " + file + " already exist");
			}

			final Map<String, String> header = new HashMap<>();
			if (this.headers != null)
				for (int i = 0; i < headers.length; i += 2)
				{
					final String key = this.headers[i];
					final String value = this.headers[i + 1];
					log.info("Adding header '" + key + "' -> " + value);
					header.put(key, value);
				}

			final Map<String, String> properties = new HashMap<>();
			if (this.properties != null)
				for (int i = 0; i < this.properties.length; i += 2)
				{
					final String key = this.properties[i];
					final String value = this.properties[i + 1];
					log.info("Adding property entry '" + key + "' -> " + value);
					properties.put(key, value);
				}

			header.put(Safe.USER_MANUAL_LABEL, Safe.USER_MANUAL);
			header.put(Safe.ENCRYPTION_LABEL, "AES/CBC/PKCS5Padding");
			header.put(Safe.KEY_ALGO_LABEL, "AES");

			final SecureRandom random = new SecureRandom();
			final byte [] iv = new byte[16];
			random.nextBytes(iv);
			header.put(Safe.ENCRYPTION_IV_LABEL, Safe.GSON.toJson(iv));

			final MessageDigest md = MessageDigest.getInstance("SHA-256");

			final byte [] key = Arrays.copyOf(md.digest(md.digest(Utils.passwordToBytes(this.password.toCharArray()))), 128 >> 3);

			Safe.create(file, key, header, properties, this.bufferSize);

			
		} catch (final Exception e)
		{
			log.severe(Utils.formatException(e));
		}
		
		return null;
	}
}
