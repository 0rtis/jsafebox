/*******************************************************************************
 * Copyright 2018 Ortis (cao.ortis.org@gmail.com)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package org.ortis.jsafebox.task;

import java.util.concurrent.TimeUnit;

/**
 * A task that that can be observed and awaited
 * 
 * @author Ortis <br>
 *         2018 May 05 2:48:25 PM <br>
 */
public interface Task
{

	double getProgress();

	String getMessage();

	Exception getException();

	boolean isCancelled();

	boolean isTerminated();

	boolean isCompleted();

	boolean isCancelRequested();

	void cancel();

	void awaitUpdate() throws InterruptedException;

	boolean awaitUpdate(final long timeout, final TimeUnit unit) throws InterruptedException;

	void awaitTermination() throws InterruptedException;

	boolean awaitTermination(final long timeout, final TimeUnit unit) throws InterruptedException;

	boolean addListener(final TaskListener listener);

	boolean removeListener(final TaskListener listener);
}
