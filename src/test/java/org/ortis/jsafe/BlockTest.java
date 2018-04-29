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
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class BlockTest
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

		final String name = "block";
		final String path = "path" + Folder.DELIMITER + "to" + Folder.DELIMITER + "the" + Folder.DELIMITER + name;

		final Map<String, String> properties = new HashMap<>();
		properties.put("property name 1", "property value 1");
		properties.put("property name 2", "property value 2");
		properties.put("property name 3", "property value 3");

		final long metaOffset = 404;
		final long metaLength = 123;

		final long dataOffset = metaOffset + metaLength;
		final long dataLength = 12345;

		final long offset = metaOffset;
		final long length = metaLength + dataLength;

		final Block block = new Block(path, properties, offset, length, metaOffset, metaLength, dataOffset, dataLength);

		assertTrue(block.isBlock());
		assertTrue(!block.isFolder());

		assertEquals(block, block);

		assertEquals(block.getName(), name);
		assertEquals(block.getComparableName(), name.toUpperCase());

		assertEquals(block.getPath(), path);
		assertEquals(block.getComparablePath(), path.toUpperCase());

		assertEquals(block.getMetaOffset(), metaOffset);
		assertEquals(block.getMetaLength(), metaLength);

		assertEquals(block.getDataOffset(), dataOffset);
		assertEquals(block.getDataLength(), dataLength);

		assertEquals(block.getOffset(), offset);
		assertEquals(block.getLength(), length);

		for (final String key : properties.keySet())
			assertEquals(block.getProperties().get(key), properties.get(key));

		final Block clone = new Block(path, properties, offset, length, metaOffset, metaLength, dataOffset, dataLength);
		assertTrue(block.equals(clone));

		final Block block2 = new Block("a" + Folder.DELIMITER + "different" + Folder.DELIMITER + "path", properties, offset, length, metaOffset, metaLength, dataOffset, dataLength);
		assertTrue(!block.equals(block2));

		try
		{

			new Block("a", properties, offset, length, metaOffset, metaLength, dataOffset, dataLength);
			fail("Invalid block path should throw exception");
		} catch (final Exception e)
		{

		}
	}

}
