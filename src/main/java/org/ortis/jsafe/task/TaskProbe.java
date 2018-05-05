
package org.ortis.jsafe.task;

/**
 * A probe to monitor status
 * 
 * @author Ortis <br>
 *         2018 May 05 2:50:18 PM <br>
 */
public interface TaskProbe
{

	void fireProgress(final double progress);

	void fireMessage(final String message);

	boolean isCancelRequested();

	void fireCanceled();

	void fireException(final Exception exception);

	void fireTerminated();

}
