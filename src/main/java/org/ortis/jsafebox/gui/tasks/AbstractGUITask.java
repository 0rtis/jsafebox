/*
 *  Copyright (c) 2019 by Adequate Systems, LLC. All Rights Reserved.
 *
 *  See LICENSE.PDF https://github.com/mochimodev/mochimo/blob/master/LICENSE.PDF
 *
 *  **** NO WARRANTY ****
 *
 */
package org.ortis.jsafebox.gui.tasks;

import java.util.Collection;
import java.util.function.Consumer;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * 
 * @author Ortis
 *
 */
public abstract class AbstractGUITask implements GUITask, Runnable
{
	private final String processingHeader;
	private final String successHeader;
	private final String successMessage;

	protected final Logger log;
	private Thread thread = null;


	private final LogHandler logHandler = new LogHandler();
	private final Object lock = new Object();

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
			this.log.addHandler(this.logHandler);

			task();
		} catch (final InterruptedException e)
		{
			Thread.currentThread().interrupt();
			setException(e);

		} catch (final Exception e)
		{
			setException(e);
		} finally
		{
			this.log.removeHandler(this.logHandler);
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
		synchronized (this.lock)
		{
			if (this.thread != null)
				throw new IllegalStateException("Task has already been started");

			this.logHandler.addListener(logListener);
			this.thread = new Thread(this);
			this.thread.setName("guit task");
			this.thread.start();

		}
	}

	@Override
	public boolean isTerminated()
	{
		synchronized (this.lock)
		{
			if (this.thread == null)
				throw new IllegalStateException("Task has not been started");

			return !this.thread.isAlive();

		}
	}

	@Override
	public Exception getException()
	{
		synchronized (this.lock)
		{
			return this.exception;
		}

	}

	void setException(final Exception exception)
	{
		synchronized (this.lock)
		{
			this.exception = exception;
		}
	}

	@Override
	public <D extends Collection<LogRecord>> D getLogs(final D destinations)
	{
		return this.logHandler.getLogs(destinations);
	}
}
