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
package io.ortis.jsafebox.gui.old.tree;

import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.InputEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JTree;
import javax.swing.SwingWorker;
import javax.swing.TransferHandler;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import io.ortis.jsafebox.gui.old.tree.SafeTreeModel;
import io.ortis.jsafebox.gui.old.tasks.AddTask;
import io.ortis.jsafebox.gui.old.tasks.SaveTask;
import io.ortis.jsafebox.Folder;
import io.ortis.jsafebox.gui.old.ProgressDialog;

public class FileTransferHandler extends TransferHandler
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public boolean importData(final JComponent comp, final Transferable t)
	{
		if (!(comp instanceof JTree))
			return false;

		if (!t.isDataFlavorSupported(DataFlavor.javaFileListFlavor))
		{
			return false;
		}
		final JTree tree = (JTree) comp;

		try
		{
			List data = (List) t.getTransferData(DataFlavor.javaFileListFlavor);
			Iterator i = data.iterator();
			final List<File> sources = new ArrayList<>();
			while (i.hasNext())
			{
				File f = (File) i.next();
				sources.add(f);
			}

			final SafeFileTreeNode node = (SafeFileTreeNode) tree.getLastSelectedPathComponent();

			new SwingWorker<Void, Void>()
			{
				@Override
				protected Void doInBackground() throws Exception
				{
					final SafeTreeModel model = (SafeTreeModel) tree.getModel();
					final ProgressDialog pd = new ProgressDialog(model.getSafeExplorer().getExplorerFrame());

					final Folder destination;
					if (node.getSafeFile().isFolder())
						destination = (Folder) node.getSafeFile();
					else
						destination = node.getSafeFile().getParent();

					pd.setTitle("Transfert...");
					final AddTask addTask = new AddTask(model.getSafeExplorer(), sources, destination);
					pd.monitor(addTask, "Initializing transfert...");

					if (addTask.isCompleted() && model.getSafeExplorer().getConfiguration().getAutoSave())
					{
						pd.setTitle("Saving safe...");
						final SaveTask saveTask = new SaveTask(model.getSafeExplorer());
						pd.monitor(saveTask, "Saving safe...");
					}

					return null;

				}
			}.execute();
			

			return true;
		} catch (Exception ioe)
		{
			ioe.printStackTrace();
		}
		return false;
	}

	@Override
	public boolean canImport(final TransferSupport support)
	{
		if (!support.isDrop())
			return false;

		if (!(support.getComponent() instanceof JTree))
			return false;

		final JTree tree = (JTree) support.getComponent();
		final Point point = support.getDropLocation().getDropPoint();
		final int selRow = tree.getRowForLocation((int) point.getX(), (int) point.getY());
		final TreePath selPath = tree.getPathForLocation((int) point.getX(), (int) point.getY());
		if (selRow != -1)
		{
			// final SafeFileTreeNode node = (SafeFileTreeNode) selPath.getLastPathComponent();

		} else
			return false;

		// support.setShowDropLocation(true);

		if (!support.isDataFlavorSupported(DataFlavor.javaFileListFlavor))
			return false;

		boolean copySupported = (COPY & support.getSourceDropActions()) == COPY;
		if (copySupported)
		{
			support.setDropAction(COPY);
			return true;
		}

		return false;
	}

	@Override
	public boolean canImport(JComponent comp, DataFlavor [] transferFlavors)
	{
		if (comp instanceof JTree)
		{

			final JTree tree = (JTree) comp;

			System.out.println(tree.getSelectionPaths());

			for (int i = 0; i < transferFlavors.length; i++)
			{
				if (!transferFlavors[i].equals(DataFlavor.javaFileListFlavor))
				{
					return false;
				}
			}
			return true;
		}
		return false;
	}

	@Override
	public int getSourceActions(final JComponent c)
	{
		return COPY;
	}

	@Override
	protected Transferable createTransferable(final JComponent c)
	{

//		System.out.println("Create tranferable");
		TreePath p = ((JTree) c).getSelectionPath();
		DefaultMutableTreeNode n = (DefaultMutableTreeNode) p.getLastPathComponent();

		if (!(n instanceof SafeFileTreeNode))
			return null;

		File file;
		try
		{
			file = Files.createTempFile(null, null).toFile();
			return new Transferable()
			{
				@Override
				public Object getTransferData(DataFlavor flavor)
				{
					return  Arrays.asList(file);
				}

				@Override
				public DataFlavor [] getTransferDataFlavors()
				{
					return new DataFlavor[] { DataFlavor.javaFileListFlavor };
				}

				@Override
				public boolean isDataFlavorSupported(DataFlavor flavor)
				{
					return flavor.equals(DataFlavor.javaFileListFlavor);
				}
			};

		} catch (IOException ex)
		{
			ex.printStackTrace();
		}

		return null;
	}

	@Override
	public void exportAsDrag(JComponent comp, InputEvent e, int action)
	{
		
		super.exportAsDrag(comp, e, action);
	}

	@Override
	protected void exportDone(JComponent c, Transferable d, int a)
	{
		
	}

}
