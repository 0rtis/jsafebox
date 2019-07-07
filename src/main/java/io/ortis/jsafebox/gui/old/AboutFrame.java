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
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Toolkit;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

public class AboutFrame extends JFrame
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String aboutContent;

	public AboutFrame(final SafeExplorer safeExplorer) throws IOException
	{

		setTitle("About");
		final JFrame parentFrame = safeExplorer.getExplorerFrame();

		setPreferredSize(new Dimension(parentFrame.getWidth() / 2, parentFrame.getHeight()*2/3));
		setSize(getPreferredSize());
		setResizable(true);
		// setSize(new Dimension(400, 400));

		setLocationRelativeTo(parentFrame);

		JEditorPane textPane = new JEditorPane();
		textPane.setEditable(false);
		textPane.setOpaque(false);
		textPane.setContentType("text/html");

		if (aboutContent == null)
		{
			final InputStream is = AboutFrame.class.getResourceAsStream("/html/about.html");

			ByteArrayOutputStream buffer = new ByteArrayOutputStream();
			int read;
			byte [] data = new byte[1024];
			while ((read = is.read(data, 0, data.length)) > -1)
				buffer.write(data, 0, read);

			aboutContent = new String(buffer.toByteArray(), Charset.forName("UTF-8"));
		}

		textPane.setText(this.aboutContent);
		textPane.addHyperlinkListener(new HyperlinkListener()
		{
			@Override
			public void hyperlinkUpdate(HyperlinkEvent hle)
			{
				if (HyperlinkEvent.EventType.ACTIVATED.equals(hle.getEventType()))
				{
					System.out.println(hle.getURL());
					Desktop desktop = Desktop.getDesktop();
					try
					{
						desktop.browse(hle.getURL().toURI());
					} catch (Exception ex)
					{
						ex.printStackTrace();
					}
				}
			}
		});

		
		final JScrollPane scrollPane = new JScrollPane(textPane);
		getContentPane().add(scrollPane, BorderLayout.CENTER);

		
		final List<Image> icons = new ArrayList<>();
		icons.add(Toolkit.getDefaultToolkit().getImage(SafeExplorer.class.getResource("/img/frame-icons/icons8-safe-16.png")));
		icons.add(Toolkit.getDefaultToolkit().getImage(SafeExplorer.class.getResource("/img/frame-icons/icons8-safe-32.png")));
		icons.add(Toolkit.getDefaultToolkit().getImage(SafeExplorer.class.getResource("/img/frame-icons/icons8-safe-64.png")));
		icons.add(Toolkit.getDefaultToolkit().getImage(SafeExplorer.class.getResource("/img/frame-icons/icons8-safe-100.png")));
		this.setIconImages(icons);
		
		textPane.setCaretPosition(0);
	}

}
