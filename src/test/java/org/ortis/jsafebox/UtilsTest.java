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

package org.ortis.jsafebox;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class UtilsTest
{
	private static File folder;
	private static Random random;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception
	{

		folder = TestUtils.mkdir();
		random = TestUtils.getRandom();
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
	public void openTest() throws Exception
	{
		final String safeFileName = TestUtils.randomString(random, 10) + ".safe";
		final File safeFile = new File(folder, safeFileName);

		final String password = TestUtils.randomString(random, 12);

		final SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
		final byte [] salt = new byte[16];
		random.nextBytes(salt);

		PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, Safe.PBKDF2_ITERATION, 128);
		SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
		final byte [] key = skf.generateSecret(spec).getEncoded();

		final Map<String, String> header = new HashMap<>();
		header.put(Safe.ENCRYPTION_LABEL, "AES/CBC/PKCS5Padding");
		header.put(Safe.KEY_ALGO_LABEL, "AES");

		header.put(Safe.ENCRYPTION_IV_LENGTH_LABEL, Integer.toString(16));
		header.put(Safe.PBKDF2_SALT_LABEL, Safe.GSON.toJson(salt));
		header.put(Safe.PBKDF2_ITERATION_LABEL, Integer.toString(Safe.PBKDF2_ITERATION));

		header.put(Safe.PROTOCOL_SPEC_LABEL, Safe.PROTOCOL_SPEC);
		header.put(TestUtils.randomString(random, 5), TestUtils.randomString(random, 20));
		header.put(TestUtils.randomString(random, 5), TestUtils.randomString(random, 20));
		header.put(TestUtils.randomString(random, 5), TestUtils.randomString(random, 20));

		final Map<String, String> properties = new HashMap<>();
		header.put(TestUtils.randomString(random, 5), TestUtils.randomString(random, 20));
		header.put(TestUtils.randomString(random, 5), TestUtils.randomString(random, 20));
		header.put(TestUtils.randomString(random, 5), TestUtils.randomString(random, 20));

		Safe.create(safeFile, key, header, properties, 1024).close();

		try
		{
			Safe.open(safeFile.getAbsolutePath() + "404", password.toCharArray(), 1024, null);
			fail("Opening non existant file should not be allowed");
		} catch (final Exception e)
		{

		}

		try (final Safe safe = Safe.open(safeFile.getAbsolutePath(), password.toCharArray(), 1024, null))
		{
			assertNotNull(safe);
		}

	}

	@Test
	public void passwordTest() throws Exception
	{

		final String password = "password";
		final byte [] extractedBytes = Utils.passwordToBytes(password.toCharArray());
		final String extractedPassword = new String(extractedBytes);

		assertEquals(password, extractedPassword);

	}

	@Test
	public void parseSystemPathTest() throws Exception
	{
		Utils.parseSystemPath(".", new ArrayList<>());
		Utils.parseSystemPath("./*", new ArrayList<>());
	}

	@Test
	public void mimeTypeTest() throws Exception
	{
		assertEquals("text/plain", Utils.getMIMEType(new File("doc.txt")));
		assertEquals("text/csv", Utils.getMIMEType(new File("doc.csv")));
		assertEquals("text/html", Utils.getMIMEType(new File("webpage.html")));
		assertEquals("application/pdf", Utils.getMIMEType(new File("doc.pdf")));
		assertEquals("image/jpg", Utils.getMIMEType(new File("img.jpg")));
		assertEquals("image/png", Utils.getMIMEType(new File("img.png")));
		assertEquals("image/bmp", Utils.getMIMEType(new File("img.bmp")));
		assertEquals("video/x-msvideo", Utils.getMIMEType(new File("movie.avi")));
		assertEquals("video/mpeg", Utils.getMIMEType(new File("movie.mpeg")));
		assertEquals("video/mp4", Utils.getMIMEType(new File("movie.mp4")));
		assertEquals("video/x-matroska", Utils.getMIMEType(new File("movie.mkv")));
		assertEquals("audio/mpeg", Utils.getMIMEType(new File("audio.mp3")));
		assertEquals("application/octet-stream", Utils.getMIMEType(new File("img.safe")));

	}

	@Test
	public void formatExceptionTest() throws Exception
	{
		final Exception exception = new Exception("Error message");
		exception.initCause(new Exception("Base exception"));
		final String msg = Utils.formatException(exception);
		assertTrue(msg.contains(exception.getMessage()));
		assertNull(Utils.formatException(null));

	}

	@Test
	public void sanitizeTokenTest() throws Exception
	{
		final String token = "before" + File.separator + "after";
		final char substitute = '_';
		final String sanitized = Utils.sanitizeToken(token, substitute);
		assertEquals("before" + substitute + "after", sanitized);

	}

	@Test
	public void headlessTest() throws Exception
	{
		Utils.isHeadless();

	}

}
