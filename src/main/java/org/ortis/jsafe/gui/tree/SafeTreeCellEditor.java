
package org.ortis.jsafe.gui.tree;

import java.awt.event.MouseEvent;
import java.util.EventObject;

import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellEditor;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;

public class SafeTreeCellEditor extends DefaultTreeCellEditor
{

	public SafeTreeCellEditor(JTree tree, DefaultTreeCellRenderer renderer)
	{
		super(tree, renderer);
	}

	public boolean isCellEditable(final EventObject event)
	{
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
				System.out.println(path.getPathCount());
				System.out.println(node);
				if(node.isSafeRoot())
					return false;
				return path.getPathCount() > 1; // root and direct children are not editable
			} else
				return false;

		}

		return false;
	}

}
