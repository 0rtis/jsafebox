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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class FolderTest
{

	@BeforeClass
	public static void setUpBeforeClass() throws Exception
	{

	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception
	{

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
	public void mkdirTest() throws Exception
	{
		final String rootName = Folder.ROOT_NAME;
		final Folder root = new Folder(null, rootName);
		assertEquals(rootName, root.getName());
		assertEquals(rootName.toUpperCase(), root.getComparableName());
		assertEquals(rootName, root.getPath());
		assertEquals(rootName.toUpperCase(), root.getComparablePath());
		assertEquals(0, root.listFiles().size());

		final String folderName = "folder";
		final String folderPath = rootName + Folder.DELIMITER + folderName;
		Folder folder = root.mkdir(folderName);
		assertEquals(folderPath, folder.getPath());
		assertTrue(folder.isFolder());
		assertTrue(!folder.isBlock());

		try
		{
			root.mkdir(folderName);
			fail("Duplicate folder should not be allowed");
		} catch (final Exception e)
		{

		}

		assertEquals(1, root.listFiles().size());
		assertEquals(folder, root.listFiles().get(0));

		final String blockPath =  "path" + Folder.DELIMITER + "to" + Folder.DELIMITER + "block" + Folder.DELIMITER + "block name";

		root.mkdir(blockPath, true);
		assertEquals(2, root.listFiles().size());

		// check folders
		SafeFile safeFile = root.getChild("path");
		assertNotNull(safeFile);
		assertTrue(safeFile.isFolder());
		folder = (Folder) safeFile;
		assertTrue(folder.getName().equals("path"));

		safeFile = folder.getChild("to");
		assertNotNull(safeFile);
		assertTrue(safeFile.isFolder());
		folder = (Folder) safeFile;
		assertTrue(folder.getName().equals("to"));

		safeFile = folder.getChild("block");
		assertNotNull(safeFile);
		assertTrue(safeFile.isFolder());
		folder = (Folder) safeFile;
		assertTrue(folder.getName().equals("block"));

		safeFile = folder.getChild("block name");
		assertNull(safeFile);

		// not equals test
		assertTrue(!folder.equals(folder.getParent()));

		final String notBlockPath = "path" + Folder.DELIMITER + "to" + Folder.DELIMITER + "nothing";

		root.mkdir(notBlockPath, false);
		assertEquals(2, root.listFiles().size());
		// check folders
		safeFile = root.getChild("path");
		assertNotNull(safeFile);
		assertTrue(safeFile.isFolder());
		folder = (Folder) safeFile;
		assertTrue(folder.getName().equals("path"));

		safeFile = folder.getChild("to");
		assertNotNull(safeFile);
		assertTrue(safeFile.isFolder());
		folder = (Folder) safeFile;
		assertTrue(folder.getName().equals("to"));

		safeFile = folder.getChild("nothing");
		assertNotNull(safeFile);
		assertTrue(safeFile.isFolder());
		folder = (Folder) safeFile;
		assertTrue(folder.getName().equals("nothing"));

		assertEquals(0, folder.listFiles().size());

		assertNull(folder.getChild(""));
		folder.toString();
		assertNull(folder.get(new String[0], 0, 0));
	}

	@Test
	public void blockTest() throws Exception
	{
		final String rootName = "";
		final Folder root = new Folder(null, rootName);
		assertEquals(rootName, root.getName());
		assertEquals(rootName.toUpperCase(), root.getComparableName());
		assertEquals(rootName, root.getPath());
		assertEquals(rootName.toUpperCase(), root.getComparablePath());
		assertEquals(0, root.listFiles().size());

		String path = rootName + Folder.DELIMITER + "block name";

		final Map<String, String> properties = new HashMap<>();

		Block block = new Block(path, properties, 0, 0, 0, 0, 0, 0, root);

		root.add(block);

		assertEquals(1, root.listFiles().size());
		assertEquals(block, root.listFiles().get(0));

		try
		{
			root.add(block);

			fail("Duplicate block should not be allowed");
		} catch (final Exception e)
		{

		}

		try
		{
			path = rootName + Folder.DELIMITER + "non existant folder" + Folder.DELIMITER + "block name";

			block = new Block(path, properties, 0, 0, 0, 0, 0, 0, root);

			root.add(block);

			fail("Non matching block path should not be allowed");
		} catch (final Exception e)
		{

		}

	}

}
