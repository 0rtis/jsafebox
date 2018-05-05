
package org.ortis.jsafe.gui.tree;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import org.ortis.jsafe.Safe;
import org.ortis.jsafe.gui.SafeExplorer;

public class SafeTreeModel extends DefaultTreeModel
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final SafeFileTreeNode root;
	private Safe safe;
	private final SafeExplorer safeExplorer;

	public SafeTreeModel(final SafeExplorer safeExplorer)
	{

		super(safeExplorer.getSafe() == null ? new DefaultMutableTreeNode() : new SafeFileTreeNode(safeExplorer.getSafe().getRootFolder()));
		
		this.safeExplorer = safeExplorer;
		if (getRoot() instanceof SafeFileTreeNode)
			this.root = (SafeFileTreeNode) getRoot();
		else
			this.root = null;

		this.safe = this.safeExplorer.getSafe();
	}

	public SafeFileTreeNode getRootMode()
	{
		return this.root;
	}

	public Safe getSafe()
	{
		return this.safe;
	}
	
	public SafeExplorer getSafeExplorer()
	{
		return safeExplorer;
	}

}
