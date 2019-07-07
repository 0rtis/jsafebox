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

import io.ortis.jsafebox.Safe;
import io.ortis.jsafebox.gui.old.SafeExplorer;
import io.ortis.jsafebox.gui.tree.SafeFileTreeNode;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

public class SafeTreeModel extends DefaultTreeModel
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final io.ortis.jsafebox.gui.tree.SafeFileTreeNode root;
	private Safe safe;
	private final SafeExplorer safeExplorer;

	public SafeTreeModel(final SafeExplorer safeExplorer)
	{

		super(safeExplorer.getSafe() == null ? new DefaultMutableTreeNode() : new io.ortis.jsafebox.gui.tree.SafeFileTreeNode(safeExplorer.getSafe().getRootFolder()));

		this.safeExplorer = safeExplorer;
		if (getRoot() instanceof io.ortis.jsafebox.gui.tree.SafeFileTreeNode)
			this.root = (io.ortis.jsafebox.gui.tree.SafeFileTreeNode) getRoot();
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
