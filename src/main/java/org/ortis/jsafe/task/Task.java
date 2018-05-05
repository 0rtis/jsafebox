
package org.ortis.jsafe.task;

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
