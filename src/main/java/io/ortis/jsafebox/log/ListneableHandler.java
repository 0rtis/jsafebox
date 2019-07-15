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
