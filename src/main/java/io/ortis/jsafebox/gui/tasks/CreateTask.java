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

package io.ortis.jsafebox.gui.tasks;

import io.ortis.jsafebox.Safe;
import io.ortis.jsafebox.cli.Init;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * @author Ortis
 */
public class CreateTask extends AbstractGUITask
{
	private final File destination;

	private final String pbkdf2;
	private final char[] pwd1;
	private final char[] pwd2;

	public CreateTask(final File destination, final String pbkdf2, char[] pwd1, final char[] pwd2, final Logger log)
	{
		super("Generating safe", "Success", "Safe has been successfully generated !", log);

		this.destination = destination;
		this.pbkdf2 = pbkdf2;
		this.pwd1 = pwd1;
		this.pwd2 = pwd2;
	}

	@Override
	public boolean skipResultOnSuccess()
	{
		return false;
	}

	@Override
	public void task() throws Exception
	{
		try
		{
			log.info("Parsing pbkdf2");
			final int pbkdf2 = Integer.parseInt(this.pbkdf2.trim());
			if(pbkdf2 <= 0)
				throw new IllegalArgumentException("PBKDF2 must be greater than zero");

			log.info("Checking password");

			if(this.pwd1.length <= 0)
				throw new IllegalArgumentException("Password cannot be empty");

			if(!Arrays.equals(this.pwd1, this.pwd2))
				throw new IllegalArgumentException("Password mismatch");


			log.info("Writing safe file");

			final Map<String, String> header = new HashMap<>();
			header.put(Safe.PBKDF2_ITERATION_LABEL, Integer.toString(pbkdf2));

			Init.init(destination, pwd1, header, null, 1024 * 8);

		} finally
		{

		}
	}
}
