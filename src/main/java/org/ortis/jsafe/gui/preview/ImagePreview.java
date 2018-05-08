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
package org.ortis.jsafe.gui.preview;

import java.awt.AlphaComposite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

public class ImagePreview extends JPanel
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final BufferedImage image;

	public ImagePreview(final BufferedImage image)
	{
		this.image = image;

	}

	public void paint(final Graphics g)
	{
		super.paint(g);

		final BufferedImage display;
		if (image.getHeight() > getHeight() || image.getWidth() > getWidth())
		{

			final float ratio = ((float) image.getWidth()) / image.getHeight();
			final BufferedImage resizedImage;
			if (getWidth() >= getHeight())
			{

				final int maxHeight = Math.min((int) (getWidth() / ratio), getHeight());
				resizedImage = new BufferedImage((int) (maxHeight * ratio), maxHeight, this.image.getType());

			} else
			{

				final int maxWidth = Math.min((int) (getHeight() * ratio), getWidth());
				resizedImage = new BufferedImage(maxWidth, (int) (maxWidth / ratio), this.image.getType());
			}

			
			
			final Graphics2D g2 = resizedImage.createGraphics();

			g2.setComposite(AlphaComposite.Src);
			g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
			g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			
			g2.drawImage(image, 0, 0, resizedImage.getWidth(), resizedImage.getHeight(), null);
			g2.dispose();

			display = resizedImage;
		} else
			display = this.image;

		final int w = getWidth() / 2 - display.getWidth() / 2;
		final int h = getHeight() / 2 - display.getHeight() / 2;
		g.drawImage(display, w, h, null);

	}

}
