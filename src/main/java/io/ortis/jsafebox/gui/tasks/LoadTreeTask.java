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

package io.ortis.jsafebox.gui.tasks;

import io.ortis.jsafebox.Folder;
import io.ortis.jsafebox.Safe;
import io.ortis.jsafebox.SafeFile;
import io.ortis.jsafebox.gui.SafeboxFrame;
import io.ortis.jsafebox.gui.tree.FileTransferHandler;
import io.ortis.jsafebox.gui.tree.SafeFileTreeCellRenderer;
import io.ortis.jsafebox.gui.tree.SafeFileTreeNode;
import io.ortis.jsafebox.gui.tree.SafeTreeModel;

import javax.swing.*;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;
import java.util.logging.Logger;

/**
 * @author Ortis
 */
public class LoadTreeTask extends AbstractGUITask
{
	private final Safe safe;
	private final JTree tree;
	private final SafeboxFrame safeboxFrame;
	private final boolean lazyLoading;

	public LoadTreeTask(final Safe safe, final JTree tree, final SafeboxFrame safeboxFrame, final boolean lazyLoading, final Logger log)
	{
		super("Loading safebox tree", "Success", "Safebox tree successfully loaded !", log);

		this.safe = safe;
		this.tree = tree;
		this.safeboxFrame = safeboxFrame;
		this.lazyLoading = lazyLoading;
	}

	@Override
	public boolean skipResultOnSuccess()
	{
		return true;
	}

	@Override
	public void task() throws Exception
	{
		try
		{
			this.tree.removeTreeSelectionListener(this.safeboxFrame);


			if(this.safe == null)
			{
				this.tree.setModel(new DefaultTreeModel(null));
				this.tree.setCellRenderer(new DefaultTreeCellRenderer());
				this.tree.setTransferHandler(null);
			}
			else
			{
				final SafeTreeModel model = new SafeTreeModel(this.safeboxFrame);
				SafeFileTreeNode node = new SafeFileTreeNode(safe.getRootFolder());

				model.getRootMode().add(node);
				if(this.lazyLoading)
					for(final SafeFile safeFile : safe.getRootFolder().listFiles())
						node.add(new SafeFileTreeNode(safeFile));
				else
					for(final SafeFile safeFile : safe.getRootFolder().listFiles())
						recursiveAdd(node, safeFile);

				this.tree.setModel(model);
				model.reload();

				tree.expandRow(0);
				tree.repaint();

				tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

				tree.setRootVisible(false);
				tree.setCellRenderer(new SafeFileTreeCellRenderer());

				this.tree.addTreeSelectionListener(this.safeboxFrame);

				tree.setTransferHandler(new FileTransferHandler(this.safeboxFrame));

				tree.addMouseListener(this.safeboxFrame);
				tree.addKeyListener(this.safeboxFrame);

				tree.setEditable(true);

				this.safeboxFrame.setTitle(SafeboxFrame.TITLE + " - " + safe.getFile().getAbsolutePath());
			}


		} finally
		{

		}
	}

	public void recursiveAdd(final SafeFileTreeNode parent, final SafeFile safeFile)
	{
		log.info("Loading " + safeFile);
		final SafeFileTreeNode node = new SafeFileTreeNode(safeFile);
		parent.add(node);

		if(safeFile.isFolder())
			for(final SafeFile child : ((Folder) safeFile).listFiles())
				recursiveAdd(node, child);
	}

	public Safe getSafe()
	{
		return safe;
	}
}
