
package org.ortis.jsafe.gui.preview;

// File : editor/NutPad.java -- A very simple text editor
// Purpose: Illustrates use of AbstractActions for menus.
// It only uses a few Action features. Many more are available.
// This program uses the obscure "read" and "write"
// text component methods.
// Author : Fred Swartz - 2006-12-14 - Placed in public domain.

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.*;

///////////////////////////////////////////////////////////////////////// NutPad
public class NutPad extends JFrame
{
	// ... Components
	private JTextArea _editArea;
	private JFileChooser _fileChooser = new JFileChooser();

	// ... Create actions for menu items, buttons, ...
	private Action _openAction = new OpenAction();
	private Action _saveAction = new SaveAction();
	private Action _exitAction = new ExitAction();

	// ===================================================================== main
	public static void main(String [] args) throws Exception
	{
		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		new NutPad();
	}

	// ============================================================== constructor
	public NutPad()
	{
		// ... Create scrollable text area.
		_editArea = new JTextArea(15, 80);
		_editArea.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
		_editArea.setFont(new Font("monospaced", Font.PLAIN, 14));
		//_editArea.setWrapStyleWord(true);
		_editArea.setLineWrap(true);
		
		
		JScrollPane scrollingText = new JScrollPane(_editArea);

		// -- Create a content pane, set layout, add component.
		JPanel content = new JPanel();
		content.setLayout(new BorderLayout());
		content.add(scrollingText, BorderLayout.CENTER);

		// ... Create menubar
		JMenuBar menuBar = new JMenuBar();
		JMenu fileMenu = menuBar.add(new JMenu("File"));
		fileMenu.setMnemonic('F');
		fileMenu.add(_openAction); // Note use of actions, not text.
		fileMenu.add(_saveAction);
		fileMenu.addSeparator();
		fileMenu.add(_exitAction);

		// ... Set window content and menu.
		setContentPane(content);
		setJMenuBar(menuBar);

		// ... Set other window characteristics.
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setTitle("NutPad");
		pack();
		setLocationRelativeTo(null);
		setVisible(true);
	}

	////////////////////////////////////////////////// inner class OpenAction
	class OpenAction extends AbstractAction
	{
		// ============================================= constructor
		public OpenAction()
		{
			super("Open...");
			putValue(MNEMONIC_KEY, new Integer('O'));
		}

		// ========================================= actionPerformed
		public void actionPerformed(ActionEvent e)
		{
			int retval = _fileChooser.showOpenDialog(NutPad.this);
			if (retval == JFileChooser.APPROVE_OPTION)
			{
				File f = _fileChooser.getSelectedFile();
				try
				{
					FileReader reader = new FileReader(f);
					_editArea.read(reader, ""); // Use TextComponent read
				} catch (IOException ioex)
				{
					System.out.println(e);
					System.exit(1);
				}
			}
		}
	}

	//////////////////////////////////////////////////// inner class SaveAction
	class SaveAction extends AbstractAction
	{
		// ============================================= constructor
		SaveAction()
		{
			super("Save...");
			putValue(MNEMONIC_KEY, new Integer('S'));
		}

		// ========================================= actionPerformed
		public void actionPerformed(ActionEvent e)
		{
			int retval = _fileChooser.showSaveDialog(NutPad.this);
			if (retval == JFileChooser.APPROVE_OPTION)
			{
				File f = _fileChooser.getSelectedFile();
				try
				{
					FileWriter writer = new FileWriter(f);
					_editArea.write(writer); // Use TextComponent write
				} catch (IOException ioex)
				{
					JOptionPane.showMessageDialog(NutPad.this, ioex);
					System.exit(1);
				}
			}
		}
	}

	///////////////////////////////////////////////////// inner class ExitAction
	class ExitAction extends AbstractAction
	{

		// ============================================= constructor
		public ExitAction()
		{
			super("Exit");
			putValue(MNEMONIC_KEY, new Integer('X'));
		}

		// ========================================= actionPerformed
		public void actionPerformed(ActionEvent e)
		{
			System.exit(0);
		}
	}
}
