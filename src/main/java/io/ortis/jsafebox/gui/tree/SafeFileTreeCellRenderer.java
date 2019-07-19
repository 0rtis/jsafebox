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

import io.ortis.jsafebox.Block;
import io.ortis.jsafebox.SafeFile;
import io.ortis.jsafebox.gui.FileType;
import io.ortis.jsafebox.gui.Settings;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.*;
import java.io.File;

public class SafeFileTreeCellRenderer extends DefaultTreeCellRenderer
{

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	private final static ImageIcon SYSTEM_FOLDER_ICON = (ImageIcon) FileSystemView.getFileSystemView().getSystemIcon(
			new File(System.getProperty("user.home")).getParentFile());

	private final static ImageIcon UNKNOWN_ICON =
			new ImageIcon(Toolkit.getDefaultToolkit().getImage(SafeFileTreeCellRenderer.class.getResource("/img/binary-file-16.png")));
	private final static ImageIcon TEXT_ICON = new ImageIcon(Toolkit.getDefaultToolkit().getImage(SafeFileTreeCellRenderer.class.getResource("/img/txt-file-16.png")));
	private final static ImageIcon IMAGE_ICON = new ImageIcon(Toolkit.getDefaultToolkit().getImage(SafeFileTreeCellRenderer.class.getResource("/img/image-file-16.png")));
	private final static ImageIcon AUDIO_ICON = new ImageIcon(Toolkit.getDefaultToolkit().getImage(SafeFileTreeCellRenderer.class.getResource("/img/audio-file-16.png")));
	private final static ImageIcon VIDEO_ICON = new ImageIcon(Toolkit.getDefaultToolkit().getImage(SafeFileTreeCellRenderer.class.getResource("/img/video-file-16.png")));


	public SafeFileTreeCellRenderer()
	{

	}

	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus)
	{
		final JLabel label = this;//(JLabel) super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
		label.setOpaque(true);
		label.setBackground(Color.GREEN);

		if(!(value instanceof SafeFileTreeNode))
			return label;

		SafeFileTreeNode node = (SafeFileTreeNode) value;
		final SafeTreeModel model = (SafeTreeModel) tree.getModel();

		Color foregroundColor = null;

		if(node.isSafeRoot())
		{
			if(model.getSafeboxFrame().isModificationPending())
				label.setText("Safe (*) ");
			else
				label.setText("Safe");

			label.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage(SafeFileTreeCellRenderer.class.getResource("/img/frame-icons/safe-filled-32.png"))));
		}
		else
		{
			SafeFile safeFile = node.getSafeFile();
			if(safeFile != null)
			{
				if(safeFile.isFolder())
					label.setIcon(SYSTEM_FOLDER_ICON);
				else
				{
					final Block block = (Block) safeFile;
					final String mime = block.getProperties().get(Block.MIME_LABEL);
					final FileType fileType = Settings.getSettings().getFileType(mime);
					switch(fileType)
					{
						case Text:
							label.setIcon(TEXT_ICON);
							break;

						case Image:
							label.setIcon(IMAGE_ICON);
							break;

						case Audio:
							label.setIcon(AUDIO_ICON);
							break;

						case Video:
							label.setIcon(VIDEO_ICON);
							break;

						default:
							label.setIcon(UNKNOWN_ICON);
							break;
					}
				}

				switch(node.getStatus())
				{
					case Added:
						label.setText("+ " + safeFile.getName());
						foregroundColor = Settings.getSettings().getUITheme().pendingAddColor();
						break;

					case Deleted:
						label.setText("- " + safeFile.getName());
						foregroundColor = Settings.getSettings().getUITheme().pendingDeleteColor();
						break;

					case Updated:
						label.setText("* " + safeFile.getName());
						foregroundColor = Settings.getSettings().getUITheme().pendingUpdateColor();
						break;

					case Unchanged:
					default:
						label.setText(safeFile.getName());
						break;

				}


				label.setToolTipText(safeFile.getPath());
			}
		}

		label.setOpaque(false);

		if(selected)
		{
			label.setBackground(backgroundSelectionColor);

			if(foregroundColor == null)
				foregroundColor = textSelectionColor;
		}
		else
		{
			label.setBackground(backgroundNonSelectionColor);

			if(foregroundColor == null)
				foregroundColor = textNonSelectionColor;
		}

		label.setForeground(foregroundColor);

		return label;
	}

	@Override
	public Color getBackground()
	{
		return Settings.getSettings().getUITheme().getLeftPanelBackgroundColor();
	}
}
