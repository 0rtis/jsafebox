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

import java.util.logging.Logger;

/**
 * @author Ortis
 */
public class ExceptionTask extends AbstractGUITask
{
	private final Exception exception;

	public ExceptionTask(final Exception exception, final Logger log)
	{
		super("", "", "", log);

		this.exception = exception;
		setException(this.exception);
	}

	@Override
	public boolean skipResultOnSuccess()
	{
		return false;
	}

	@Override
	public void task() throws Exception
	{
		throw this.exception;
	}
}
