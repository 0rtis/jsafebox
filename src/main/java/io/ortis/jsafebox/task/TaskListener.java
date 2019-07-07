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
package io.ortis.jsafebox.task;

public interface TaskListener
{

	void onProgress(final Task task, final double progress);

	void onMessage(final Task task, final String message);

	void onException(final Task task, final Exception exception);

	void onTerminated(final Task task);

	void onCancellationRequested(final Task task);

	void onCancelled(final Task task);

}
