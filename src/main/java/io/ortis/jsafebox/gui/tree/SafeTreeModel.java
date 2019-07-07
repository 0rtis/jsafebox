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
package io.ortis.jsafebox.gui.tree;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import io.ortis.jsafebox.Safe;
import io.ortis.jsafebox.gui.old.SafeExplorer;
import io.ortis.jsafebox.gui.old.tree.SafeFileTreeNode;

public class SafeTreeModel extends DefaultTreeModel
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final SafeFileTreeNode root;
	private final Safe safe;


	public SafeTreeModel(final Safe safe)
	{

		super(safe == null ? new DefaultMutableTreeNode() : new SafeFileTreeNode(safe.getRootFolder()));
		
		this.safe = safe;

		if (getRoot() instanceof SafeFileTreeNode)
			this.root = (SafeFileTreeNode) getRoot();
		else
			this.root = null;
	}

	public SafeFileTreeNode getRootMode()
	{
		return this.root;
	}

	public Safe getSafe()
	{
		return this.safe;
	}
	

}
