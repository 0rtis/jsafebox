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

package org.ortis.jsafe.gui.preview;

import java.awt.BorderLayout;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class TextPreview extends JPanel
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JTextArea editArea;

	public TextPreview(final String text)
	{

		super(new BorderLayout());

		editArea = new JTextArea();
		editArea.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
		editArea.setEditable(false);
		editArea.setText(text);

		JScrollPane scrollingText = new JScrollPane(editArea);

		add(scrollingText, BorderLayout.CENTER);

	}
}
