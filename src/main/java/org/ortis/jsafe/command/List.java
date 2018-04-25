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

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.logging.Logger;

import org.ortis.jsafe.Block;
import org.ortis.jsafe.Environment;
import org.ortis.jsafe.Folder;
import org.ortis.jsafe.Safe;
import org.ortis.jsafe.SafeFile;
import org.ortis.jsafe.SafeFiles;
import org.ortis.jsafe.Utils;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(description = "List content", name = "ls", mixinStandardHelpOptions = true, version = Bootstrap.VERSION, showDefaultValues = true)
public class List implements Callable<Void>
{
	private final static DecimalFormat BYTE_FORMAT = new DecimalFormat("###,###");

	@Option(names = { "-pw", "--password" }, required = true, description = "Password")
	private String password;

	@Option(names = { "-b", "--buffer" }, description = "Read buffer size")
	private int bufferSize = 1024;

	@Parameters(index = "0", description = "File path of safe file")
	private String safeFile;

	@Parameters(index = "1", arity = "1...*", description = "Path to list")
	private String [] paths;

	@Override
	public Void call() throws Exception
	{
		final Logger log = Environment.getLogger();

		try
		{

			final Safe safe = Utils.open(this.safeFile, this.password.toCharArray(), this.bufferSize, log);

			final java.util.Set<SafeFile> safeFiles = new LinkedHashSet<>();

			for (final String path : this.paths)
			{
				log.fine("Lookup " + path + "...");
				final java.util.List<SafeFile> buffer = new ArrayList<>();
				SafeFiles.match(path, safe.getRootFolder(), safe.getRootFolder(), buffer);

				if (buffer.size() == 1)
				{
					if (buffer.get(0).isFolder())
					{
						final Folder folder = (Folder) buffer.get(0);
						for (final SafeFile sf : folder.listFiles())
							safeFiles.add(sf);
					} else
						safeFiles.addAll(buffer);

				} else
					safeFiles.addAll(buffer);

			}

			if (safeFiles.isEmpty())
			{
				log.info("No file found");

			} else if (safeFiles.size() == 1)
			{

				final StringBuilder sb = new StringBuilder("\n");
				final SafeFile sf = safeFiles.iterator().next();

				if (sf.isBlock())
				{
					final Block block = (Block) sf;
					final Map<String, String> metadatas = safe.readMetadata(block);
					sb.append(block.getPath() + "\t" + BYTE_FORMAT.format(block.getDataLength()) + " bytes\n");
					for (final Map.Entry<String, String> e : metadatas.entrySet())
						sb.append(e.getKey() + ": " + e.getValue() + "\n");

				} else
				{
					final Folder folder = (Folder) sf;

					int bCount = 0;
					long size = 0;
					int fCount = 0;

					for (final SafeFile ssf : folder.listFiles())
						if (ssf.isBlock())
						{
							final Block block = (Block) ssf;
							sb.append(ssf.getPath() + "\t" + BYTE_FORMAT.format(block.getDataLength()) + " bytes\n");
							bCount++;
							size += block.getDataLength();
						} else
						{
							sb.append(ssf.getPath() + "\t" + ((Folder) ssf).listFiles().size() + " child(s)\n");
							fCount++;
						}

					sb.append("\n");

					if (bCount > 0)
						sb.append(bCount + " file(s) - Total " + BYTE_FORMAT.format(size) + " bytes");

					if (fCount > 0)
					{
						if (bCount > 0)
							sb.append("\n");
						sb.append(fCount + " folder(s)");
					}

				}

				log.info(sb.toString());

			} else
			{

				int bCount = 0;
				long size = 0;
				int fCount = 0;

				final StringBuilder sb = new StringBuilder("\n");

				for (final SafeFile sf : safeFiles)
				{

					if (sf.isBlock())
					{
						final Block block = (Block) sf;

						sb.append(sf.getPath() + "\t" + BYTE_FORMAT.format(block.getDataLength()) + " bytes\n");
						size += block.getDataLength();
						bCount++;
					} else
					{
						final Folder folder = (Folder) sf;
						sb.append(folder.getPath() + "\t" + folder.listFiles().size() + " child(s)\n");
						fCount++;
					}

				}

				sb.append("\n");

				if (bCount > 0)
					sb.append(bCount + " file(s) found - Total " + BYTE_FORMAT.format(size) + " bytes");

				if (fCount > 0)
				{
					if (bCount > 0)
						sb.append("\n");
					sb.append(fCount + " folder(s) found");
				}

				log.info(sb.toString());

			}

		} catch (final Exception e)
		{
			log.severe(Utils.formatException(e));
		}

		return null;
	}

}
