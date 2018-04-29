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

import java.util.ArrayList;
import java.util.Random;

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
		final byte [] bytes = new byte[404];
		new Random().nextBytes(bytes);
		final String password = new String(bytes);
		final byte [] extractedBytes = Utils.passwordToBytes(password.toCharArray());
		final String extractedPassword = new String(extractedBytes);
		
		assertEquals(password, extractedPassword);
		
		

	}


	@Test
	public void parseSystemPathTest() throws Exception
	{
		Utils.parseSystemPath(".", new ArrayList<>());
	}
	
	
	@Test
	public void formatExceptionTest() throws Exception
	{
		final Exception exception = new Exception("Error message");
		final String msg = Utils.formatException(exception);
		assertTrue(msg.contains(exception.getMessage()));
		
	}
	

}
