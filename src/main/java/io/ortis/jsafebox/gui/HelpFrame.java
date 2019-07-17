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

package io.ortis.jsafebox.gui;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

public class HelpFrame extends JFrame
{
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	private String helpContent;

	public HelpFrame(final Window parent) throws IOException
	{
		setTitle("Help");

		final Settings settings = Settings.getSettings();

		setBackground(settings.getUITheme().getBackgroundColor());
		this.setIconImages(settings.getHelpIcons());

		setPreferredSize(new Dimension(parent.getWidth() / 2, parent.getHeight()));
		setSize(getPreferredSize());
		setLocationRelativeTo(parent);

		JEditorPane textPane = new JEditorPane();
		textPane.setEditable(false);
		textPane.setOpaque(false);
		textPane.setContentType("text/html");

		if(helpContent == null)
		{
			final InputStream is = HelpFrame.class.getResourceAsStream("/html/help.html");

			ByteArrayOutputStream buffer = new ByteArrayOutputStream();
			int read;
			byte[] data = new byte[1024];
			while((read = is.read(data, 0, data.length)) > -1)
				buffer.write(data, 0, read);

			helpContent = new String(buffer.toByteArray(), Charset.forName("UTF-8"));
		}

		textPane.setText(this.helpContent);
		textPane.addHyperlinkListener(new HyperlinkListener()
		{
			@Override
			public void hyperlinkUpdate(HyperlinkEvent hle)
			{
				if(HyperlinkEvent.EventType.ACTIVATED.equals(hle.getEventType()))
				{
					System.out.println(hle.getURL());
					Desktop desktop = Desktop.getDesktop();
					try
					{
						desktop.browse(hle.getURL().toURI());
					} catch(Exception ex)
					{
						ex.printStackTrace();
					}
				}
			}
		});

		final JScrollPane scrollPane = new JScrollPane(textPane);
		getContentPane().add(scrollPane, BorderLayout.CENTER);

		textPane.setCaretPosition(0);
	}

}
