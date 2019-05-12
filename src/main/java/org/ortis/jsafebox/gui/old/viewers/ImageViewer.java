/*******************************************************************************
 * Copyright 2018 Ortis (cao.ortis.org@gmail.com)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under the License.
 ******************************************************************************/

package org.ortis.jsafebox.gui.old.viewers;

import java.awt.BorderLayout;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.ortis.jsafebox.Block;
import org.ortis.jsafebox.Folder;
import org.ortis.jsafebox.Safe;
import org.ortis.jsafebox.SafeFile;
import org.ortis.jsafebox.gui.old.ErrorDialog;
import org.ortis.jsafebox.gui.old.SafeExplorer;

public class ImageViewer extends JFrame implements KeyListener
{

	private static final long serialVersionUID = 1L;

	private final String titleHeader;
	private final Safe safe;
	private final Folder folder;
	private final List<Block> images = new ArrayList<>();
	private int index = -1;

	private JPanel canvasPanel;
	private final Map<Block, BufferedImage> cache = new HashMap<>();

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

		int i = 0;
		for (final SafeFile sf : this.folder.listFiles())
		{
			if (!sf.isBlock())
				continue;

			final Block b = (Block)sf;
			final String mime = b.getProperties().get("content-type");

			if (mime == null || !mime.startsWith("image"))
				continue;

			if (sf.equals(block))
				this.index = i;

			this.images.add(b);
			i++;
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
		// this.canvasPanel.addKeyListener(this);
		main.add(this.canvasPanel, BorderLayout.CENTER);

		setContentPane(main);

		final List<Image> icons = new ArrayList<>();
		icons.add(Toolkit.getDefaultToolkit().getImage(SafeExplorer.class.getResource("/img/icons8-image-file-16.png")));
		icons.add(Toolkit.getDefaultToolkit().getImage(SafeExplorer.class.getResource("/img/icons8-image-file-32.png")));
		icons.add(Toolkit.getDefaultToolkit().getImage(SafeExplorer.class.getResource("/img/icons8-image-file-64.png")));
		icons.add(Toolkit.getDefaultToolkit().getImage(SafeExplorer.class.getResource("/img/icons8-image-file-100.png")));
		setIconImages(icons);

		setSize(Toolkit.getDefaultToolkit().getScreenSize().width / 2, Toolkit.getDefaultToolkit().getScreenSize().height * 2 / 3);
		setLocation(Toolkit.getDefaultToolkit().getScreenSize().width / 2 - getSize().width / 2, Toolkit.getDefaultToolkit().getScreenSize().height / 2 - getSize().height / 2);
		addKeyListener(this);
	}

	private void loadImage(final Block block)
	{
		try
		{

			BufferedImage img = this.cache.get(block);
			if (img == null)
			{
				final ByteArrayOutputStream baos = new ByteArrayOutputStream();
				safe.extract(block, baos);
				img = ImageIO.read(new ByteArrayInputStream(baos.toByteArray()));
				this.cache.put(block, img);
			}

			setTitle(titleHeader + block.getName());
			final ImagePanel canvas = new ImagePanel(img);
			canvasPanel.removeAll();
			canvasPanel.add(canvas, BorderLayout.CENTER);
			canvasPanel.revalidate();
			canvas.repaint();
		} catch (final Exception e)
		{
			new ErrorDialog(this, "Error while opening loading image", e).setVisible(true);
		}
	}

	@Override
	public void keyTyped(final KeyEvent e)
	{

	}

	@Override
	public void keyPressed(final KeyEvent e)
	{
		if (e.getKeyCode() == KeyEvent.VK_RIGHT)
		{
			

			this.index++;
			if (this.index >= this.images.size())
				this.index = 0;
			loadImage(this.images.get(this.index));

		} else if (e.getKeyCode() == KeyEvent.VK_LEFT)
		{
			

			this.index--;
			if (this.index < 0)
				this.index = this.images.size() - 1;
			loadImage(this.images.get(this.index));
		}
	}

	@Override
	public void keyReleased(KeyEvent e)
	{
	}

}
