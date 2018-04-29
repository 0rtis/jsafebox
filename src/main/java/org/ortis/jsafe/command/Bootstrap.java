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

package org.ortis.jsafe.command;

import java.util.concurrent.Callable;

import picocli.CommandLine;
import picocli.CommandLine.Command;

/**
 * Entry point of JSafe application. All command are subcommand of this one
 * 
 * @author Ortis <br>
 *         2018 Apr 26 8:14:45 PM <br>
 */
@Command(description = "Bootstrap", mixinStandardHelpOptions = true, version = Bootstrap.VERSION, subcommands = { Init.class, List.class, Add.class, Delete.class, Cat.class, Extract.class })
public class Bootstrap implements Callable<Void>
{
	public static final String VERSION = "0.1 alpha";

	@Override
	public Void call() throws Exception
	{
		return null;
	}

	/**
	 * Entry point of JSafe application
	 * 
	 * @param args
	 */
	public static void main(String [] args)
	{
		CommandLine.call(new Bootstrap(), System.err, args);
	}

}
