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
package io.ortis.jsafebox.gui.old;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.List;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;

import io.ortis.jsafebox.gui.old.tasks.GuiTask;
import io.ortis.jsafebox.task.Task;

public class ProgressDialog extends JDialog implements WindowListener
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private JFrame parentFrame;
	// private final JTextPane textPane;
	private final JTextArea textPane;

	private final JProgressBar progressBar;

	private Task task;

	public ProgressDialog(final JFrame parentFrame)
	{

		super(parentFrame, true);
		
		this.parentFrame=parentFrame;
		
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		addWindowListener(this);


		getContentPane().setLayout(new BorderLayout(0, 0));

		final JPanel infoPanel = new JPanel();
		infoPanel.setLayout(new BorderLayout(0, 0));
		/*
				textPane = new JTextPane();
				textPane.setFont(new Font("Segoe UI", Font.PLAIN, 14));
				textPane.setOpaque(false);
		
				textPane.setBorder(new EmptyBorder(0, 20, 0, 20));
				textPane.setFocusable(false);
				textPane.setEditable(false);
				StyledDocument doc = textPane.getStyledDocument();
				SimpleAttributeSet center = new SimpleAttributeSet();
				StyleConstants.setAlignment(center, StyleConstants.ALIGN_CENTER);
				doc.setParagraphAttributes(0, doc.getLength(), center, false);
		*/

		textPane = new JTextArea();
		textPane.setFont(new Font("Segoe UI", Font.PLAIN, 14));
		textPane.setOpaque(false);

		textPane.setBorder(new EmptyBorder(0, 20, 0, 20));
		textPane.setFocusable(false);
		textPane.setEditable(false);
		textPane.setLineWrap(true);
		textPane.setWrapStyleWord(true);

		// getContentPane().add(textArea, BorderLayout.CENTER);
		infoPanel.add(textPane, BorderLayout.CENTER);

		/*
				sourceLabel = new JLabel("Initializing transfert...");
				sourceLabel.setBorder(new EmptyBorder(0, 20, 0, 20));
				sourceLabel.setAlignmentY(Component.TOP_ALIGNMENT);
				sourceLabel.setHorizontalAlignment(SwingConstants.CENTER);
				sourceLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
				sourceLabel.setOpaque(true);
				infoPanel.add(sourceLabel, BorderLayout.CENTER);
		
				destinationLabel = new JLabel("");
				destinationLabel.setBorder(new EmptyBorder(0, 20, 0, 20));
				destinationLabel.setAlignmentY(Component.TOP_ALIGNMENT);
				destinationLabel.setHorizontalAlignment(SwingConstants.CENTER);
				destinationLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
				destinationLabel.setOpaque(true);
				infoPanel.add(destinationLabel, BorderLayout.SOUTH);
		*/
		getContentPane().add(infoPanel, BorderLayout.CENTER);

		this.progressBar = new JProgressBar();
		this.progressBar.setStringPainted(true);
		this.progressBar.setMinimumSize(new Dimension(this.getMinimumSize().width, this.getMinimumSize().height / 4));
		this.progressBar.setSize(new Dimension(this.getSize().width, this.getSize().height / 4));
		getContentPane().add(progressBar, BorderLayout.SOUTH);

		setPreferredSize(new Dimension(400, 150));
		setMaximumSize(new Dimension(parentFrame.getWidth() / 2, parentFrame.getHeight() / 2));
		setMinimumSize(new Dimension(parentFrame.getWidth() / 5, parentFrame.getHeight() / 5));
		setSize(getPreferredSize());
		setLocationRelativeTo(parentFrame);
	}

	public void monitor(final GuiTask task, final String defaultMessage)
	{
		if (defaultMessage != null)
			SwingUtilities.invokeLater(new Runnable()
			{

				@Override
				public void run()
				{

					textPane.setText(defaultMessage);
				}
			});

		this.task = task;
		new SwingWorker<Void, Task>()
		{

			@Override
			protected Void doInBackground() throws Exception
			{
				task.start();
				while (!task.isTerminated())
				{
					task.awaitUpdate();
					publish(task);
				}

				return null;
			}

			@Override
			protected void process(List<Task> chunks)
			{

				for (final Task taskMonitor : chunks)
				{

					if (taskMonitor.isCancelRequested())
						textPane.setText("Cancelling...");
					else
					{
						final String msg = taskMonitor.getMessage();
						if (msg != null)
							textPane.setText(msg);

						final double progress = taskMonitor.getProgress();

						if (Double.isFinite(progress))
						{
							final int iProgress = (int) (progress * 100);
							progressBar.setValue(iProgress);
						} else if (!progressBar.isIndeterminate())
						{
							progressBar.setStringPainted(false);
							progressBar.setIndeterminate(true);
						}
					}
					break;
				}

			}

			protected void done()
			{
				dispose();

				if (task.getException() != null)
					SwingUtilities.invokeLater(new Runnable()
					{
						@Override
						public void run()
						{
							new ErrorDialog(ProgressDialog.this.parentFrame, null, task.getException()).setVisible(true);
						}
					});

			}

		}.execute();

		this.setVisible(true);

	}

	@Override
	public void windowOpened(WindowEvent e)
	{

	}

	@Override
	public void windowClosing(final WindowEvent event)
	{

		new SwingWorker<Void, Void>()
		{

			protected Void doInBackground() throws Exception
			{

				if (task != null)
				{
					task.cancel();

					
					ProgressDialog.this.textPane.setText("Cancelling task...");
					try
					{

						task.awaitTermination();
					} catch (final Exception e)
					{
						ProgressDialog.this.dispose();
						new ErrorDialog(ProgressDialog.this.parentFrame, "Error while waiting cancelled task", e).setVisible(true);
					}

					ProgressDialog.this.dispose();
				}

				return null;
			};

		}.execute();

	}

	@Override
	public void windowClosed(WindowEvent e)
	{
	}

	@Override
	public void windowIconified(WindowEvent e)
	{
	}

	@Override
	public void windowDeiconified(WindowEvent e)
	{
	}

	@Override
	public void windowActivated(WindowEvent e)
	{
	}

	@Override
	public void windowDeactivated(WindowEvent e)
	{
	}

}
