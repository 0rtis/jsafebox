
package org.ortis.jsafe.gui.preview;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.io.FileFilter;
import java.util.Scanner;

/**
 * @author s1gma
 */
public class Notepad extends JFrame implements ActionListener
{// like it can use all the
	private JTextArea txt = new JTextArea(); // basically i had to make it local so that it could be used in more than 1 method yh i see thnx for explaining :d

	private JMenuBar newMenubar()
	{
		JMenuBar menubar = new JMenuBar(); // Sets up the menubar
		String [] titles = { "File", "test" }; // leave lol ok
		String [] [] elements = { { "New", "Open", "Save" }, { "LOL" } }; // allready there lol ok go :d k lol
		for (int i = 0; i < titles.length; i++)
		{ // basically loops through the menu titles
			String title = titles[i]; // selects the titles from the loop
			String [] elems = elements[i];// basically finds the menuitems for the menu
			menubar.add(newMenu(title, elems)); // adds a new menu with the title and elements, u understand? lhl yes ;d
			// Okay now we add the menu to the frame and we will boot it up

		}
		return menubar;// Returns the menubar ok
	}

	/**
	 *
	 * @param title
	 *            The title like "File"
	 * @param elements
	 *            The elements like "New", "Load", "Save"
	 * @return returns the JMenu that you make o
	 */
	private JMenu newMenu(String title, String [] elements)
	{
		JMenu menu = new JMenu(title); // Creates a new JMenu with the title ik
		for (String element : elements)
		{ // u understand?yes :d
			JMenuItem menuitem = new JMenuItem(element);// already told you about this :Pok
			menu.add(menuitem); // uses the add method in the JMenu class for our menu to add them menuitems yh ok :d
			menuitem.addActionListener(this);// makes it so that the menuitems respond to the actionlistenerok
		}
		return menu;
	}

	private Notepad()
	{
		setTitle("untitled - Notepad"); // Wanna add the title thing now? k we'll do it now
		try
		{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); // basically gives it the system themeik
		} catch (ClassNotFoundException e)
		{
			e.printStackTrace();
		} catch (InstantiationException e)
		{
			e.printStackTrace();
		} catch (IllegalAccessException e)
		{
			e.printStackTrace();
		} catch (UnsupportedLookAndFeelException e)
		{
			e.printStackTrace();
		}
		setSize(800, 600); // straight forward lol
		setJMenuBar(newMenubar());
		JScrollPane scroller = new JScrollPane(txt);// it has the txt(JTextArea) in it to select what its the container fork
		add(scroller); // adds the scroller which has the text area in it.ok
		// Now this is where we have to make a menubar for files, i'll show you a nice way to do menus.ok
		// what else was i going to add?scrollbar oh yeah
	}

	public static void main(String [] args)
	{
		new Notepad().setVisible(true);// loads and sets it visible ik :p
	}

	// This shit here is for later to make the buttons and shit work. i know :d fk don't delete this class sent over msn soon :d
	public void actionPerformed(ActionEvent actionEvent)
	{
		String cmd = actionEvent.getActionCommand(); // basically retrieves what you've clickedok
		if (cmd.equals("Save"))
		{ // If the button pressed has the text "Save" on it do something inside.
			// Forgot file chooser
			JFileChooser chooser = new JFileChooser(); // sets up the file choosing dialog. ok
			int option = chooser.showSaveDialog(this); // Shows the save dialog and is the option for what you've clicked
			if (option == JFileChooser.APPROVE_OPTION)
			{ // if you've pressed the ok or save button or w/e do somethingok
				// also stop pressing o when i press control, it's frustrating l0lklol
				try
				{
					BufferedWriter buf = new BufferedWriter(new FileWriter(chooser.getSelectedFile().getAbsolutePath()));
					// ^ Basically the bufferedwriter is something used for writing to a file along with filewriter
					// yeah and the chooser.getSelectedFile().getAbsolutePath() basically finds the place in the filechooserik
					// and writes to it.
					buf.write(txt.getText()); // basically this gets the text in the text area and writes it to the file
					setTitle(chooser.getSelectedFile().getName()); // this basically gets the file name in the chooser. rofl thisi s easy lol :d thnx, i told you it wasnt hard :Pyh
					buf.close(); // closes the stream for memory purposesk, now we can run it
				} catch (IOException e)
				{
					e.printStackTrace();
				}
			}
		} else if (cmd.equals("Open"))
		{
			JFileChooser chooser = new JFileChooser(); // filechooser object

			int option = chooser.showOpenDialog(this); // same as before but with open this time. ok w8
			if (option == JFileChooser.APPROVE_OPTION)
			{
				try
				{
					Scanner scanner = new Scanner(chooser.getSelectedFile()); // gets the selected file from chooser
					while (scanner.hasNext())
					{ // When the scanner still has stuff to read, do something
						String data = scanner.nextLine(); // Read lines inside the scanner
						txt.setText(data); // Puts the data it read from the file into the text area.k
					}
					setTitle(chooser.getSelectedFile().getName());
					// Problem is i havent used file filter in a while so i mite do something wrong. ok
					scanner.close(); // close the scanner for memory purposes.
				} catch (FileNotFoundException e)
				{
					e.printStackTrace();
				}
			}
		}
	}

}
