
package io.ortis.jsafebox.gui.tasks;

import java.util.Collection;
import java.util.function.Consumer;
import java.util.logging.LogRecord;

/**
 * 
 * @author Ortis
 *
 */
public interface GUITask
{
	void task() throws Exception;

	String getProcessingHeader();

	default boolean skipResultOnSuccess()
	{
		return false;

	}
/*
	void setSucessCallback(final Callback callback);

	void sucessCallback();
*/
	String getSuccessHeader();

	String getSuccessMessage();

	void start(final Consumer<LogRecord> logListener);

	boolean isTerminated();

	Exception getException();

	<D extends Collection<LogRecord>> D getLogs(final D destination);
}
