
package org.ortis.jsafe.gui.tree;

import javax.swing.tree.DefaultMutableTreeNode;

import org.ortis.jsafe.Folder;
import org.ortis.jsafe.SafeFile;

public class SafeFileTreeNode extends DefaultMutableTreeNode
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final SafeFile safeFile;
	private final boolean root;
	private boolean dropTarget;

	public SafeFileTreeNode(final SafeFile safeFile)
	{
		this.safeFile = safeFile;
		this.dropTarget = false;

		if (safeFile.isFolder())
		{
			if (((Folder) safeFile).getParent() == null)
				this.root = true;
			else
				this.root = false;
		} else
			this.root = false;
		
		setUserObject(this.safeFile)	;
	}
	
	public SafeFile getSafeFile()
	{
		return safeFile;
	}

	public boolean isSafeRoot()
	{
		return root;
	}

	public boolean isDropTarget()
	{
		return dropTarget;
	}

	public void setDropTarget(final boolean dropTarget)
	{
		this.dropTarget = dropTarget;
	}

}
