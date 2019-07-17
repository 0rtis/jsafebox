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

package io.ortis.jsafebox.gui.metadata;

import io.ortis.jsafebox.Block;
import io.ortis.jsafebox.SafeFile;
import io.ortis.jsafebox.Utils;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Vector;

/**
 * @author Ortis
 */
class MetadataTableModel extends DefaultTableModel
{
	private static final Class[] TYPES = new Class[]{String.class, String.class};

	private final JTable table;

	private final Object lock = new Object();

	public MetadataTableModel(final JTable table)
	{
		super(new String[]{"Property", "Value"}, 0);

		this.table = table;

	}


	public void setSafeFile(final SafeFile safeFile)
	{
		if(!(safeFile instanceof Block))
			return;

		final Block block = (Block) safeFile;

		final LinkedHashMap<String, String> properties = new LinkedHashMap<>();
		properties.put(Block.NAME_LABEL, block.getProperties().get(Block.NAME_LABEL));
		properties.put(Block.PATH_LABEL, block.getProperties().get(Block.PATH_LABEL));
		properties.put("size", Utils.humanReadableByteCount(block.getDataLength()));
		block.getProperties().forEach(properties::putIfAbsent);

		for(final Map.Entry<String, String> prop : properties.entrySet())
		{
			final Object[] row = new Object[]{prop.getKey(), prop.getValue()};

			final Vector vector = super.convertToVector(row);

			synchronized(this.lock)
			{
				addRow(vector);
			}
		}
	}

	public void clear()
	{
		synchronized(this.lock)
		{
			while(getRowCount() > 0)
				removeRow(0);

			fireTableDataChanged();

		}
	}

	public Class getColumnClass(final int columnIndex)
	{
		return TYPES[columnIndex];
	}

	public boolean isCellEditable(final int row, final int column)
	{
		return false;
	}
}
