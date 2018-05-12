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

package org.ortis.jsafe.gui;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileSystemView;

import org.ortis.jsafe.gui.tasks.InitTask;
import org.ortis.jsafe.gui.tasks.OpenTask;

public class LoginFrame implements ActionListener
{

	private final Configuration configuration;
	private JFrame frame;

	private JComboBox comboBox;
	private JButton newButton;
	private JButton browseButton;
	private JButton openButton;

	/**
	 * Launch the application.
	 */
	public static void main(String [] args)
	{

		EventQueue.invokeLater(new Runnable()
		{
			public void run()
			{

				try
				{
					LoginFrame window = new LoginFrame();
					window.show();

				} catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public LoginFrame()
	{
		try
		{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (final Exception e)
		{
		}

		this.configuration = new Configuration();

		final File confFile = new File(SafeExplorer.CONFIG_FILE);
		if (confFile.exists())
			try
			{
				this.configuration.load(new FileInputStream(confFile));
			} catch (final IOException e)
			{
				e.printStackTrace();
			}

		initialize();
	}

	public void show()
	{
		this.frame.setVisible(true);

		focusTextField();

	}

	public void focusTextField()
	{
		final JTextField field = (JTextField) comboBox.getEditor().getEditorComponent();
		field.requestFocus();
		field.selectAll();

	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize()
	{
		frame = new JFrame();
		final JPanel main = new JPanel();
		main.setBackground(Color.WHITE);
		main.setBorder(new EmptyBorder(5, 5, 5, 5));

		frame.setContentPane(main);

		final List<String> paths = this.configuration.getSafeFilePaths();
		final String [] pastPaths = paths.toArray(new String[paths.size()]);

		comboBox = new JComboBox(pastPaths);
		comboBox.setEditable(true);

		final JTextField field = (JTextField) comboBox.getEditor().getEditorComponent();
		if (comboBox.getItemCount() == 0)
		{
			field.setText("Select a safe or create a new one");
		} else
			field.setText(pastPaths[0]);

		field.addKeyListener(new KeyListener()
		{

			@Override
			public void keyTyped(final KeyEvent e)
			{
				if (e.getKeyChar() == '\n')
					openButton.doClick();
			}

			@Override
			public void keyReleased(KeyEvent e)
			{
			}

			@Override
			public void keyPressed(KeyEvent e)
			{
			}
		});

		browseButton = new JButton("Browse");
		browseButton.addActionListener(this);

		JLabel lblNewLabel_1 = new JLabel("JSafe");
		lblNewLabel_1.setFont(new Font("Segoe UI", Font.PLAIN, 17));
		lblNewLabel_1.setIcon(new ImageIcon(LoginFrame.class.getResource("/img/icons8-safe-64.png")));

		newButton = new JButton("New");
		newButton.addActionListener(this);

		openButton = new JButton("Open");
		openButton.addActionListener(this);
		GroupLayout gl_main = new GroupLayout(main);
		gl_main.setHorizontalGroup(
			gl_main.createParallelGroup(Alignment.TRAILING)
				.addGroup(gl_main.createSequentialGroup()
					.addGroup(gl_main.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_main.createSequentialGroup()
							.addContainerGap()
							.addComponent(comboBox, 0, 335, Short.MAX_VALUE)
							.addGap(18))
						.addGroup(gl_main.createSequentialGroup()
							.addComponent(lblNewLabel_1, GroupLayout.PREFERRED_SIZE, 292, GroupLayout.PREFERRED_SIZE)
							.addGap(73)))
					.addGap(6)
					.addGroup(gl_main.createParallelGroup(Alignment.TRAILING)
						.addComponent(browseButton, GroupLayout.PREFERRED_SIZE, 87, GroupLayout.PREFERRED_SIZE)
						.addComponent(openButton, GroupLayout.PREFERRED_SIZE, 87, GroupLayout.PREFERRED_SIZE)
						.addComponent(newButton, GroupLayout.DEFAULT_SIZE, 87, Short.MAX_VALUE))
					.addContainerGap())
		);
		gl_main.setVerticalGroup(
			gl_main.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_main.createSequentialGroup()
					.addGroup(gl_main.createParallelGroup(Alignment.TRAILING)
						.addGroup(gl_main.createSequentialGroup()
							.addComponent(newButton, GroupLayout.PREFERRED_SIZE, 29, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.UNRELATED)
							.addComponent(browseButton, GroupLayout.PREFERRED_SIZE, 29, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.UNRELATED)
							.addComponent(openButton, GroupLayout.PREFERRED_SIZE, 29, GroupLayout.PREFERRED_SIZE))
						.addGroup(gl_main.createSequentialGroup()
							.addComponent(lblNewLabel_1)
							.addGap(45)
							.addComponent(comboBox, GroupLayout.PREFERRED_SIZE, 29, GroupLayout.PREFERRED_SIZE)))
					.addGap(93))
		);
		main.setLayout(gl_main);

		final List<Image> icons = new ArrayList<>();
		icons.add(Toolkit.getDefaultToolkit().getImage(SafeExplorer.class.getResource("/img/icons8-safe-16.png")));
		icons.add(Toolkit.getDefaultToolkit().getImage(SafeExplorer.class.getResource("/img/icons8-safe-32.png")));
		icons.add(Toolkit.getDefaultToolkit().getImage(SafeExplorer.class.getResource("/img/icons8-safe-64.png")));
		frame.setIconImages(icons);

		frame.setSize(Toolkit.getDefaultToolkit().getScreenSize().width * 1 / 4, Toolkit.getDefaultToolkit().getScreenSize().height * 1 / 5);
		frame.setResizable(false);
		frame.setLocation(Toolkit.getDefaultToolkit().getScreenSize().width / 2 - frame.getSize().width / 2, Toolkit.getDefaultToolkit().getScreenSize().height / 2 - frame.getSize().height / 2);

		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	@Override
	public void actionPerformed(final ActionEvent event)
	{

		if (event.getActionCommand().equals(this.newButton.getActionCommand()))
		{
			final List<String> previousPaths = this.configuration.getSafeFilePaths();
			File directory = previousPaths.isEmpty() ? null : new File(previousPaths.get(0));
			if (directory != null)
			{
				if (!directory.exists() || !directory.isDirectory())
					directory = directory.getParentFile();

			}

			if (directory == null || !directory.exists() || !directory.isDirectory())
				directory = FileSystemView.getFileSystemView().getHomeDirectory();

			JFileChooser jfc = new JFileChooser(directory);
			jfc.setDialogTitle("Select a destination for the safe");
			jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);

			File destination = null;
			int returnValue = jfc.showSaveDialog(this.frame);
			if (returnValue == JFileChooser.APPROVE_OPTION)
				destination = jfc.getSelectedFile();

			if (destination != null)
			{

				final JPasswordField pwd = new JPasswordField(10);
				final JOptionPane optionPane = new JOptionPane(pwd, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
				JDialog dialog = optionPane.createDialog(this.frame, "Enter Password");
				dialog.addComponentListener(new ComponentListener()
				{

					@Override
					public void componentShown(final ComponentEvent e)
					{
						pwd.requestFocusInWindow();
					}

					@Override
					public void componentHidden(ComponentEvent e)
					{
					}

					@Override
					public void componentResized(ComponentEvent e)
					{
					}

					@Override
					public void componentMoved(ComponentEvent e)
					{
					}
				});
				dialog.setVisible(true);

				Integer action = (Integer) optionPane.getValue();

				if (action == null)
					return;

				if (action == 0)
				{
					final char [] pwd1 = pwd.getPassword();
					pwd.setText("");

					dialog = optionPane.createDialog(this.frame, "Enter Password");
					dialog.addComponentListener(new ComponentListener()
					{

						@Override
						public void componentShown(final ComponentEvent e)
						{
							pwd.requestFocusInWindow();
						}

						@Override
						public void componentHidden(ComponentEvent e)
						{
						}

						@Override
						public void componentResized(ComponentEvent e)
						{
						}

						@Override
						public void componentMoved(ComponentEvent e)
						{
						}
					});
					dialog.setVisible(true);

					action = (Integer) optionPane.getValue();

					if (action == null)
						return;

					action = (int) optionPane.getValue();
					if (action == 0)
					{

						final char [] pwd2 = pwd.getPassword();
						pwd.setText("");

						if (Arrays.equals(pwd1, pwd2))
						{
							this.configuration.addSafeFilePath(destination.getAbsolutePath());
							final ProgressDialog pd = new ProgressDialog(this.frame);
							pd.setTitle("Creating safe...");
							final InitTask initTask = new InitTask(destination, pwd1, this);
							pd.monitor(initTask, "Creating safe...");

							focusTextField();
						} else
							new ErrorDialog(this.frame, "Passwords does not match", null).setVisible(true);
					}
				}

			}

		} else if (event.getActionCommand().equals(this.browseButton.getActionCommand()))
		{
			final List<String> previousPaths = this.configuration.getSafeFilePaths();
			File directory = previousPaths.isEmpty() ? null : new File(previousPaths.get(0));
			if (directory != null)
			{
				if (!directory.exists() || !directory.isDirectory())
					directory = directory.getParentFile();

			}

			if (directory == null || !directory.exists() || !directory.isDirectory())
				directory = FileSystemView.getFileSystemView().getHomeDirectory();

			JFileChooser jfc = new JFileChooser(directory);
			jfc.setDialogTitle("Select a safe");
			jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);

			File destination = null;
			int returnValue = jfc.showOpenDialog(this.frame);
			if (returnValue == JFileChooser.APPROVE_OPTION)
				if (jfc.getSelectedFile().isFile())
				{
					destination = jfc.getSelectedFile();
					this.configuration.addSafeFilePath(destination.getAbsolutePath());
					this.comboBox.setSelectedItem(destination.getAbsolutePath());
					this.openButton.requestFocus();
				}

		} else if (event.getActionCommand().equals(this.openButton.getActionCommand()))
		{

			// final String item = (String) comboBox.getSelectedItem();
			final String item = getText();
			File safeFile = new File(item);

			if (!safeFile.exists())
			{
				new ErrorDialog(this.frame, "File " + item + " not found", null).setVisible(true);
			} else if (!safeFile.isFile())
			{
				new ErrorDialog(this.frame, "Path " + item + " is not a file", null).setVisible(true);
			} else
			{

				final JPasswordField pwd = new JPasswordField(10);

				final JOptionPane optionPane = new JOptionPane(pwd, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
				JDialog dialog = optionPane.createDialog(this.frame, "Enter Password");
				dialog.addComponentListener(new ComponentListener()
				{

					@Override
					public void componentShown(final ComponentEvent e)
					{
						pwd.requestFocusInWindow();
					}

					@Override
					public void componentHidden(ComponentEvent e)
					{
					}

					@Override
					public void componentResized(ComponentEvent e)
					{
					}

					@Override
					public void componentMoved(ComponentEvent e)
					{
					}
				});
				dialog.setVisible(true);

				Integer action = (Integer) optionPane.getValue();
				if (action != null)
					if (action == 0)
					{

						final ProgressDialog pd = new ProgressDialog(this.frame);
						pd.setTitle("Opening safe...");
						final OpenTask saveTask = new OpenTask(safeFile, pwd.getPassword(), this);
						pwd.setText("");
						pd.monitor(saveTask, "Opening safe...");
					}
			}

		}
	}

	public void setText(final String text)
	{
		final JTextField textField = (JTextField) comboBox.getEditor().getEditorComponent();
		textField.setText(text);
	}

	public String getText()
	{
		final JTextField textField = (JTextField) comboBox.getEditor().getEditorComponent();
		return textField.getText();
	}

	public Configuration getConfiguration()
	{
		return configuration;
	}

	public JFrame getFrame()
	{
		return frame;
	}
}
