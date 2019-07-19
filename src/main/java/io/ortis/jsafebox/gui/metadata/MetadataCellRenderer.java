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

import io.ortis.jsafebox.gui.Settings;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

/**
 * @author Ortis
 */
public class MetadataCellRenderer extends DefaultTableCellRenderer
{

	private final Color foregroundColor;
	private final Color backgroundColor;

	public MetadataCellRenderer(final Color foregroundColor, final Color backgroundColor)
	{
		this.foregroundColor = foregroundColor;
		this.backgroundColor = backgroundColor;

		setHorizontalAlignment(JLabel.CENTER);
	}

	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
	{
		final JComponent c = (JComponent) super.getTableCellRendererComponent(table, value, isSelected, false, row, column);

		if(row == -1)
			c.setFont(Settings.getSettings().getFontTheme().getMetaDataFieldFont());
		else
			c.setFont(Settings.getSettings().getFontTheme().getFieldFont());
			

		if(row >= 0)
		{

			if(value != null)
			{
				final String tooltip = value.toString();
				c.setToolTipText(tooltip);
			}
			else
				c.setToolTipText(null);

		}




		c.setBorder(new EmptyBorder(0, 0, 0, 0));
		c.setForeground(this.foregroundColor);
		c.setBackground(this.backgroundColor);

		return c;
	}

	@Override
	public boolean isEnabled()
	{
		return true;
	}
}
