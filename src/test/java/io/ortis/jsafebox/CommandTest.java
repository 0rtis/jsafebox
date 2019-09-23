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

package io.ortis.jsafebox;

import io.ortis.jsafebox.commands.Bootstrap;
import org.junit.*;
import picocli.CommandLine;

import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Random;
import java.util.logging.Logger;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;

public class CommandTest
{
	private static File folder;
	private static Random random;
	private static Logger log;

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

		new Bootstrap().call();

		/**
		 * Init
		 */
		final File safeFile = new File(folder, TestUtils.randomString(random, 10) + ".safe");
		if(safeFile.exists())
			throw new Exception("Safe file " + safeFile.getAbsolutePath() + " already exists");

		String[] args = new String[]{"init", "--password", "mypassword", safeFile.getAbsolutePath(), "--header", "headerKey", "headerValue", "--property", "safePropertyKey", "safePropertyValue"};
		CommandLine.call(new Bootstrap(), System.err, args);

		assertTrue(safeFile.exists());

		// try to init on existing file
		args = new String[]{"init", "--password", "mypassword", safeFile.getAbsolutePath(), "--header", "headerKey", "headerValue", "--property", "safePropertyKey", "safePropertyValue"};
		CommandLine.call(new Bootstrap(), System.err, args);

		/**
		 * Hash
		 */

		args = new String[]{"hash", "--password", "mypassword", safeFile.getAbsolutePath()};
		Bootstrap.main(args);

		/**
		 * Add
		 */
		final InputStream is = SafeTest.class.getResourceAsStream("/img/Gentleman.sh-600x600.png");

		if(is == null)
			throw new Exception("Could not load resource file /img/Gentleman.sh-600x600.png");

		final File systemFile = new File(folder, TestUtils.randomString(random, 5));

		if(systemFile.exists())
			throw new Exception("System file " + systemFile.getAbsolutePath() + " already exists");

		final FileOutputStream fos = new FileOutputStream(systemFile);

		int b;
		while((b = is.read()) > -1)
			fos.write(b);

		is.close();
		fos.flush();
		fos.close();

		if(!systemFile.exists())
			throw new Exception("System file " + systemFile.getAbsolutePath() + " not found");

		final String safeFolderPath = Folder.ROOT_NAME + Folder.DELIMITER + "folder";

		// no destination
		args = new String[]{"add", "--password", "mypassword", safeFile.getAbsolutePath(), "-pp", "filePropertyKey", "filePropertyValue", systemFile.getAbsolutePath(), "404"};
		Bootstrap.main(args);

		// no source
		args = new String[]{"add", "--password", "mypassword", safeFile.getAbsolutePath(), "-m", "-pp", "filePropertyKey", "filePropertyValue", systemFile.getAbsolutePath() + "404", safeFolderPath};
		Bootstrap.main(args);

		// add whole file system folder
		args = new String[]{"add", "--password", "mypassword", safeFile.getAbsolutePath(), "-m", "-pp", "filePropertyKey", "filePropertyValue", systemFile.getParentFile().getAbsolutePath(), safeFolderPath};
		Bootstrap.main(args);

		// add a single file system file
		args = new String[]{"add", "--password", "mypassword", safeFile.getAbsolutePath(), "-m", "-pp", "filePropertyKey", "filePropertyValue", systemFile.getAbsolutePath(), safeFolderPath};
		Bootstrap.main(args);

		/**
		 * List
		 */
		args = new String[]{"ls", "--password", "mypassword", safeFile.getAbsolutePath(), safeFolderPath};
		CommandLine.call(new Bootstrap(), System.err, args);

		args = new String[]{"ls", "--password", "mypassword", safeFile.getAbsolutePath(), safeFolderPath + Folder.DELIMITER + systemFile.getName()};
		CommandLine.call(new Bootstrap(), System.err, args);

		args = new String[]{"ls", "--password", "mypassword", safeFile.getAbsolutePath(), safeFolderPath + Folder.DELIMITER + systemFile.getParentFile().getName()};
		CommandLine.call(new Bootstrap(), System.err, args);

		/**
		 * Cat
		 */
		// cat folder
		args = new String[]{"cat", "--password", "mypassword", safeFile.getAbsolutePath(), safeFolderPath};
		Bootstrap.main(args);

		// cat non existent file
		args = new String[]{"cat", "--password", "mypassword", safeFile.getAbsolutePath(), safeFolderPath + Folder.DELIMITER + systemFile.getName() + "404"};
		Bootstrap.main(args);

		// cat the file
		args = new String[]{"cat", "--password", "mypassword", safeFile.getAbsolutePath(), safeFolderPath + Folder.DELIMITER + systemFile.getName()};
		Bootstrap.main(args);

		/**
		 * Extract
		 */
		final File extractTargetSystemFolder = new File(folder, TestUtils.randomString(random, 10) + ".extracted");

