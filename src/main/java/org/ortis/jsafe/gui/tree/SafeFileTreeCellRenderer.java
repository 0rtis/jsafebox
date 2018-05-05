
package org.ortis.jsafe.gui.tree;

import java.awt.Component;
import java.awt.Toolkit;
import java.io.File;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.filechooser.FileSystemView;
import javax.swing.tree.DefaultTreeCellRenderer;

import org.ortis.jsafe.Block;
import org.ortis.jsafe.SafeFile;
import org.ortis.jsafe.gui.SafeExplorer;

public class SafeFileTreeCellRenderer extends DefaultTreeCellRenderer
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final static ImageIcon SYSTEM_FOLDER_ICON = (ImageIcon) FileSystemView.getFileSystemView().getSystemIcon(new File(System.getProperty("user.home")).getParentFile());

	private final static ImageIcon UNKNOWN_ICON = new ImageIcon(Toolkit.getDefaultToolkit().getImage(SafeExplorer.class.getResource("/img/icons8-document-16.png")));
	private final static ImageIcon TEXT_ICON = new ImageIcon(Toolkit.getDefaultToolkit().getImage(SafeExplorer.class.getResource("/img/icons8-txt-16.png")));
	private final static ImageIcon IMAGE_ICON = new ImageIcon(Toolkit.getDefaultToolkit().getImage(SafeExplorer.class.getResource("/img/icons8-image-file-16.png")));
	private final static ImageIcon AUDIO_ICON = new ImageIcon(Toolkit.getDefaultToolkit().getImage(SafeExplorer.class.getResource("/img/icons8-audio-file-16.png")));
	private final static ImageIcon VIDEO_ICON = new ImageIcon(Toolkit.getDefaultToolkit().getImage(SafeExplorer.class.getResource("/img/icons8-video-file-16.png")));

	private JLabel label;

	public SafeFileTreeCellRenderer()
	{
		label = new JLabel();
		label.setOpaque(true);
	}

	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus)
	{

		if (!(value instanceof SafeFileTreeNode))
			return label;

		SafeFileTreeNode node = (SafeFileTreeNode) value;

		if (node.isSafeRoot())
		{

			final SafeTreeModel model = (SafeTreeModel) tree.getModel();
			if (model.getSafeExplorer().isModificationPending())
			{
				label.setText("Safe (* Modification pending) ");
			} else
				label.setText("Safe");

			label.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage(SafeExplorer.class.getResource("/img/icons8-safe-16.png"))));
		} else
		{
			SafeFile safeFile = node.getSafeFile();
			if (safeFile != null)
			{
				if (safeFile.isFolder())
					label.setIcon(SYSTEM_FOLDER_ICON);
				else
				{
					final Block block = (Block) safeFile;
					final String mime = block.getProperties().get("content-type");
					if (mime == null)
						label.setIcon(UNKNOWN_ICON);
					else if (mime.startsWith("text"))
						label.setIcon(TEXT_ICON);
					else if (mime.startsWith("image"))
						label.setIcon(IMAGE_ICON);
					else if (mime.startsWith("audio"))
						label.setIcon(AUDIO_ICON);
					else if (mime.startsWith("video"))
						label.setIcon(VIDEO_ICON);
					else
						label.setIcon(UNKNOWN_ICON);

				}

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
