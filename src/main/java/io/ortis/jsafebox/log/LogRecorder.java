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
