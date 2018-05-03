
package org.ortis.jsafe.gui;

import java.awt.Component;
import java.awt.Toolkit;
import java.io.File;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.filechooser.FileSystemView;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

import org.ortis.jsafe.Folder;
import org.ortis.jsafe.SafeFile;

public class SafeFileRenderer extends DefaultTreeCellRenderer
{

	private final static ImageIcon SYSTEM_FOLDER_ICON = (ImageIcon) FileSystemView.getFileSystemView().getSystemIcon(new File(System.getProperty("user.home")).getParentFile());

	private FileSystemView fileSystemView;

	private JLabel label;

	public SafeFileRenderer()
	{
		label = new JLabel();
		label.setOpaque(true);
		fileSystemView = FileSystemView.getFileSystemView();
	}

	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus)
	{

		DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;

		if (node.getUserObject() instanceof String)
		{
			//label.setText((String) node.getUserObject());
			label.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage(SafeExplorer.class.getResource("/img/icons8-safe-16.png"))));
		} else
		{
			SafeFile safeFile = (SafeFile) node.getUserObject();
			if (safeFile != null)
			{
				if (safeFile.isFolder())
					label.setIcon(SYSTEM_FOLDER_ICON);
				else
					label.setIcon(null);

				label.setText(safeFile.getName());
				label.setToolTipText(safeFile.getPath());

			}

		}

		if (selected)
		{
			label.setBackground(backgroundSelectionColor);
			label.setForeground(textSelectionColor);
		} else
		{
			label.setBackground(backgroundNonSelectionColor);
			label.setForeground(textNonSelectionColor);
		}

		return label;
	}
}
