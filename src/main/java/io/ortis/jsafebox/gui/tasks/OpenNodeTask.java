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

package io.ortis.jsafebox.gui.tasks;

import io.ortis.jsafebox.Block;
import io.ortis.jsafebox.SafeFile;
import io.ortis.jsafebox.gui.FileType;
import io.ortis.jsafebox.gui.SafeboxFrame;
import io.ortis.jsafebox.gui.Settings;
import io.ortis.jsafebox.gui.tree.SafeFileTreeNode;
import io.ortis.jsafebox.gui.viewers.ImageViewer;
import io.ortis.jsafebox.gui.viewers.TextViewer;

import java.awt.*;
import java.util.logging.Logger;

/**
 * @author Ortis
 */
public class OpenNodeTask extends AbstractGUITask
{
	private final SafeFileTreeNode node;
	private final SafeboxFrame safeboxFrame;

	private Window window;

	public OpenNodeTask(final SafeFileTreeNode node, final SafeboxFrame safeboxFrame, final Logger log)
	{
		super("Loading node", "Success", "Node has been successfully loaded !", log);

		this.node = node;
		this.safeboxFrame = safeboxFrame;
	}

	@Override
	public boolean skipResultOnSuccess()
	{
		return true;
	}

	@Override
	public void task() throws Exception
	{
		try
		{
			SafeFile file = (SafeFile) node.getUserObject();

			if(file.isBlock())
			{
				final Block block = (Block) file;

				log.info("Reading block mime");
				final String mime = block.getProperties().get(Block.MIME_LABEL);

				final Settings settings = Settings.getSettings();
				final FileType fileType = settings.getFileType(mime);

				switch(fileType)
				{
					case Image:
					{
						log.info("Loading image...");
						final ImageViewer viewer = new ImageViewer(this.safeboxFrame.getSafe(), block, this.safeboxFrame.getTitle() + " - ");
						viewer.addWindowListener(this.safeboxFrame);
						viewer.setVisible(true);
						viewer.toFront();

						this.window = viewer;
					}
					break;


					case Text:
					{
						log.info("Loading text...");
						final TextViewer viewer = new TextViewer(this.safeboxFrame.getjTree(), node);
						viewer.setTitle(this.safeboxFrame.getTitle() + " - " + block.getPath());
						viewer.addWindowListener(this.safeboxFrame);
						viewer.setVisible(true);
						viewer.toFront();

						this.window = viewer;
					}
					break;

					default:
						break;
				}
			}

		} finally
		{

		}
	}


	public Window getWindow()
	{
		return window;
	}
}
