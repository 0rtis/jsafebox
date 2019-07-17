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

package io.ortis.jsafebox.gui.viewers;

import io.ortis.jsafebox.Block;
import io.ortis.jsafebox.Folder;
import io.ortis.jsafebox.Safe;
import io.ortis.jsafebox.SafeFile;
import io.ortis.jsafebox.gui.FileType;
import io.ortis.jsafebox.gui.GUI;
import io.ortis.jsafebox.gui.ResultFrame;
import io.ortis.jsafebox.gui.Settings;
import io.ortis.jsafebox.gui.tasks.ExceptionTask;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Ortis
 */
public class ImageViewer extends JFrame implements KeyListener
{
	private static final long serialVersionUID = 1L;

	private final String titleHeader;
	private final Safe safe;
	private final Folder folder;
	private final List<Block> images = new ArrayList<>();
	private final Map<Block, BufferedImage> cache = new HashMap<>();
	private int index = -1;
	private JPanel canvasPanel;

	/**
	 * Create the application.
	 */
	public ImageViewer(final Safe safe, final Block block, final String titleHeader) throws Exception
	{
		this.safe = safe;
		this.folder = block.getParent();
		this.titleHeader = titleHeader;
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		initialize();

		final Settings settings = Settings.getSettings();

		int i = 0;
		for(final SafeFile sf : this.folder.listFiles())
		{
			if(!sf.isBlock())
				continue;

			final Block b = (Block) sf;

			final String mime = b.getProperties().get(Block.MIME_LABEL);
			final FileType fileType = settings.getFileType(mime);

			if(fileType != FileType.Image)
				continue;

			if(sf.equals(block))
				this.index = i;

			this.images.add(b);
			i++;
		}

		if(this.index < 0)
			for(final SafeFile sf : safe.getDeleted(new ArrayList<>()))
			{
				if(!sf.equals(block))
					continue;

				final Block b = (Block) sf;
				this.index = i;
				this.images.add(b);

				break;
			}

		loadImage(this.images.get(this.index));
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize()
	{

		final JPanel main = new JPanel(new BorderLayout());
		this.canvasPanel = new JPanel(new BorderLayout());
		main.add(this.canvasPanel, BorderLayout.CENTER);

		setContentPane(main);

		setIconImages(Settings.getSettings().getImageFrameIcons());

		setSize(Toolkit.getDefaultToolkit().getScreenSize().width / 2, Toolkit.getDefaultToolkit().getScreenSize().height * 2 / 3);
		setLocation(Toolkit.getDefaultToolkit().getScreenSize().width / 2 - getSize().width / 2,
				Toolkit.getDefaultToolkit().getScreenSize().height / 2 - getSize().height / 2);
		addKeyListener(this);
	}

	private void loadImage(final Block block)
	{
		try
		{
			BufferedImage img = this.cache.get(block);
			if(img == null)
			{
				final ByteArrayOutputStream baos = new ByteArrayOutputStream();
				safe.extract(block,true, baos);
				img = ImageIO.read(new ByteArrayInputStream(baos.toByteArray()));
				this.cache.put(block, img);
			}

			setTitle(titleHeader + block.getName());
			final ImagePanel canvas = new ImagePanel(img);
			canvasPanel.removeAll();
			canvasPanel.add(canvas, BorderLayout.CENTER);
			canvasPanel.revalidate();
			canvas.repaint();
		} catch(final Exception e)
		{
			new ResultFrame(this, new ExceptionTask(e, GUI.getLogger()));
		}
	}

	@Override
	public void keyTyped(final KeyEvent e)
	{

	}

	@Override
	public void keyPressed(final KeyEvent e)
	{
		if(e.getKeyCode() == KeyEvent.VK_RIGHT)
		{


			this.index++;
			if(this.index >= this.images.size())
				this.index = 0;
			loadImage(this.images.get(this.index));

		}
		else if(e.getKeyCode() == KeyEvent.VK_LEFT)
		{


			this.index--;
			if(this.index < 0)
				this.index = this.images.size() - 1;
			loadImage(this.images.get(this.index));
		}
	}

	@Override
	public void keyReleased(KeyEvent e)
	{
	}

}
