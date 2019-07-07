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
package io.ortis.jsafebox.gui.old.viewers;

import java.awt.BorderLayout;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import io.ortis.jsafebox.gui.old.SafeExplorer;

public class TextViewer extends JFrame implements ActionListener
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JTextArea textArea;
	private JRadioButtonMenuItem wordWrapMenuItem;

	/**
	 * Create the application.
	 */
	public TextViewer(final String text)
	{

		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		initialize();
		this.textArea.setText(text);
		this.textArea.setCaretPosition(0);
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize()
	{

		final JPanel main = new JPanel(new BorderLayout());
		textArea = new JTextArea();
		this.textArea.setEditable(false);

		// textArea.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));

		JScrollPane scrollPane = new JScrollPane(textArea);

		main.add(scrollPane, BorderLayout.CENTER);

		setContentPane(main);

		JMenuBar menuBar = new JMenuBar();
		main.add(menuBar, BorderLayout.NORTH);

		JMenu mnNewMenu = new JMenu("View");

		menuBar.add(mnNewMenu);

		wordWrapMenuItem = new JRadioButtonMenuItem("Line wrap");
		wordWrapMenuItem.addActionListener(this);
		mnNewMenu.add(wordWrapMenuItem);

		
		final List<Image> icons = new ArrayList<>();
		icons.add(Toolkit.getDefaultToolkit().getImage(SafeExplorer.class.getResource("/img/icons8-txt-16.png")));
		icons.add(Toolkit.getDefaultToolkit().getImage(SafeExplorer.class.getResource("/img/icons8-txt-32.png")));
		icons.add(Toolkit.getDefaultToolkit().getImage(SafeExplorer.class.getResource("/img/icons8-txt-64.png")));
		icons.add(Toolkit.getDefaultToolkit().getImage(SafeExplorer.class.getResource("/img/icons8-txt-100.png")));
		setIconImages(icons);
		
		setSize(Toolkit.getDefaultToolkit().getScreenSize().width / 2, Toolkit.getDefaultToolkit().getScreenSize().height * 2 / 3);
		setLocation(Toolkit.getDefaultToolkit().getScreenSize().width / 2 - getSize().width / 2, Toolkit.getDefaultToolkit().getScreenSize().height / 2 - getSize().height / 2);

	}

	@Override
	public void actionPerformed(final ActionEvent event)
	{

		if (event.getActionCommand().equals(wordWrapMenuItem.getActionCommand()))
		{
			this.textArea.setLineWrap(wordWrapMenuItem.isSelected());
		}
	}

}
