
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

package io.ortis.jsafebox.gui.old.viewers;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;

public class ImagePanel extends JScrollPane implements MouseListener, MouseMotionListener, MouseWheelListener

{

	private static final long serialVersionUID = 1L;

	private static final double MIN_SCALE = .01;
	private static final double MAX_SCALE = 100;

	private final BufferedImage image;
	private final JPanel canvas;
	private final Dimension preferedSize;
	private final JViewport viewport;

	private boolean init = false;

	private double scale = 1;
	private Point startPoint;

	public ImagePanel()
	{
		this(null);
	}

	public ImagePanel(final BufferedImage image)
	{

		setWheelScrollingEnabled(false);

		this.image = image;
		this.preferedSize = new Dimension(this.image.getWidth(), this.image.getHeight());

		this.canvas = new JPanel()
		{

			private static final long serialVersionUID = 1L;

			@Override
			protected void paintComponent(Graphics g)
			{
				super.paintComponent(g);
				draw(g);
			}

			@Override
			public Dimension getPreferredSize()
			{
				return preferedSize;
			}

		};

		this.canvas.addMouseListener(this);
		this.canvas.addMouseMotionListener(this);
		this.canvas.addMouseWheelListener(this);

		this.viewport = getViewport();
		this.viewport.add(this.canvas);

		setSize(Toolkit.getDefaultToolkit().getScreenSize().width / 2, Toolkit.getDefaultToolkit().getScreenSize().height * 2 / 3);
		setLocation(Toolkit.getDefaultToolkit().getScreenSize().width / 2 - getSize().width / 2, Toolkit.getDefaultToolkit().getScreenSize().height / 2 - getSize().height / 2);
	}

	private void draw(final Graphics g)
	{

		if (this.image == null)
			return;

		if (!this.init)
		{
			final Point center = new Point(this.viewport.getWidth() / 2, this.viewport.getHeight() / 2);

			if (this.image.getWidth() > this.image.getHeight())
			{
				final double initScale = ((double) this.viewport.getWidth()) / this.image.getWidth();
				scale(initScale, center);
			} else
			{
				final double initScale = ((double) this.viewport.getHeight()) / this.image.getHeight();
				scale(initScale, center);
			}

			this.init = true;
		}

		final Graphics2D g2 = (Graphics2D) g;
		final int w = this.canvas.getWidth();
		final int h = this.canvas.getHeight();

		final double imageWidth = this.scale * image.getWidth();
		final double imageHeight = this.scale * image.getHeight();
		final double x = (w - imageWidth) / 2;
		final double y = (h - imageHeight) / 2;

		final AffineTransform at = AffineTransform.getTranslateInstance(x, y);
		at.scale(this.scale, this.scale);
		g2.drawRenderedImage(this.image, at);
	}

	private void scale(double newScale, final Point target)
	{
		if (this.image == null)
			return;

		newScale = Math.min(MAX_SCALE, Math.max(MIN_SCALE, newScale));
		final double scaleRate = newScale / this.scale;
		final double scaleChange = scaleRate - 1;

		this.scale = newScale;
		this.preferedSize.setSize(this.scale * this.image.getWidth(), this.scale * this.image.getHeight());

		revalidate();
		repaint();

		final Point position = this.viewport.getViewPosition();
		final int newX = (int) (target.x * scaleChange + scaleRate * position.x);
		final int newY = (int) (target.y * scaleChange + scaleRate * position.y);

		final Point newPosition = new Point(newX, newY);
		this.viewport.setViewPosition(newPosition);

	}

	@Override
	public void mouseWheelMoved(final MouseWheelEvent e)
	{

		final Point position = e.getPoint();

		SwingUtilities.convertPointToScreen(position, e.getComponent());
		SwingUtilities.convertPointFromScreen(position, getParent());

		if (e.getWheelRotation() > 0)
			scale(this.scale * .9f, position);
		else if (e.getWheelRotation() < 0)
			scale(this.scale * 1.1f, position);

	}

	@Override
	public void mouseDragged(final MouseEvent e)
	{
		final int deltaX = this.startPoint.x - e.getX();
		final int deltaY = this.startPoint.y - e.getY();

		final Rectangle view = this.viewport.getViewRect();
		view.x += deltaX;
		view.y += deltaY;

		this.canvas.scrollRectToVisible(view);
	}

	@Override
	public void mouseMoved(final MouseEvent e)
	{
	}

	@Override
	public void mouseClicked(final MouseEvent e)
	{
	}

	@Override
	public void mousePressed(final MouseEvent e)
	{
		this.startPoint = new Point(e.getPoint());
	}

	@Override
	public void mouseReleased(final MouseEvent e)
	{
	}

	@Override
	public void mouseEntered(final MouseEvent e)
	{
	}

	@Override
	public void mouseExited(final MouseEvent e)
	{
	}

}
