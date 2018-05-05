
package org.ortis.jsafe.task;

public interface TaskListener
{

	void onProgress(final Task task, final double progress);

	void onMessage(final Task task, final String message);

	void onException(final Task task, final Exception exception);

	void onTerminated(final Task task);

	void onCancellationRequested(final Task task);

	void onCancelled(final Task task);

}
