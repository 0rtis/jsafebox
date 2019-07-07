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
package io.ortis.jsafebox.gui.old.previews;

import java.awt.BorderLayout;
import java.awt.Font;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import io.ortis.jsafebox.Utils;

public class ErrorPreview extends JPanel
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ErrorPreview(final Exception exception)
	{
		super(new BorderLayout());

		JLabel lblNewLabel = new JLabel("");
		lblNewLabel.setIcon(new ImageIcon(ErrorPreview.class.getResource("/img/icons8-high-priority-filled-50.png")));
		add(lblNewLabel, BorderLayout.WEST);

		JTextPane textPane = new JTextPane();
		textPane.setFont(new Font("Segoe UI", Font.PLAIN, 14));
		textPane.setOpaque(false);
		textPane.setEditable(false);
		final String msg = Utils.formatException(exception);
		textPane.setText(msg);
		textPane.setBorder(new EmptyBorder(0, 0, 0, 0));

		JScrollPane scrollPane = new JScrollPane(textPane);
		scrollPane.setBorder(new EmptyBorder(50, 30, 0, 0));

		add(scrollPane, BorderLayout.CENTER);

		JLabel lblNewLabel_1 = new JLabel("Error");
		lblNewLabel_1.setFont(new Font("Segoe UI", Font.BOLD, 15));
		lblNewLabel_1.setHorizontalAlignment(SwingConstants.CENTER);
		add(lblNewLabel_1, BorderLayout.NORTH);
	}

}
