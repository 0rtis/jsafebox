
package org.ortis.jsafe.gui;

import java.io.File;
import java.util.Date;

import javax.swing.ImageIcon;
import javax.swing.filechooser.FileSystemView;
import javax.swing.table.DefaultTableModel;

import org.ortis.jsafe.Block;
import org.ortis.jsafe.Folder;
import org.ortis.jsafe.SafeFile;

public class SafeFileTableModel extends DefaultTableModel
{

	private final String [] columns = { "Property key", "Property value" };

	private final Block block;
	private final Folder folder;

	public SafeFileTableModel(final SafeFile safeFile)
	{
		this.block = null;
		this.folder = null;

	}

	public Object getValueAt(final int row, final int column)
	{

		return row + " " + column;
	}

	public int getColumnCount()
	{
		return 2;
	}

	public Class<?> getColumnClass(int column)
	{
		return String.class;
	}

	public String getColumnName(int column)
	{
		return columns[column];
	}

	public int getRowCount()
	{
		return 3;
	}

}
