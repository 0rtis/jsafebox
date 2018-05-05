
package org.ortis.jsafe.gui.tasks;

import javax.swing.SwingWorker;

import org.ortis.jsafe.Safe;
import org.ortis.jsafe.gui.SafeExplorer;
import org.ortis.jsafe.task.MultipartTask;
import org.ortis.jsafe.task.TaskProbeAdapter;

public class SaveTask extends MultipartTask implements GuiTask
{

	private final SafeExplorer safeExplorer;
	private final Safe safe;

	public SaveTask(final SafeExplorer safeExplorer)
	{

		this.safeExplorer = safeExplorer;
		this.safe = this.safeExplorer.getSafe();
	}

	public void start()
	{
		final TaskProbeAdapter adapter = new TaskProbeAdapter();
		this.fireMessage("Saving safe...");
		this.fireProgress(0);
		new SwingWorker<Void, String>()
		{
			private Safe newSafe = null;

			@Override
			protected Void doInBackground() throws Exception
			{

				try
				{

					SaveTask.this.setSubTask(adapter);
					newSafe = safe.save(adapter);

				} catch (final Exception e)
				{
					safe.discardChanges();
					return null;

				} finally
				{
					SaveTask.this.setSubTask(null);
				}

				return null;
			}

			@Override
			protected void done()
			{

				if (SaveTask.this.getException() == null)
				{
					SaveTask.this.fireProgress(1);
					SaveTask.this.fireMessage("Updating tree...");

					safeExplorer.setSafe(newSafe);

				}

				SaveTask.this.fireTerminated();
			}

		}.execute();

	}

}
