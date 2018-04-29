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
import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.logging.Logger;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class SafeTest
{

	private static Logger log;
	private static File folder;
	private static Random random;
	private String filePath;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception
	{
		log = TestUtils.getLog();
		random = TestUtils.getRandom();
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

		for (int i = 0; i < 100; i++)
		{
			filePath = TestUtils.randomString(random, 10) + ".jsafe";

			final File file = new File(folder, this.filePath);
			if (!file.exists())
				break;

		}

		final File file = new File(this.filePath);
		if (file.exists())
			throw new Exception("Could not find non existing file");
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception
	{
		final File file = new File(this.filePath);
		if (file.exists())
		{
			log.fine("Deleting file " + file);
			file.delete();
		}

	}

	@Test
	public void initTest() throws Exception
	{

		final File safeFile = new File(folder, this.filePath);

		final MessageDigest md = MessageDigest.getInstance("SHA-256");
		final byte [] key = Arrays.copyOf(md.digest(md.digest(TestUtils.randomString(random, 12).getBytes())), 128 >> 3);

		final Map<String, String> header = new HashMap<>();
		header.put(Safe.ENCRYPTION_LABEL, "AES/CBC/PKCS5Padding");
		header.put(Safe.KEY_ALGO_LABEL, "AES");
		final SecureRandom random = new SecureRandom();
		final byte [] iv = new byte[16];
		random.nextBytes(iv);
		header.put(Safe.ENCRYPTION_IV_LABEL, Safe.GSON.toJson(iv));
		header.put(Safe.PROTOCOL_SPEC_LABEL, Safe.PROTOCOL_SPEC);
		header.put(TestUtils.randomString(random, 5), TestUtils.randomString(random, 20));
		header.put(TestUtils.randomString(random, 5), TestUtils.randomString(random, 20));
		header.put(TestUtils.randomString(random, 5), TestUtils.randomString(random, 20));

		final Map<String, String> properties = new HashMap<>();
		header.put(TestUtils.randomString(random, 5), TestUtils.randomString(random, 20));
		header.put(TestUtils.randomString(random, 5), TestUtils.randomString(random, 20));
		header.put(TestUtils.randomString(random, 5), TestUtils.randomString(random, 20));

		final Safe safe = Safe.create(safeFile, key, header, properties, 1024);
		try
		{
			Assert.assertNotNull(safe.getPublicHeader().get(Safe.ENCRYPTION_LABEL));
			Assert.assertNotNull(safe.getPublicHeader().get(Safe.ENCRYPTION_LABEL));
			Assert.assertNotNull(safe.getPublicHeader().get(Safe.ENCRYPTION_IV_LABEL));
			Assert.assertNotNull(safe.getPublicHeader().get(Safe.KEY_ALGO_LABEL));
			Assert.assertNotNull(safe.getPublicHeader().get(Safe.PROTOCOL_SPEC_LABEL));

			for (final Map.Entry<String, String> entry : header.entrySet())
				Assert.assertEquals(entry.getValue(), safe.getPublicHeader().get(entry.getKey()));

			for (final Map.Entry<String, String> entry : properties.entrySet())
				Assert.assertEquals(entry.getValue(), safe.getPrivateProperties().get(entry.getKey()));

			Assert.assertEquals(0, safe.getBlocks().size());
		} finally
		{
			safe.close();
		}

	}

	@Test
	public void readWriteTest() throws Exception
	{

		final File safeFile = new File(folder, this.filePath);
		final MessageDigest md = MessageDigest.getInstance("SHA-256");
		final byte [] key = Arrays.copyOf(md.digest(md.digest(TestUtils.randomString(random, 12).getBytes())), 128 >> 3);

		final Map<String, String> header = new HashMap<>();
		header.put(Safe.ENCRYPTION_LABEL, "AES/CBC/PKCS5Padding");
		header.put(Safe.KEY_ALGO_LABEL, "AES");
		final SecureRandom random = new SecureRandom();
		final byte [] iv = new byte[16];
		random.nextBytes(iv);
		header.put(Safe.ENCRYPTION_IV_LABEL, Safe.GSON.toJson(iv));

		Safe safe = Safe.create(safeFile, key, header, null, 1024);
		try
		{
			final String name = TestUtils.randomString(random, 20);
			final String path = Folder.ROOT_NAME + Folder.DELIMITER + name;
			final Map<String, String> metadatas = new HashMap<>();
			metadatas.put(Block.PATH_LABEL, path);
			metadatas.put(Block.NAME_LABEL, name);
			metadatas.put(TestUtils.randomString(random, 5), TestUtils.randomString(random, 20));
			final ByteArrayOutputStream original = new ByteArrayOutputStream();
			final InputStream is = SafeTest.class.getResourceAsStream("/img/Gentleman.sh-600x600.png");

			int b;
			while ((b = is.read()) > -1)
				original.write(b);

			safe.add(metadatas, new ByteArrayInputStream(original.toByteArray()));

			// extract

			// before save
			final Block block = safe.getTempBlock(path);
			Assert.assertNotNull(block);

			assertEquals(block.getName(), name);
			assertEquals(block.getComparableName(), name.toUpperCase());

			assertEquals(block.getPath(), path);
			assertEquals(block.getComparablePath(), path.toUpperCase());

			assertEquals(block.getProperties(), metadatas);

			// assertEquals(block.getDataLength(), original.size());

			final ByteArrayOutputStream extracted = new ByteArrayOutputStream();
			safe.extract(path, extracted);
			assertArrayEquals(original.toByteArray(), extracted.toByteArray());

			// after saved
			safe = safe.save();
			final Block savedBlock = safe.getBlock(path);
			Assert.assertNotNull(savedBlock);

			assertEquals(savedBlock.getName(), name);
			assertEquals(savedBlock.getComparableName(), name.toUpperCase());

			assertEquals(savedBlock.getPath(), path);
			assertEquals(savedBlock.getComparablePath(), path.toUpperCase());

			assertEquals(savedBlock.getProperties(), metadatas);

			// assertEquals(savedBlock.getDataLength(), original.size());

			extracted.reset();
			safe.extract(path, extracted);
			assertArrayEquals(original.toByteArray(), extracted.toByteArray());

		} finally
		{
			safe.close();
		}

	}

}
