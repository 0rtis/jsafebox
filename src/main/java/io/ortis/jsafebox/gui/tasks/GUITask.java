
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

import java.util.Collection;
import java.util.function.Consumer;
import java.util.logging.LogRecord;

/**
 * @author Ortis
 */
public interface GUITask
{
	void task() throws Exception;

	String getProcessingHeader();

	default boolean skipResultOnSuccess()
	{
		return false;

	}

	default long getResultTimer()
	{
		return 1500;
	}

	String getSuccessHeader();

	String getSuccessMessage();

	void start(final Consumer<LogRecord> logListener);

	boolean isTerminated();

	Exception getException();

	<D extends Collection<LogRecord>> D getLogs(final D destination);
}
