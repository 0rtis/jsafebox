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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class UtilsTest
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
		assertEquals("image/jpg", Utils.getMIMEType(new File("img.jpg")));
		assertEquals("image/png", Utils.getMIMEType(new File("img.png")));
		assertEquals("text/plain", Utils.getMIMEType(new File("doc.txt")));
		assertEquals("application/pdf", Utils.getMIMEType(new File("doc.pdf")));
		assertEquals("video/x-msvideo", Utils.getMIMEType(new File("movie.avi")));
		assertEquals("video/mpeg", Utils.getMIMEType(new File("movie.mpeg")));
		assertEquals("video/mp4", Utils.getMIMEType(new File("movie.mp4")));
		assertEquals("application/octet-stream", Utils.getMIMEType(new File("img.safe")));

	}

	@Test
	public void formatExceptionTest() throws Exception
	{
		final Exception exception = new Exception("Error message");
		final String msg = Utils.formatException(exception);
		assertTrue(msg.contains(exception.getMessage()));
		assertNull(Utils.formatException(null));

	}

}
