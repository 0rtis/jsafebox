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

import io.ortis.jsafebox.SafeFile;

import javax.swing.tree.DefaultMutableTreeNode;

public class SafeFileTreeNode extends DefaultMutableTreeNode
{
	private static final long serialVersionUID = 1L;

	public enum Status
	{
		Unchanged, Added, Deleted, Updated
	}

	private  SafeFile safeFile;
	private final boolean root;
	private boolean dropTarget;
	private Status status = Status.Unchanged;


	public SafeFileTreeNode(final SafeFile safeFile)
	{
		this.safeFile = safeFile;
		this.dropTarget = false;

		if(this.safeFile.isFolder())
		{
			if(safeFile.getParent() == null)
				this.root = true;
			else
				this.root = false;
		}
		else
			this.root = false;

		setUserObject(this.safeFile);
	}

	public synchronized Status getStatus()
	{
		return status;
	}

	public synchronized void setStatus(final Status status)
	{
		this.status = status;
	}

	public SafeFile getSafeFile()
	{
		return safeFile;
	}

	public void setSafeFile(final SafeFile safeFile)
	{
		this.safeFile = safeFile;
		this.status = Status.Updated;
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

	@Override
	public int hashCode()
	{
		return this.safeFile.hashCode();
	}

	@Override
	public boolean equals(final Object o)
	{
		if(o == this)
			return true;

		if(o instanceof  SafeFileTreeNode)
		{
			final SafeFileTreeNode other = (SafeFileTreeNode)o;

			return this.safeFile.equals(other.safeFile);
		}
		return false;
	}
}
