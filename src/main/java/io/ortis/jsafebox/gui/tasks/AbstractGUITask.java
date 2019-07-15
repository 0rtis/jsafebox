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

package io.ortis.jsafebox.gui.tasks;

import io.ortis.jsafebox.log.LogRecorder;

import java.util.Collection;
import java.util.function.Consumer;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * @author Ortis
 */
public abstract class AbstractGUITask implements GUITask, Runnable
{
	protected final Logger log;
	private final String processingHeader;
	private final String successHeader;
	private final String successMessage;
	private final LogRecorder logRecorder = new LogRecorder();
	private final Object lock = new Object();
	private Thread thread = null;
	private Exception exception;

	public AbstractGUITask(final String processingHeader, final String successHeader, final String successMessage, final Logger log)
	{
		this.processingHeader = processingHeader;
		this.successHeader = successHeader;
		this.successMessage = successMessage;
		this.log = log;
	}

	@Override
	public void run()
	{
		try
		{
			// attach log hadler
			this.log.addHandler(this.logRecorder);

			task();
		} catch(final InterruptedException e)
		{
			Thread.currentThread().interrupt();
			setException(e);

		} catch(final Exception e)
		{
			setException(e);
		} finally
		{
			this.log.removeHandler(this.logRecorder);
		}
	}

	@Override
	public String getProcessingHeader()
	{
		return this.processingHeader;
	}

	@Override
	public String getSuccessHeader()
	{
		return this.successHeader;
	}

	@Override
	public String getSuccessMessage()
	{
		return this.successMessage;
	}

	@Override
	public void start(final Consumer<LogRecord> logListener)
	{
		synchronized(this.lock)
		{
			if(this.thread != null)
				throw new IllegalStateException("Task has already been started");

			this.logRecorder.addListener(logListener);
			this.thread = new Thread(this);
			this.thread.setName("gui task");
			this.thread.start();
		}
	}

	@Override
	public boolean isTerminated()
	{
		synchronized(this.lock)
		{
			if(this.thread == null)
				throw new IllegalStateException("Task has not been started");

			return !this.thread.isAlive();

		}
	}

	@Override
	public Exception getException()
	{
		synchronized(this.lock)
		{
			return this.exception;
		}

	}

	void setException(final Exception exception)
	{
		synchronized(this.lock)
		{
			this.exception = exception;
		}
	}


	@Override
	public <D extends Collection<LogRecord>> D getLogs(final D destinations)
	{
		return this.logRecorder.getLogs(destinations);
	}
}
