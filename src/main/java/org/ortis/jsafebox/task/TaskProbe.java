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

package org.ortis.jsafebox.task;

import java.util.concurrent.CancellationException;

/**
 * A probe to monitor status
 * 
 * @author Ortis <br>
 *         2018 May 05 2:50:18 PM <br>
 */
public interface TaskProbe
{

	public final static TaskProbe DULL_PROBE = new TaskProbe()
	{

		@Override
		public boolean isCancelRequested()
		{
			return false;
		}

		@Override
		public void fireException(final Exception exception)
		{
		}

		@Override
		public void fireTerminated()
		{
		}

		@Override
		public void fireProgress(final double progress)
		{
		}

		@Override
		public void fireMessage(final String message)
		{
		}

		@Override
		public void fireCanceled()
		{
		}
	};

	void fireProgress(final double progress);

	void fireMessage(final String message);

	boolean isCancelRequested();

	void fireCanceled();

	void fireException(final Exception exception);

	void fireTerminated();

	default void checkCancel() throws CancellationException
	{
		if (isCancelRequested())
		{
			fireCanceled();
			throw new CancellationException();
		}
	}

}
