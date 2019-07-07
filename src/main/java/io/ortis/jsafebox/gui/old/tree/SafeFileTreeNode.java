/*******************************************************************************
 * Copyright 2018 Ortis (cao.ortis.org@gmail.com)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package io.ortis.jsafebox.gui.old.tree;

import javax.swing.tree.DefaultMutableTreeNode;

import io.ortis.jsafebox.Folder;
import io.ortis.jsafebox.SafeFile;

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
