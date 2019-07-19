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
package io.ortis.jsafebox.gui.viewers;

import io.ortis.jsafebox.Block;
import io.ortis.jsafebox.Folder;
import io.ortis.jsafebox.Safe;
import io.ortis.jsafebox.gui.*;
import io.ortis.jsafebox.gui.tasks.AddStreamTask;
import io.ortis.jsafebox.gui.tasks.DeleteTask;
import io.ortis.jsafebox.gui.tasks.ExceptionTask;
import io.ortis.jsafebox.gui.tasks.SaveTask;
import io.ortis.jsafebox.gui.tree.SafeFileTreeNode;
import io.ortis.jsafebox.gui.tree.SafeTreeModel;

import javax.swing.*;
import javax.swing.tree.TreeNode;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * @author Ortis
 */
public class TextViewer extends JFrame implements ActionListener
{

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	private final JTree tree;
	private final SafeboxFrame safeboxFrame;

	private SafeFileTreeNode node;
	private JTextArea textArea;
	private JRadioButtonMenuItem wordWrapMenuItem;

	private JMenuItem saveMenuItem;
	private JRadioButtonMenuItem editableMenuItem;

	/**
	 * Create the application.
	 */
	public TextViewer(final JTree tree, final SafeFileTreeNode node) throws Exception
	{
		this.tree = tree;
		this.safeboxFrame = ((SafeTreeModel) tree.getModel()).getSafeboxFrame();
		this.node = node;

		initialize();
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);


		try(final ByteArrayOutputStream baos = new ByteArrayOutputStream())
		{
			this.safeboxFrame.getSafe().extract((Block) node.getSafeFile(), true, baos);
			final String text = new String(baos.toByteArray());// Use local Charset
			this.textArea.setText(text);
		}


		this.textArea.setCaretPosition(0);
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize()
	{

		final JPanel main = new JPanel(new BorderLayout());
		textArea = new JTextArea();
		this.textArea.setEditable(false);

		textArea.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));

		JScrollPane scrollPane = new JScrollPane(textArea);

		main.add(scrollPane, BorderLayout.CENTER);

		setContentPane(main);

		JMenuBar menuBar = new JMenuBar();
		main.add(menuBar, BorderLayout.NORTH);

		JMenu fileMenu = new JMenu("File");
		menuBar.add(fileMenu);


		saveMenuItem = new JMenuItem("Save");
		saveMenuItem.setEnabled(false);
		saveMenuItem.addActionListener(this);
		fileMenu.add(saveMenuItem);

		editableMenuItem = new JRadioButtonMenuItem("Editable");
		editableMenuItem.setSelected(false);
		editableMenuItem.addActionListener(this);
		fileMenu.add(editableMenuItem);


		JMenu mnNewMenu = new JMenu("View");
		menuBar.add(mnNewMenu);
		wordWrapMenuItem = new JRadioButtonMenuItem("Line wrap");
		wordWrapMenuItem.addActionListener(this);
		mnNewMenu.add(wordWrapMenuItem);


		setIconImages(Settings.getSettings().getTextFrameIcons());

		setSize(Toolkit.getDefaultToolkit().getScreenSize().width / 2, Toolkit.getDefaultToolkit().getScreenSize().height * 2 / 3);
		setLocation(Toolkit.getDefaultToolkit().getScreenSize().width / 2 - getSize().width / 2,
				Toolkit.getDefaultToolkit().getScreenSize().height / 2 - getSize().height / 2);
	}

	@Override
	public void actionPerformed(final ActionEvent event)
	{
		if(event.getSource() == saveMenuItem)
		{
			final Safe safe = this.safeboxFrame.getSafe();
			final DeleteTask deleteTask = new DeleteTask(this.node.getSafeFile(), safe, GUI.getLogger()){
				@Override
				public boolean skipResultOnSuccess()
				{
					return true;
				}
			};

			final ProgressFrame progressFrame = new ProgressFrame(this.safeboxFrame);
			progressFrame.execute(deleteTask);

			if(deleteTask.getException() == null)
			{
				final String text = this.textArea.getText();

				try(final ByteArrayInputStream bais = new ByteArrayInputStream(text.getBytes()))
				{
					final Folder destination = node.getSafeFile().getParent();
					final AddStreamTask task = new AddStreamTask(bais, node.getSafeFile().getName(), destination, safe, GUI.getLogger());

					progressFrame.execute(task);

					if(task.getException() == null)
					{
						this.node.setStatus(SafeFileTreeNode.Status.Updated);
						this.safeboxFrame.notifyModificationPending();

						if(Settings.getSettings().isAutoSave())
						{
							final SaveTask saveTask = new SaveTask(safe, GUI.getLogger());
							progressFrame.execute(saveTask);

							if(saveTask.getException() == null)
								this.safeboxFrame.setSafe(saveTask.getNewSafe());
						}


						editableMenuItem.doClick();
					}

					toFront();

				} catch(final IOException e)
				{
					new ResultFrame(this.safeboxFrame, new ExceptionTask(e, GUI.getLogger()));
				}
			}
		}
		else if(event.getSource() == editableMenuItem)
		{
			this.textArea.setEditable(editableMenuItem.isSelected());
			this.saveMenuItem.setEnabled(this.textArea.isEditable());
		}
		else if(event.getSource() == wordWrapMenuItem)
		{
			this.textArea.setLineWrap(wordWrapMenuItem.isSelected());
		}

	}

}
