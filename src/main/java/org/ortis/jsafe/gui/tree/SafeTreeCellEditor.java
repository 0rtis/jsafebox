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
package org.ortis.jsafe.gui.tree;

import java.util.EventObject;

import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellEditor;
import javax.swing.tree.DefaultTreeCellRenderer;

public class SafeTreeCellEditor extends DefaultTreeCellEditor
{

	public SafeTreeCellEditor(JTree tree, DefaultTreeCellRenderer renderer)
	{
		super(tree, renderer);
	}

	public boolean isCellEditable(final EventObject event)
	{

		return false;
		/*
		if (!super.isCellEditable(event))
			return false;
		
		if (event != null && event.getSource() instanceof JTree && event instanceof MouseEvent)
		{
			MouseEvent mouseEvent = (MouseEvent) event;
			JTree tree = (JTree) event.getSource();
			TreePath path = tree.getPathForLocation(mouseEvent.getX(), mouseEvent.getY());
			final int selRow = tree.getRowForLocation(mouseEvent.getX(), mouseEvent.getY());
			if (selRow != -1)
			{
				final SafeFileTreeNode node = (SafeFileTreeNode) path.getLastPathComponent();
				if(node.isSafeRoot())
					return false;
				return path.getPathCount() > 1; // root and direct children are not editable
			} else
				return false;
		
		}
		
		return false;
		*/
	}

}
