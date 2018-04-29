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
package org.ortis.jsafe;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class SafeFilesTest
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
	public void test() throws Exception
	{

		final Folder root = new Folder(null, Folder.ROOT_NAME);

		// check mkdir
		SafeFiles.mkdir(root.getName() + Folder.DELIMITER + "1", false, root, root);

		SafeFile safeFile = root.get("1");
		assertNotNull(safeFile);
		assertTrue(safeFile.isFolder());
		Folder folder = (Folder) safeFile;
		assertEquals(0, folder.listFiles().size());

		// creating root/1/11 with a block path argument
		SafeFiles.mkdir(folder.getPath() + Folder.DELIMITER + "11" + Folder.DELIMITER + "block", true, root, root);
		safeFile = folder.get("11");
		assertNotNull(safeFile);
		assertEquals(folder.getPath() + Folder.DELIMITER + "11", safeFile.getPath());
		assertEquals(safeFile.getName(), SafeFiles.getName(safeFile.getPath()));
		assertTrue(safeFile.isFolder());
		assertEquals(0, ((Folder) safeFile).listFiles().size());

		assertEquals(1, folder.listFiles().size());

		// creating root/1/12 from within root/1
		SafeFiles.mkdir("12", false, folder, root);
		safeFile = folder.get("12");
		assertNotNull(safeFile);
		assertEquals(folder.getPath() + Folder.DELIMITER + "12", safeFile.getPath());
		assertEquals(safeFile.getName(), SafeFiles.getName(safeFile.getPath()));
		assertTrue(safeFile.isFolder());
		assertEquals(0, ((Folder) safeFile).listFiles().size());

		// creating root/1/13 from within root/1 and with a block path argument
		SafeFiles.mkdir("13" + Folder.DELIMITER + "block", true, folder, root);
		safeFile = folder.get("13");
		assertNotNull(safeFile);
		assertEquals(folder.getPath() + Folder.DELIMITER + "13", safeFile.getPath());
		assertEquals(safeFile.getName(), SafeFiles.getName(safeFile.getPath()));
		assertTrue(safeFile.isFolder());
		assertEquals(0, ((Folder) safeFile).listFiles().size());

	
		// creating additional directory for testing
		folder = root.mkdir("1b");
		folder.mkdir("11");
		folder.mkdir("12");
		folder.mkdir("13");

		folder = root.mkdir("2");
		folder.mkdir("21");
		folder.mkdir("22");
		folder.mkdir("23");

		folder = root.mkdir("3");
		folder.mkdir("31");
		folder.mkdir("32");
		folder.mkdir("33");

		// SafeFiles.get
		safeFile = SafeFiles.get("1", root, root);
		assertNotNull(safeFile);
		assertEquals("1", safeFile.getName());

		safeFile = SafeFiles.get("." + Folder.DELIMITER + "11", (Folder) safeFile, root);// relative path
		assertNotNull(safeFile);
		assertEquals("11", safeFile.getName());

		safeFile = SafeFiles.get("1" + Folder.DELIMITER + "11", root, root);
		assertNotNull(safeFile);
		assertEquals("11", safeFile.getName());

		// SafeFiles.match

		// root/*
		List<SafeFile> matches = SafeFiles.match(root.getName() + Folder.DELIMITER + Folder.WILDCARD, root, root, new ArrayList<>());
		assertEquals(4, matches.size());
		contains(root.getName() + Folder.DELIMITER + "1", matches);
		contains(root.getName() + Folder.DELIMITER + "1b", matches);
		contains(root.getName() + Folder.DELIMITER + "2", matches);
		contains(root.getName() + Folder.DELIMITER + "3", matches);

		// root/1/*
		matches = SafeFiles.match(root.getName() + Folder.DELIMITER + "1" + Folder.DELIMITER + Folder.WILDCARD, root, root, new ArrayList<>());
		assertEquals(3, matches.size());
		contains(root.getName() + Folder.DELIMITER + "1" + Folder.DELIMITER + "11", matches);
		contains(root.getName() + Folder.DELIMITER + "1" + Folder.DELIMITER + "12", matches);
		contains(root.getName() + Folder.DELIMITER + "1" + Folder.DELIMITER + "13", matches);

		// root/*/*2
		matches = SafeFiles.match(root.getName() + Folder.DELIMITER + Folder.WILDCARD + Folder.DELIMITER + Folder.WILDCARD + "2", root, root, new ArrayList<>());
		assertEquals(4, matches.size());
		contains(root.getName() + Folder.DELIMITER + "1" + Folder.DELIMITER + "12", matches);
		contains(root.getName() + Folder.DELIMITER + "1b" + Folder.DELIMITER + "12", matches);
		contains(root.getName() + Folder.DELIMITER + "2" + Folder.DELIMITER + "22", matches);
		contains(root.getName() + Folder.DELIMITER + "3" + Folder.DELIMITER + "32", matches);

		// root/1*/*
		matches = SafeFiles.match(root.getName() + Folder.DELIMITER + "1" + Folder.WILDCARD + Folder.DELIMITER + Folder.WILDCARD, root, root, new ArrayList<>());
		assertEquals(6, matches.size());
		contains(root.getName() + Folder.DELIMITER + "1" + Folder.DELIMITER + "11", matches);
		contains(root.getName() + Folder.DELIMITER + "1" + Folder.DELIMITER + "12", matches);
		contains(root.getName() + Folder.DELIMITER + "1" + Folder.DELIMITER + "13", matches);
		contains(root.getName() + Folder.DELIMITER + "1b" + Folder.DELIMITER + "11", matches);
		contains(root.getName() + Folder.DELIMITER + "1b" + Folder.DELIMITER + "12", matches);
		contains(root.getName() + Folder.DELIMITER + "1b" + Folder.DELIMITER + "13", matches);

		// root/1*/*2
		matches = SafeFiles.match(root.getName() + Folder.DELIMITER + "1" + Folder.WILDCARD + Folder.DELIMITER + Folder.WILDCARD + "2", root, root, new ArrayList<>());
		assertEquals(2, matches.size());
		contains(root.getName() + Folder.DELIMITER + "1" + Folder.DELIMITER + "12", matches);
		contains(root.getName() + Folder.DELIMITER + "1b" + Folder.DELIMITER + "12", matches);

	}

	private void contains(final String path, List<SafeFile> candidates)
	{
		boolean found = false;

		for (final SafeFile sf : candidates)
			if (sf.getPath().equals(path))
			{
				found = true;
			}

		assertTrue("Path " + path + " not found", found);

	}

}
