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

package io.ortis.jsafebox;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.logging.Logger;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

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

		final SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
		final byte [] salt = new byte[16];
		random.nextBytes(salt);

		PBEKeySpec spec = new PBEKeySpec(TestUtils.randomString(random, 12).toCharArray(), salt, Safe.PBKDF2_ITERATION, 128);
		SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
		final byte [] key = skf.generateSecret(spec).getEncoded();

		final Map<String, String> header = new HashMap<>();

		header.put(Safe.ENCRYPTION_LABEL, "AES/CBC/PKCS5Padding");
		header.put(Safe.KEY_ALGO_LABEL, "AES");

		header.put(Safe.PBKDF2_SALT_LABEL, Safe.GSON.toJson(spec.getSalt()));
		header.put(Safe.PBKDF2_ITERATION_LABEL, Integer.toString(spec.getIterationCount()));

		header.put(Safe.ENCRYPTION_IV_LENGTH_LABEL, Integer.toString(salt.length));
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
			Assert.assertNotNull(safe.getPublicHeader().get(Safe.ENCRYPTION_IV_LENGTH_LABEL));
			Assert.assertNotNull(safe.getPublicHeader().get(Safe.KEY_ALGO_LABEL));
			Assert.assertNotNull(safe.getPublicHeader().get(Safe.PROTOCOL_SPEC_LABEL));

			for (final Map.Entry<String, String> entry : header.entrySet())
				assertEquals(entry.getValue(), safe.getPublicHeader().get(entry.getKey()));

			for (final Map.Entry<String, String> entry : properties.entrySet())
				assertEquals(entry.getValue(), safe.getPrivateProperties().get(entry.getKey()));

			assertEquals(0, safe.getBlocks().size());
		} finally
		{
			safe.close();
		}

	}

	@Test
	public void readWriteTest() throws Exception
	{

		final File safeFile = new File(folder, this.filePath);

		final Map<String, String> header = new HashMap<>();

		final SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
		final byte [] salt = new byte[16];
		random.nextBytes(salt);

		PBEKeySpec spec = new PBEKeySpec(TestUtils.randomString(random, 12).toCharArray(), salt, Safe.PBKDF2_ITERATION, 128);
		SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
		final byte [] key = skf.generateSecret(spec).getEncoded();

		try
		{
			final Safe safe = Safe.create(safeFile, key, header, null, 1024);
			safe.close();
			fail("Uncomplete safe's header should not be allowed");
		} catch (final Exception e)
		{

		}

		header.put(Safe.PBKDF2_SALT_LABEL, Safe.GSON.toJson(salt));
		try
		{
			final Safe safe = Safe.create(safeFile, key, header, null, 1024);
			safe.close();
			fail("Uncomplete safe's header should not be allowed");
		} catch (final Exception e)
		{

		}

		header.put(Safe.PBKDF2_ITERATION_LABEL, Safe.GSON.toJson(Safe.PBKDF2_ITERATION));
		try
		{
			final Safe safe = Safe.create(safeFile, key, header, null, 1024);
			safe.close();
			fail("Uncomplete safe's header should not be allowed");
		} catch (final Exception e)
		{

		}

		header.put(Safe.ENCRYPTION_LABEL, "AES/CBC/PKCS5Padding");
		try
		{
			final Safe safe = Safe.create(safeFile, key, header, null, 1024);
			safe.close();
			fail("Uncomplete safe's header should not be allowed");
		} catch (final Exception e)
		{

		}

		header.put(Safe.KEY_ALGO_LABEL, "AES");
		try
		{
			final Safe safe = Safe.create(safeFile, key, header, null, 1024);
			safe.close();
			fail("Uncomplete safe's header should not be allowed");
		} catch (final Exception e)
		{

		}
		
		header.put(Safe.ENCRYPTION_IV_LENGTH_LABEL, Integer.toString(salt.length));
		
		
		try (Safe safe = Safe.create(safeFile, key, header, null, 1024))
		{

			assertEquals(0, safe.getPrivateProperties().size());

			try
			{
				Safe.create(safeFile, key, header, null, 1024);
				fail("Should not allow create on existing file");
			} catch (final Exception e)
			{

			}

			final String name = TestUtils.randomString(random, 20);
			final String path = Folder.ROOT_NAME + Folder.DELIMITER + name;
			final Map<String, String> metadatas = new HashMap<>();

			metadatas.put(TestUtils.randomString(random, 5), TestUtils.randomString(random, 20));
			final ByteArrayOutputStream original = new ByteArrayOutputStream();
			final InputStream is = SafeTest.class.getResourceAsStream("/img/Gentleman.sh-600x600.png");

			int b;
			while ((b = is.read()) > -1)
				original.write(b);

			// add missing path and name
			try
			{
				safe.add(metadatas, new ByteArrayInputStream(original.toByteArray()), null);
				fail("Uncomplete metadata's block should not be allowed");
			} catch (final Exception e)
			{

			}

			metadatas.put(Block.PATH_LABEL, path);

			// add missing name
			try
			{
				safe.add(metadatas, new ByteArrayInputStream(original.toByteArray()), null);
				fail("Uncomplete metadata's block should not be allowed");
			} catch (final Exception e)
			{

			}

			metadatas.put(Block.NAME_LABEL, name);
			safe.add(metadatas, new ByteArrayInputStream(original.toByteArray()), null);

			// add existing block
			try
			{
				safe.add(metadatas, new ByteArrayInputStream(original.toByteArray()), null);
				fail("Adding existing block should not be allowed");
			} catch (final Exception e)
			{

			}

			// add missing destination
			try
			{
				final Map<String, String> metadatas2 = new HashMap<>(metadatas);
				metadatas2.put(Block.PATH_LABEL, Folder.ROOT_NAME + Folder.DELIMITER + "404" + Folder.DELIMITER + name);
				safe.add(metadatas2, new ByteArrayInputStream(original.toByteArray()), null);
				fail("Adding block without desintation should not be allowed");
			} catch (final Exception e)
			{

			}

			/**
			 * extract
			 */

			// before save
			Block block = safe.getTempBlock(path);
			Assert.assertNotNull(block);

			assertEquals(block.getName(), name);
			assertEquals(block.getComparableName(), name.toUpperCase());

			assertEquals(block.getPath(), path);
			assertEquals(block.getComparablePath(), path.toUpperCase());

			assertEquals(block.getProperties(), metadatas);

			final ByteArrayOutputStream extracted = new ByteArrayOutputStream();
			safe.extract(path, extracted);
			assertArrayEquals(original.toByteArray(), extracted.toByteArray());

			// discard
			safe.discardChanges();

			// add again

			safe.add(metadatas, new ByteArrayInputStream(original.toByteArray()), null);

			// before save
			final Block block2 = safe.getTempBlock(path);
			Assert.assertNotNull(block2);

			assertEquals(block2.getName(), name);
			assertEquals(block2.getComparableName(), name.toUpperCase());

			assertEquals(block2.getPath(), path);
			assertEquals(block2.getComparablePath(), path.toUpperCase());

			assertEquals(block2.getProperties(), metadatas);

			final ByteArrayOutputStream extracted2 = new ByteArrayOutputStream();
			safe.extract(path, extracted2);
			assertArrayEquals(original.toByteArray(), extracted2.toByteArray());

			// after saved
			try (Safe savedSafe = safe.save())
			{
				final Block savedBlock = savedSafe.getBlock(path);
				Assert.assertNotNull(savedBlock);

				assertEquals(savedBlock.getName(), name);
				assertEquals(savedBlock.getComparableName(), name.toUpperCase());

				assertEquals(savedBlock.getPath(), path);
				assertEquals(savedBlock.getComparablePath(), path.toUpperCase());

				assertEquals(savedBlock.getProperties(), metadatas);
				assertEquals(savedBlock.getProperties(), savedSafe.readMetadata(savedBlock));

				extracted.reset();
				savedSafe.extract(savedBlock, extracted);
				assertArrayEquals(original.toByteArray(), extracted.toByteArray());

				// add existing
				try
				{
					savedSafe.add(metadatas, new ByteArrayInputStream(original.toByteArray()), null);
					fail("Duplicate block should not be allowed");
				} catch (final Exception e)
				{

				}

				// add new
				final String name2 = name + "2";
				final String path2 = Folder.ROOT_NAME + Folder.DELIMITER + name2;

				metadatas.put(Block.PATH_LABEL, path2);
				metadatas.put(Block.NAME_LABEL, name2);
				savedSafe.add(metadatas, new ByteArrayInputStream(original.toByteArray()), null);
				try (final Safe lastSafe = savedSafe.save())
				{
					assertEquals(0, lastSafe.getTempBlocks().size());
					assertEquals(0, lastSafe.getDeletedBlocks().size());
					assertEquals(safeFile.getAbsolutePath(), lastSafe.getFile().getAbsolutePath());
					assertTrue(lastSafe.getTempFile().exists());

					assertNotNull(lastSafe.getBlock(path2));
					lastSafe.delete(path2);
					assertNotNull(lastSafe.getBlock(path2));// not removed until the safe is saved
					assertEquals(1, lastSafe.getDeletedBlocks().size());
				}
			}

		}

	}

}
