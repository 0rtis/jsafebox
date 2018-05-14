/*******************************************************************************
 * Copyright 2018 Ortis (cao.ortis.org@gmail.com)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under the License.
 ******************************************************************************/

package org.ortis.jsafebox.gui.tree;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.JTree;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.ortis.jsafebox.Environment;
import org.ortis.jsafebox.Folder;
import org.ortis.jsafebox.SafeFile;
import org.ortis.jsafebox.Utils;
import org.ortis.jsafebox.gui.ErrorDialog;
import org.ortis.jsafebox.gui.ProgressDialog;
import org.ortis.jsafebox.gui.SafeExplorer;
import org.ortis.jsafebox.gui.tasks.DeleteTask;
import org.ortis.jsafebox.gui.tasks.ExtractTask;
import org.ortis.jsafebox.gui.tasks.SaveTask;

public class SafeFileNodePopupMenu extends JPopupMenu implements ActionListener
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final static String DELETE = "Delete";
	private final static String EXTRACT = "Extract...";
	private final static String NEW_FOLDER = "New folder";

	private final SafeExplorer safeExplorer;
	private final JTree tree;
	private final SafeFile safeFile;
	private final Folder folder;

	public SafeFileNodePopupMenu(final JTree tree, final SafeFileTreeNode node)
	{

		this.tree = tree;
		this.safeExplorer = ((SafeTreeModel) this.tree.getModel()).getSafeExplorer();
		this.safeFile = node.getSafeFile();
		if (this.safeFile.isFolder())
			this.folder = (Folder) this.safeFile;

		else
			this.folder = null;

		JMenuItem item;

		item = new JMenuItem(EXTRACT);
		item.addActionListener(this);
		item.setOpaque(false);
		item.setBackground(Color.WHITE);
		if (this.folder != null && this.folder.listFiles().isEmpty())
			item.setEnabled(false);
		this.add(item);

		item = new JMenuItem(DELETE);
		item.addActionListener(this);
		item.setOpaque(false);
		item.setBackground(Color.WHITE);
		if (folder != null && folder.getParent() == null)
			item.setEnabled(false);

		this.add(item);

		if (safeFile.isFolder())
		{
			if (this.getComponentCount() > 0)
				this.add(new JSeparator());
			item = new JMenuItem(NEW_FOLDER);
			item.addActionListener(this);
			this.add(item);
		}

	}

	@Override
	public void actionPerformed(final ActionEvent event)
	{

		switch (event.getActionCommand())
		{
			case DELETE:
				try
				{

					final JOptionPane optionPane = new JOptionPane("Do you want to delete '" + this.safeFile.getName() + "' ?", JOptionPane.WARNING_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
					final JDialog dialog = optionPane.createDialog(this.safeExplorer.getExplorerFrame(), "Confirm delete");
					dialog.addKeyListener(new KeyAdapter()
					{
						public void keyPressed(KeyEvent ke)
						{ // handler
							if (ke.getKeyCode() == KeyEvent.VK_ESCAPE)
								dialog.dispose();
						}
					});

					dialog.setVisible(true);

					final Integer action = (Integer) optionPane.getValue();

					if (action == null || action != JOptionPane.YES_OPTION)
						return;

					final ProgressDialog pd = new ProgressDialog(this.safeExplorer.getExplorerFrame());
					pd.setTitle("Deleting...");
					final DeleteTask deleteTask = new DeleteTask(this.safeExplorer, this.safeFile);
					pd.monitor(deleteTask, "Deleting...");
					if (deleteTask.isCompleted() && this.safeExplorer.getConfiguration().getAutoSave())
					{
						pd.setTitle("Saving safe...");
						final SaveTask saveTask = new SaveTask(this.safeExplorer);
						pd.monitor(saveTask, "Saving safe...");
					}

				} catch (Exception e)
				{
					new ErrorDialog(this.safeExplorer.getExplorerFrame(), "Error while deleting", e).setVisible(true);
				}
				break;

			case EXTRACT:

				try
				{

					JFileChooser jfc = new JFileChooser(this.safeExplorer.getConfiguration().getExtractDirectory());
					jfc.setDialogTitle("Choose a directory to extract in");
					jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

					File destination = null;
					int returnValue = jfc.showSaveDialog(this.safeExplorer.getExplorerFrame());
					if (returnValue == JFileChooser.APPROVE_OPTION)
						if (jfc.getSelectedFile().isDirectory())
						{
							destination = jfc.getSelectedFile();
							this.safeExplorer.getConfiguration().setExtractDirectory(destination);
						}

					if (destination != null)
					{
						final ProgressDialog pd = new ProgressDialog(this.safeExplorer.getExplorerFrame());
						pd.setTitle("Extracting...");
						final ExtractTask extractTask = new ExtractTask(this.safeExplorer, this.safeFile, destination);
						pd.monitor(extractTask, "Extracting...");
					}

				} catch (Exception e)
				{
					new ErrorDialog(this.safeExplorer.getExplorerFrame(), "Error while extracting", e).setVisible(true);
				}
				break;
			case NEW_FOLDER:

				if (this.folder != null)
				{

					final String name = JOptionPane.showInputDialog(this.safeExplorer.getExplorerFrame(), "Folder name", "New folder", JOptionPane.PLAIN_MESSAGE);

					if (name != null && name.length() > 0)
					{
						final String sanitized = Utils.sanitizeToken(name, Environment.getSubstitute());
						try
						{
							this.folder.mkdir(sanitized);
							this.safeExplorer.setSafe(this.safeExplorer.getSafe());
						} catch (final Exception e)
						{
							new ErrorDialog(this.safeExplorer.getExplorerFrame(), "Error creating new folder", e).setVisible(true);
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
		if (treeNode != null)
		{
			nodes.add(treeNode);
			treeNode = treeNode.getParent();
			while (treeNode != null)
			{
				nodes.add(0, treeNode);
				treeNode = treeNode.getParent();
			}
		}

		return nodes.isEmpty() ? null : new TreePath(nodes.toArray());
	}

}
