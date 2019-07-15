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


import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * 
 * @author Ortis
 *
 */
public class LogServiceImpl implements LogService
{
	public final static DateTimeFormatter LOG_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
	private final Map<String, Logger> cache = new HashMap<>();

	private final ListneableHandler handler = new ListneableHandler();

	public Logger getLogger(final String name)
	{
		Logger log;
		synchronized (this.cache)
		{
			log = this.cache.get(name);
			if (log != null)
				return log;

			log = Logger.getLogger(name);
			this.cache.put(name, log);
			log.setUseParentHandlers(false);

			log.setLevel(Level.ALL);

			log.addHandler(this.handler);
			return log;
		}
	}

	public void setLevel(final Level level)
	{
		this.handler.setLevel(level);
	}

	public boolean addListener(final LogListener listener)
	{
		return this.handler.addListener(listener);
	}

	public boolean removeListener(final LogListener listener)
	{
		return this.handler.removeListener(listener);
	}

	public String format(final LogRecord record)
	{
		final LocalDateTime now = LocalDateTime.ofInstant(Instant.ofEpochMilli(record.getMillis()), TimeZone.getDefault().toZoneId());
		final String log = "[" + record.getLevel().getName() + "] " + LOG_FORMATTER.format(now) + " - " + record.getLoggerName() + "|" + Thread.currentThread().getName() + "|"
				+ record.getSourceClassName() + "." + record.getSourceMethodName() + ":  " + record.getMessage();
		return log;
	}
}
