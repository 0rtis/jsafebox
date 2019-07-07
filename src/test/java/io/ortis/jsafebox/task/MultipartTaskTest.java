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

package io.ortis.jsafebox.task;

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

public class MultipartTaskTest
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
		final MultipartTask task = new MultipartTask()
		{

		};

		final MultipartTask subTask = new MultipartTask()
		{

		};

		assertEquals(0, task.getProgress(), .0000000001);
		assertNull(task.getMessage());
		assertNull(task.getException());
		assertFalse(task.isCancelled());
		assertFalse(task.isCancelRequested());
		assertFalse(task.isTerminated());
		assertFalse(task.isCompleted());

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

		assertTrue(task.addListener(listner));

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

					task.awaitUpdate();
					notified[0] = true;
				} catch (Exception e)
				{
					fail(e.getMessage());
				}
			}
		});

		task.setSilentSubTask(false);
		task.setSubTask(subTask);

		synchronized (notified)
		{
			thread.start();
			notified.wait();
			Thread.sleep(1000);
		}

		subTask.fireProgress(1);

		thread.join(1000);

		assertTrue(notified[0]);

		assertEquals(1, task.getProgress(), .0000000001);
		assertEquals(progress[0], task.getProgress(), .0000000001);

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

					task.awaitUpdate(1000, TimeUnit.DAYS);

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

		subTask.fireMessage("msg");

		thread.join(1000);

		assertTrue(notified[0]);

		assertEquals("msg", task.getMessage());
		assertEquals(message[0], task.getMessage());

		final Exception e = new Exception();
		subTask.fireException(e);
		assertEquals(e, task.getException());
		assertEquals(exception[0], task.getException());

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

					task.awaitTermination();
					task.awaitTermination(1000, TimeUnit.DAYS);

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

		task.fireTerminated();// subtask termination does not mean the parent task is terminated
		thread.join(1000);

		assertTrue(notified[0]);

		assertTrue(task.isTerminated());
		assertTrue(terminated[0]);
		assertFalse(task.isCompleted());

		task.cancel();
		assertTrue(task.isCancelRequested());
		assertTrue(cancelRequested[0]);
		assertFalse(task.isCancelled());
		assertFalse(cancelled[0]);

		subTask.fireCanceled();
		assertTrue(task.isCancelled());
		assertTrue(cancelled[0]);

		assertFalse(task.isCompleted());

		assertTrue(task.removeListener(listner));

	}

}
