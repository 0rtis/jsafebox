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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Random;
import java.util.logging.Logger;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.ortis.jsafe.commands.Bootstrap;

import picocli.CommandLine;

public class CommandTest
{
	private static File folder;
	private static Random random;
	private static Logger log;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception
	{
		random = TestUtils.getRandom();
		log = TestUtils.getLog();
		folder = TestUtils.mkdir();
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception
	{
		TestUtils.delete(folder);
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception
	{

	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception
	{

	}

	@Test
	public void test() throws Exception
	{

		final File safeFile = new File(folder, TestUtils.randomString(random, 10) + ".safe");
		if (safeFile.exists())
			throw new Exception("Safe file " + safeFile.getAbsolutePath() + " already exists");

		String [] args = new String[] { "init", "--password", "mypassword", safeFile.getAbsolutePath(), "--header", "headerKey", "headerValue", "--property", "propertyKey", "propertyValue" };
		CommandLine.call(new Bootstrap(), System.err, args);

		assertTrue(safeFile.exists());

		final InputStream is = SafeTest.class.getResourceAsStream("/img/Gentleman.sh-600x600.png");

		if (is == null)
			throw new Exception("Could not load resource file /img/Gentleman.sh-600x600.png");

		final File systemFile = new File(folder, TestUtils.randomString(random, 5));

		if (systemFile.exists())
			throw new Exception("System file " + systemFile.getAbsolutePath() + " already exists");

		final FileOutputStream fos = new FileOutputStream(systemFile);

		int b;
		while ((b = is.read()) > -1)
			fos.write(b);

		is.close();
		fos.flush();
		fos.close();

		if (!systemFile.exists())
			throw new Exception("System file " + systemFile.getAbsolutePath() + " not found");

		final String safeFolderPath = Folder.ROOT_NAME + Folder.DELIMITER + "folder";
		args = new String[] { "add", "--password", "mypassword", safeFile.getAbsolutePath(), "-m", "-pp", "propertyKey", "propertyValue", systemFile.getAbsolutePath(), safeFolderPath };
		Bootstrap.main(args);

		args = new String[] { "ls", "--password", "mypassword", safeFile.getAbsolutePath(), safeFolderPath };
		CommandLine.call(new Bootstrap(), System.err, args);

		args = new String[] { "cat", "--password", "mypassword", safeFile.getAbsolutePath(), safeFolderPath + Folder.DELIMITER + systemFile.getName() };
		Bootstrap.main(args);

		final File extractTargetSystemFolder = new File(folder, TestUtils.randomString(random, 10) + ".extracted");

		if (!extractTargetSystemFolder.exists())
		{
			log.info("Creating folder " + extractTargetSystemFolder.getAbsolutePath());
			extractTargetSystemFolder.mkdir();
			if (!extractTargetSystemFolder.exists())
				throw new Exception("Could not create folder " + extractTargetSystemFolder.getAbsolutePath());
		}

		if (!extractTargetSystemFolder.isDirectory())
			throw new Exception("Path " + extractTargetSystemFolder.getAbsolutePath() + " is not a directory");

		final File extractTargetSystemFile = new File(extractTargetSystemFolder, systemFile.getName());
		if (extractTargetSystemFile.exists())
			throw new Exception("Extract target file " + safeFile.getAbsolutePath() + " already exists");

		args = new String[] { "extract", "--password", "mypassword", safeFile.getAbsolutePath(), safeFolderPath + Folder.DELIMITER + systemFile.getName(),
				extractTargetSystemFolder.getAbsolutePath() };
		Bootstrap.main(args);

		assertTrue(extractTargetSystemFile.exists());

		assertArrayEquals(Files.readAllBytes(systemFile.toPath()), Files.readAllBytes(extractTargetSystemFile.toPath()));

	}

}
