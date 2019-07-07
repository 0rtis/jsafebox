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
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

/**
 * 
 * @author Ortis
 *
 */
public class ListneableHandler extends Handler
{
	private final List<LogListener> listeners = new ArrayList<>();

	public ListneableHandler()
	{

	}

	@Override
	public void publish(final LogRecord record)
	{
		if (record.getLevel().intValue() >= getLevel().intValue())
			synchronized (this.listeners)
			{
				for (final LogListener listner : listeners)
					listner.onLog(record);
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

	public boolean addListener(final LogListener listener)
	{
		synchronized (this.listeners)
		{
			if (this.listeners.contains(listener))
				return false;
			return this.listeners.add(listener);
		}
	}

	public boolean removeListener(final LogListener listener)
	{
		synchronized (this.listeners)
		{
			return this.listeners.remove(listener);

		}
	}
}
