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

package io.ortis.jsafebox.cli;

import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.logging.Logger;


import io.ortis.jsafebox.*;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

/**
 * Output {@link Block} content as text
 * 
 * @author Ortis <br>
 *         2018 Apr 26 8:16:54 PM <br>
 */
@Command(description = "Check the integrity hash", name = "hash", mixinStandardHelpOptions = true, version = Version.VERSION, showDefaultValues = true)
public class Hash implements Callable<Void>
{

	@Option(names = { "-pw", "-pwd", "--password" }, description = "Password")
	private String password;

	@Option(names = { "-b", "--buffer" }, description = "Read buffer size")
	private int bufferSize = 65536;

	@Parameters(index = "0", arity = "0...1", description = "File path of safe file")
	private String safeFile;

	@Override
	public Void call() throws Exception
	{
		
		final Logger log = Environment.getLogger();

		try (final Safe safe = Safe.open(this.safeFile, this.password.toCharArray(), this.bufferSize, log))
		{
			System.gc();// Somehow, it looks like the GC is having trouble detecting large heap when using ByteBuffer. So we just call it before and after hash computation
			final byte [] hash = safe.computeHash(null);
			System.gc();

			final String readableHash = Utils.bytesToHex(hash);
			if (Arrays.equals(hash, safe.getHash()))

				log.info("Integrity hash " + readableHash + " sucessfully verified");
			else 
			{
				
				final String expectedHash = Utils.bytesToHex(safe.getHash());
				log.warning("Integrity hash is "+readableHash+" but "+expectedHash+" was expected");
				log.warning("The content of the file might have been altered. It is strongly advised to revert to a backup file");
			}

		} catch (final Exception e)
		{
			log.severe(Utils.formatException(e));
		}

		return null;
	}

}
