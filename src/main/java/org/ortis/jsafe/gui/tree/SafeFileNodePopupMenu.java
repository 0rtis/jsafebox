
package org.ortis.jsafe.gui.tree;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.JTree;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.ortis.jsafe.Folder;
import org.ortis.jsafe.SafeFile;

public class SafeFileNodePopupMenu extends JPopupMenu implements ActionListener
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final static String DELETE = "Delete";
	private final static String RENAME = "Rename";
	private final static String NEW_FOLDER = "New folder";

	private final JTree tree;
	private SafeFileTreeNode node;
	private final SafeFile safeFile;
	private final Folder folder;

	public SafeFileNodePopupMenu(final JTree tree, final SafeFileTreeNode node)
	{

		this.tree = tree;
		this.node = node;
		this.safeFile = node.getSafeFile();
		if (this.safeFile.isFolder())
			this.folder = (Folder) this.safeFile;

		else
			this.folder = null;

		JMenuItem item;

		if (this.folder != null && this.folder.getParent() != null)
		{
			item = new JMenuItem(DELETE);
			item.addActionListener(this);
			item.setOpaque(false);
			item.setBackground(Color.WHITE);
			this.add(item);

			item = new JMenuItem(RENAME);
			item.addActionListener(this);
			item.setOpaque(false);
			item.setBackground(Color.WHITE);
			this.add(item);
		}

		if (safeFile.isFolder())
		{
			if (this.getComponentCount() > 0)
				this.add(new JSeparator());
			item = new JMenuItem(NEW_FOLDER);
			item.addActionListener(this);
			this.add(item);
		}

	}

	@Override
	public void actionPerformed(final ActionEvent e)
	{

		switch (e.getActionCommand())
		{
			case DELETE:

				break;

			case RENAME:
				this.tree.startEditingAtPath(this.tree.getSelectionPath());
				break;
			case NEW_FOLDER:

				if (this.folder != null)
				{

					// this.folder.mkdir(name)
				}

				break;
			default:
				break;
		}

		System.out.println(e);
	}

	public static TreePath getPath(TreeNode treeNode)
	{
		List<Object> nodes = new ArrayList<Object>();
		if (treeNode != null)
		{
			nodes.add(treeNode);
			treeNode = treeNode.getParent();
			while (treeNode != null)
			{
				nodes.add(0, treeNode);
				treeNode = treeNode.getParent();
			}
		}

		return nodes.isEmpty() ? null : new TreePath(nodes.toArray());
	}

}
