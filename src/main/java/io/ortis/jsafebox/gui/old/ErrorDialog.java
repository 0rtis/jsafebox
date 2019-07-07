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

package io.ortis.jsafebox.gui.old;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import io.ortis.jsafebox.gui.old.previews.ErrorPreview;
import io.ortis.jsafebox.Utils;

public class ErrorDialog extends JDialog
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ErrorDialog(final JFrame parentFrame, final String message, final Exception exception)
	{

		super(parentFrame, "Error", true);

		JLabel lblNewLabel = new JLabel("");
		lblNewLabel.setIcon(new ImageIcon(ErrorPreview.class.getResource("/img/icons8-high-priority-filled-50.png")));
		add(lblNewLabel, BorderLayout.WEST);

		JTextPane textPane = new JTextPane();
		textPane.setFont(new Font("Segoe UI", Font.PLAIN, 14));
		textPane.setOpaque(false);
		textPane.setEditable(false);
		final String msg = (message == null ? "" : message) + "\n" + Utils.formatException(exception);
		textPane.setText(msg);
		textPane.setBorder(new EmptyBorder(0, 0, 0, 0));

		JScrollPane scrollPane = new JScrollPane(textPane);
		scrollPane.setBorder(new EmptyBorder(50, 30, 0, 0));
		textPane.setCaretPosition(0);
		add(scrollPane, BorderLayout.CENTER);

		JLabel lblNewLabel_1 = new JLabel("Error");
		lblNewLabel_1.setFont(new Font("Segoe UI", Font.BOLD, 15));
		lblNewLabel_1.setHorizontalAlignment(SwingConstants.CENTER);
		add(lblNewLabel_1, BorderLayout.NORTH);

		addKeyListener(new KeyAdapter()
		{
			public void keyPressed(KeyEvent ke)
			{ // handler
				if (ke.getKeyCode() == KeyEvent.VK_ESCAPE)
					ErrorDialog.this.dispose();
			}
		});

		addWindowListener(new WindowListener()
		{

			@Override
			public void windowOpened(WindowEvent e)
			{
				ErrorDialog.this.requestFocus();
			}

			@Override
			public void windowIconified(WindowEvent e)
			{
			}

			@Override
			public void windowDeiconified(WindowEvent e)
			{
			}

			@Override
			public void windowDeactivated(WindowEvent e)
			{
			}

			@Override
			public void windowClosing(WindowEvent e)
			{
			}

			@Override
			public void windowClosed(WindowEvent e)
			{
			}

			@Override
			public void windowActivated(WindowEvent e)
			{
			}
		});

		setPreferredSize(new Dimension(400, 400));
		setMaximumSize(new Dimension(parentFrame.getWidth() * 2 / 3, parentFrame.getHeight() * 2 / 3));
		setMinimumSize(new Dimension(parentFrame.getWidth() / 2, parentFrame.getHeight() / 2));
		setSize(getPreferredSize());
		setLocationRelativeTo(parentFrame);
		
		final List<Image> icons = new ArrayList<>();
		icons.add(Toolkit.getDefaultToolkit().getImage(SafeExplorer.class.getResource("/img/icons8-high-priority-filled-16.png")));
		icons.add(Toolkit.getDefaultToolkit().getImage(SafeExplorer.class.getResource("/img/icons8-high-priority-filled-32.png")));
		icons.add(Toolkit.getDefaultToolkit().getImage(SafeExplorer.class.getResource("/img/icons8-high-priority-filled-64.png")));
		icons.add(Toolkit.getDefaultToolkit().getImage(SafeExplorer.class.getResource("/img/icons8-high-priority-filled-100.png")));
		this.setIconImages(icons);
	}

}
