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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
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

		SafeFile safeFile = root.getChild("1");
		assertNotNull(safeFile);
		assertTrue(safeFile.isFolder());
		Folder folder = (Folder) safeFile;
		assertEquals(0, folder.listFiles().size());

		// creating root/1/11 with a block path argument
		SafeFiles.mkdir(folder.getPath() + Folder.DELIMITER + "11" + Folder.DELIMITER + "block", true, root, root);
		safeFile = folder.getChild("11");
		assertNotNull(safeFile);
		assertEquals(folder.getPath() + Folder.DELIMITER + "11", safeFile.getPath());
		assertEquals(safeFile.getName(), SafeFiles.getName(safeFile.getPath()));
		assertTrue(safeFile.isFolder());
		assertEquals(0, ((Folder) safeFile).listFiles().size());

		assertEquals(1, folder.listFiles().size());

		// creating root/1/12 from within root/1
		SafeFiles.mkdir("12", false, folder, root);
		safeFile = folder.getChild("12");
		assertNotNull(safeFile);
		assertEquals(folder.getPath() + Folder.DELIMITER + "12", safeFile.getPath());
		assertEquals(safeFile.getName(), SafeFiles.getName(safeFile.getPath()));
		assertTrue(safeFile.isFolder());
		assertEquals(0, ((Folder) safeFile).listFiles().size());

		// creating root/1/13 from within root/1 and with a block path argument
		SafeFiles.mkdir("13" + Folder.DELIMITER + "block", true, folder, root);
		safeFile = folder.getChild("13");
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

		{// test all code paths
			safeFile = SafeFiles.get("", root, root);
			assertNull(safeFile);

			safeFile = SafeFiles.get(".", folder, root);
			assertEquals(folder, safeFile);

			safeFile = SafeFiles.get("..", folder, root);
			assertEquals(root, safeFile);

			safeFile = SafeFiles.get("1", root, root);
			assertNotNull(safeFile);
			assertEquals("1", safeFile.getName());

			safeFile = SafeFiles.get(Character.toString(Folder.DELIMITER), root, root);
			assertNotNull(safeFile);
			assertEquals(root, safeFile);
		}

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

		{// test all code paths
			matches = SafeFiles.match("", root, root, new ArrayList<>());
			assertEquals(0, matches.size());

			matches = SafeFiles.match(".", folder, root, new ArrayList<>());
			assertEquals(1, matches.size());
			assertEquals(folder, matches.get(0));

			matches = SafeFiles.match("." + Folder.DELIMITER + folder.getName(), root, root, new ArrayList<>());
			assertEquals(1, matches.size());
			assertEquals(folder, matches.get(0));

			matches = SafeFiles.match("." + Folder.DELIMITER + Folder.WILDCARD, folder, root, new ArrayList<>());
			assertEquals(3, matches.size());

			matches = SafeFiles.match("..", folder, root, new ArrayList<>());
			assertEquals(1, matches.size());
			assertEquals(root, matches.get(0));

			matches = SafeFiles.match("../*", folder, root, new ArrayList<>());
			assertEquals(4, matches.size());

			matches = SafeFiles.match(Folder.WILDCARD, root, root, new ArrayList<>());
			assertEquals(4, matches.size());
			contains(root.getName() + Folder.DELIMITER + "1", matches);
			contains(root.getName() + Folder.DELIMITER + "1b", matches);
			contains(root.getName() + Folder.DELIMITER + "2", matches);
			contains(root.getName() + Folder.DELIMITER + "3", matches);
		}

		{// test all code path

			SafeFiles.mkdir("." + Folder.DELIMITER + "final folder", false, root, root);
			safeFile = root.getChild("final folder");
			assertNotNull(safeFile);
		}

		// sanitize
		final StringBuilder sb = new StringBuilder();
		for (final char c : Environment.getForbidenChars())
			sb.append(Character.toString(c) + Folder.DELIMITER);

		String sanitized = Utils.sanitize(sb.toString(), Folder.DELIMITER, null);
		for (final Character c : sanitized.toCharArray())
			assertTrue(c.equals(Folder.DELIMITER));

		sanitized = Utils.sanitize(sb.toString(), Folder.DELIMITER, '#');

		for (final Character c : sanitized.toCharArray())
			assertTrue(c.equals('#') || c.equals(Folder.DELIMITER));

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
