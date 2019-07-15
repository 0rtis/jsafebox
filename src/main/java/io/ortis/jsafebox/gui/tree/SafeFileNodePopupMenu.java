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

package io.ortis.jsafebox.gui.tree;

import io.ortis.jsafebox.*;
import io.ortis.jsafebox.gui.*;
import io.ortis.jsafebox.gui.tasks.DeleteTask;
import io.ortis.jsafebox.gui.tasks.ExceptionTask;
import io.ortis.jsafebox.gui.tasks.ExtractTask;
import io.ortis.jsafebox.gui.tasks.SaveTask;

import javax.swing.*;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SafeFileNodePopupMenu extends JPopupMenu implements ActionListener
{
	public final static String DELETE = "Delete";
	public final static String EXTRACT = "Extract...";
	public final static String NEW_FOLDER = "New folder";


	private static final long serialVersionUID = 1L;
	private final SafeboxFrame safeboxFrame;
	private final Safe safe;
	private final SafeFileTreeNode node;
	private final SafeFile safeFile;
	private final Folder folder;

	public SafeFileNodePopupMenu(final JTree tree, final SafeFileTreeNode node)
	{
		this.safeboxFrame = ((SafeTreeModel) tree.getModel()).getSafeboxFrame();
		this.safe = safeboxFrame.getSafe();
		this.node = node;
		this.safeFile = this.node.getSafeFile();
		if(this.safeFile.isFolder())
			this.folder = (Folder) this.safeFile;
		else
			this.folder = null;

		JMenuItem item;

		item = new JMenuItem(EXTRACT);
		item.addActionListener(this);
		item.setOpaque(false);
		item.setBackground(Color.WHITE);
		if(this.folder != null && this.folder.listFiles().isEmpty())
			item.setEnabled(false);
		this.add(item);

		item = new JMenuItem(DELETE);
		item.addActionListener(this);
		item.setOpaque(false);
		item.setBackground(Color.WHITE);
		if(folder != null && folder.getParent() == null)
			item.setEnabled(false);

		this.add(item);

		if(safeFile.isFolder())
		{
			if(this.getComponentCount() > 0)
				this.add(new JSeparator());
			item = new JMenuItem(NEW_FOLDER);
			item.addActionListener(this);
			this.add(item);
		}
	}

	@Override
	public void actionPerformed(final ActionEvent event)
	{

		switch(event.getActionCommand())
		{
			case DELETE:
				try
				{
					final WarningOptionFrame optionFrame = new WarningOptionFrame(this.safeboxFrame, "Confirm deletion",
							"\n\nYou are about to delete " + this.safeFile.getPath() + "", "Confirm ?");
					optionFrame.setVisible(true);

					if(optionFrame.getChoice() != Boolean.TRUE)
						return;

					final DeleteTask deleteTask = new DeleteTask(this.safeFile, this.safe, GUI.getLogger());
					final ProgressFrame progressFrame = new ProgressFrame(this.safeboxFrame);
					progressFrame.execute(deleteTask);

					if(deleteTask.getException() == null)
					{
						this.node.setStatus(SafeFileTreeNode.Status.Deleted);
						this.safeboxFrame.notifyModificationPending();

						if(Settings.getSettings().isAutoSave())
						{
							final SaveTask saveTask = new SaveTask(this.safe, GUI.getLogger());
							progressFrame.execute(saveTask);
							if(saveTask.getException() == null)
							{
								this.safeboxFrame.setSafe(saveTask.getNewSafe());
							}
						}
					}
				} catch(Exception e)
				{
					new ResultFrame(this.safeboxFrame, new ExceptionTask(e, GUI.getLogger()));
				}
				break;

			case EXTRACT:

				try
				{
					JFileChooser jfc = new JFileChooser(Settings.getDefaultDirectory());
					jfc.setDialogTitle("Choose a directory to extract in");
					jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

					File destination = null;
					int returnValue = jfc.showSaveDialog(this.safeboxFrame);
					if(returnValue == JFileChooser.APPROVE_OPTION)
						if(jfc.getSelectedFile().isDirectory())
						{
							destination = jfc.getSelectedFile();
							Settings.setDefaultDirectory(destination);
						}

					if(destination != null)
					{
						final ExtractTask extractTask = new ExtractTask(this.safeFile, destination, this.safe, GUI.getLogger());
						final ProgressFrame progressFrame = new ProgressFrame(this.safeboxFrame);
						progressFrame.execute(extractTask);

					}

				} catch(Exception e)
				{
					new ResultFrame(this.safeboxFrame, new ExceptionTask(e, GUI.getLogger()));
				}
				break;

			case NEW_FOLDER:

				if(this.folder != null)
				{
					final String name = JOptionPane.showInputDialog(this.safeboxFrame, "Folder name", "New folder", JOptionPane.PLAIN_MESSAGE);

					if(name != null && name.length() > 0)
					{
						final String sanitized = Utils.sanitizeToken(name, Environment.getSubstitute());
						try
						{
							this.folder.mkdir(sanitized);
							this.safeboxFrame.notifyModificationPending();

						} catch(final Exception e)
						{
							new ResultFrame(this.safeboxFrame, new ExceptionTask(e, GUI.getLogger()));
						}
					}
				}

				break;
			default:
				break;
		}

	}

	public static TreePath getPath(TreeNode treeNode)
	{
		List<Object> nodes = new ArrayList<Object>();
		if(treeNode != null)
		{
			nodes.add(treeNode);
			treeNode = treeNode.getParent();
			while(treeNode != null)
			{
				nodes.add(0, treeNode);
				treeNode = treeNode.getParent();
			}
		}

		return nodes.isEmpty() ? null : new TreePath(nodes.toArray());
	}

}
