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


	private final static ImageIcon ROOT_ICON = new ImageIcon(
			Toolkit.getDefaultToolkit().getImage(SafeFileTreeCellRenderer.class.getResource("/img/frame-icons/safe" + "-filled-16.png")));
	private final static ImageIcon SYSTEM_FOLDER_ICON = (ImageIcon) FileSystemView.getFileSystemView().getSystemIcon(
			new File(System.getProperty("user.home")).getParentFile());

	private final static ImageIcon UNKNOWN_ICON = new ImageIcon(
			Toolkit.getDefaultToolkit().getImage(SafeFileTreeCellRenderer.class.getResource("/img/binary-file-16.png")));
	private final static ImageIcon TEXT_ICON = new ImageIcon(Toolkit.getDefaultToolkit().getImage(SafeFileTreeCellRenderer.class.getResource("/img/txt-file-16.png")));
	private final static ImageIcon IMAGE_ICON = new ImageIcon(Toolkit.getDefaultToolkit().getImage(SafeFileTreeCellRenderer.class.getResource("/img/image-file-16.png")));
	private final static ImageIcon AUDIO_ICON = new ImageIcon(Toolkit.getDefaultToolkit().getImage(SafeFileTreeCellRenderer.class.getResource("/img/audio-file-16.png")));
	private final static ImageIcon VIDEO_ICON = new ImageIcon(Toolkit.getDefaultToolkit().getImage(SafeFileTreeCellRenderer.class.getResource("/img/video-file-16.png")));


	private final Color selectedBackground;
	private final Color nonSelectedBackground;

	public SafeFileTreeCellRenderer()
	{
		this.nonSelectedBackground = Settings.getSettings().getUITheme().getLeftPanelBackgroundColor();
		this.selectedBackground = new Color(backgroundSelectionColor.getRed(), backgroundSelectionColor.getGreen(), backgroundSelectionColor.getBlue());
	}

	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus)
	{
		final JLabel label = this;//(JLabel) super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);

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

			label.setIcon(ROOT_ICON);
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

		label.setOpaque(true);

		if(selected)
		{
			if(foregroundColor == null)
				foregroundColor = textSelectionColor;
		}
		else
		{
			if(foregroundColor == null)
				foregroundColor = textNonSelectionColor;
		}

		label.setForeground(foregroundColor);

		if(selected)
			//label.setBackground(Settings.getSettings().getUITheme().getButtonFirstColorMouseOver());
			label.setBackground(selectedBackground);
		else
			label.setBackground(nonSelectedBackground);

		//	label.setBackground();

		return label;
	}

}
