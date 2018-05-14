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

package org.ortis.jsafebox.task;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.ortis.jsafebox.task.Task;
import org.ortis.jsafebox.task.TaskListener;
import org.ortis.jsafebox.task.TaskProbeAdapter;

public class TaskProbeAdapterTest
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
		final TaskProbeAdapter adapter = new TaskProbeAdapter();
		assertEquals(0, adapter.getProgress(), .0000000001);
		assertNull(adapter.getMessage());
		assertNull(adapter.getException());
		assertFalse(adapter.isCancelled());
		assertFalse(adapter.isCancelRequested());
		assertFalse(adapter.isTerminated());
		assertFalse(adapter.isCompleted());

		final boolean [] terminated = new boolean[] { false };
		final boolean [] cancelled = new boolean[] { false };
		final boolean [] cancelRequested = new boolean[] { false };
		final double [] progress = new double[] { -1 };
		final String [] message = new String[] { null };
		final Exception [] exception = new Exception[] { null };

		final TaskListener listner = new TaskListener()
		{

			@Override
			public void onTerminated(Task task)
			{
				terminated[0] = true;
			}

			@Override
			public void onProgress(Task task, double p)
			{
				progress[0] = p;
			}

			@Override
			public void onMessage(Task task, String msg)
			{
				message[0] = msg;
			}

			@Override
			public void onException(Task task, Exception ex)
			{
				exception[0] = ex;
			}

			@Override
			public void onCancelled(Task task)
			{
				cancelled[0] = true;
			}

			@Override
			public void onCancellationRequested(Task task)
			{
				cancelRequested[0] = true;
			}
		};

		assertTrue(adapter.addListener(listner));

		final boolean [] notified = new boolean[] { false };
		Thread thread = new Thread(new Runnable()
		{

			@Override
			public void run()
			{

				try
				{
					synchronized (notified)
					{
						notified.notifyAll();
					}

					adapter.awaitUpdate();
					notified[0] = true;
				} catch (Exception e)
				{
					fail(e.getMessage());
				}
			}
		});

		synchronized (notified)
		{
			thread.start();
			notified.wait();
			Thread.sleep(1000);
		}

		adapter.fireProgress(1);
		thread.join(1000);

		assertEquals(1, adapter.getProgress(), .0000000001);
		assertEquals(progress[0], adapter.getProgress(), .0000000001);

		notified[0] = false;
		thread = new Thread(new Runnable()
		{

			@Override
			public void run()
			{

				try
				{
					synchronized (notified)
					{
						notified.notifyAll();
					}

					adapter.awaitUpdate(1000, TimeUnit.DAYS);

					notified[0] = true;
				} catch (Exception e)
				{
					fail(e.getMessage());
				}
			}
		});

		synchronized (notified)
		{
			thread.start();
			notified.wait();
			Thread.sleep(1000);
		}

		adapter.fireMessage("msg");
		thread.join(1000);

		assertEquals("msg", adapter.getMessage());
		assertEquals(message[0], adapter.getMessage());

		final Exception e = new Exception();
		adapter.fireException(e);
		assertEquals(e, adapter.getException());
		assertEquals(exception[0], adapter.getException());

		notified[0] = false;
		thread = new Thread(new Runnable()
		{

			@Override
			public void run()
			{

				try
				{
					synchronized (notified)
					{
						notified.notifyAll();
					}

					adapter.awaitTermination();
					adapter.awaitTermination(1000, TimeUnit.DAYS);

					notified[0] = true;
				} catch (Exception e)
				{
					fail(e.getMessage());
				}
			}
		});

		synchronized (notified)
		{
			thread.start();
			notified.wait();
			Thread.sleep(1000);
		}

		adapter.fireTerminated();
		thread.join(1000);

		assertTrue(adapter.isTerminated());
		assertTrue(terminated[0]);
		assertFalse(adapter.isCompleted());

		adapter.cancel();
		assertTrue(adapter.isCancelRequested());
		assertTrue(cancelRequested[0]);
		assertFalse(adapter.isCancelled());
		assertFalse(cancelled[0]);

		adapter.fireCanceled();
		assertTrue(adapter.isCancelled());
		assertTrue(cancelled[0]);

		assertFalse(adapter.isCompleted());

		assertTrue(adapter.removeListener(listner));

	}

}
