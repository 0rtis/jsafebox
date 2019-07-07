/*
 *  Copyright (c) 2019 by Adequate Systems, LLC. All Rights Reserved.
 *
 *  See LICENSE.PDF https://github.com/mochimodev/mochimo/blob/master/LICENSE.PDF
 *
 *  **** NO WARRANTY ****
 *
 */
package io.ortis.jsafebox.log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

/**
 * 
 * @author Ortis
 *
 */
public class LogRecorder extends Handler
{
	private final List<Consumer<LogRecord>> listners = new ArrayList<>();
	private final List<LogRecord> logs = new ArrayList<>();
	private final Object lock = new Object();

	@Override
	public void publish(final LogRecord record)
	{
		synchronized (this.lock)
		{
			logs.add(record);
			for (final Consumer<LogRecord> consumer : this.listners)
				consumer.accept(record);
		}
	}

	@Override
	public void flush()
	{
	}

	@Override
	public void close() throws SecurityException
	{
	}

	public boolean addListener(final Consumer<LogRecord> listner)
	{
		synchronized (this.lock)
		{
			return this.listners.add(listner);
		}
	}

	public <D extends Collection<LogRecord>> D getLogs(final D destinations)
	{
		synchronized (this.lock)
		{
			destinations.addAll(this.logs);
		}

		return destinations;
	}
}