		if(!extractTargetSystemFolder.exists())
		{
			log.info("Creating folder " + extractTargetSystemFolder.getAbsolutePath());
			extractTargetSystemFolder.mkdir();
			if(!extractTargetSystemFolder.exists())
				throw new Exception("Could not create folder " + extractTargetSystemFolder.getAbsolutePath());
		}

		if(!extractTargetSystemFolder.isDirectory())
			throw new Exception("Path " + extractTargetSystemFolder.getAbsolutePath() + " is not a directory");

		final File extractTargetSystemFile = new File(extractTargetSystemFolder, systemFile.getName());
		if(extractTargetSystemFile.exists())
			throw new Exception("Extract target file " + extractTargetSystemFile.getAbsolutePath() + " already exists");

		// extract to non existent directory
		args = new String[]{"extract", "--password", "mypassword", safeFile.getAbsolutePath(), safeFolderPath + Folder.DELIMITER + systemFile.getName(), extractTargetSystemFolder.getAbsolutePath() + "404"};
		Bootstrap.main(args);

		assertTrue(!extractTargetSystemFile.exists());

		// extract to file path
		args = new String[]{"extract", "--password", "mypassword", safeFile.getAbsolutePath(), safeFolderPath + Folder.DELIMITER + systemFile.getName(), systemFile.getAbsolutePath()};
		Bootstrap.main(args);

		assertTrue(!extractTargetSystemFile.exists());

		args = new String[]{"extract", "--password", "mypassword", safeFile.getAbsolutePath(), safeFolderPath + Folder.DELIMITER + systemFile.getName(), extractTargetSystemFolder.getAbsolutePath()};
		Bootstrap.main(args);

		assertTrue(extractTargetSystemFile.exists());

		// check if file are identical
		assertArrayEquals(Files.readAllBytes(systemFile.toPath()), Files.readAllBytes(extractTargetSystemFile.toPath()));

		if(!extractTargetSystemFile.delete())
			throw new Exception("Could not delete extracted file " + extractTargetSystemFile.getAbsolutePath());

		assertTrue(!extractTargetSystemFile.exists());

		// extract again to check multiple extract works fine
		args = new String[]{"extract", "--password", "mypassword", safeFile.getAbsolutePath(), safeFolderPath + Folder.DELIMITER + systemFile.getName(), extractTargetSystemFolder.getAbsolutePath()};
		Bootstrap.main(args);

		assertTrue(extractTargetSystemFile.exists());

		if(!extractTargetSystemFile.delete())
			throw new Exception("Could not delete extracted file " + extractTargetSystemFile.getAbsolutePath());

		assertTrue(!extractTargetSystemFile.exists());

		// extract folder

		final File extractTargetSystemDirectory = new File(extractTargetSystemFolder, systemFile.getParentFile().getName());
		if(extractTargetSystemDirectory.exists())
			throw new Exception("Extract target directory " + extractTargetSystemDirectory.getAbsolutePath() + " already exists");

		args = new String[]{"extract", "--password", "mypassword", safeFile.getAbsolutePath(), safeFolderPath + Folder.DELIMITER + systemFile.getParentFile().getName(), extractTargetSystemFolder.getAbsolutePath()};
		Bootstrap.main(args);

		assertTrue(extractTargetSystemDirectory.exists());
		assertTrue(new File(extractTargetSystemDirectory, systemFile.getName()).exists());

		/**
		 * Delete
		 */
		// delete non existent file
		args = new String[]{"rm", "--password", "mypassword", "-f", safeFile.getAbsolutePath(), safeFolderPath + Folder.DELIMITER + systemFile.getName() + "404", extractTargetSystemFolder.getAbsolutePath()};
		Bootstrap.main(args);

		// delete non empty folder from safe
		args = new String[]{"rm", "--password", "mypassword", safeFile.getAbsolutePath(), safeFolderPath + Folder.DELIMITER + systemFile.getParentFile().getName()};
		Bootstrap.main(args);

		// delete non empty folder from safe (force)
		args = new String[]{"rm", "--password", "mypassword", "-f", safeFile.getAbsolutePath(), safeFolderPath + Folder.DELIMITER + systemFile.getParentFile().getName()};
		Bootstrap.main(args);

		// delete file from safe
		args = new String[]{"rm", "--password", "mypassword", "-f", safeFile.getAbsolutePath(), safeFolderPath + Folder.DELIMITER + systemFile.getName()};
		Bootstrap.main(args);

		// extract deleted file
		args = new String[]{"extract", "--password", "mypassword", safeFile.getAbsolutePath(), safeFolderPath + Folder.DELIMITER + systemFile.getName(), extractTargetSystemFolder.getAbsolutePath()};
		Bootstrap.main(args);

		/**
		 * StartGui
		 */
		if(!GraphicsEnvironment.isHeadless())
			Bootstrap.main(new String[]{});

	}

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

}
