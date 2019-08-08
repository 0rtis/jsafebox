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

package io.ortis.jsafebox.gui.theme.ui;

import io.ortis.jsafebox.OS;

import java.awt.*;

/**
 * @author Ortis
 */
public class MojoUITheme implements UITheme
{
	private final static Color MOJO_TEXT_HEADER_COLOR = new Color(115, 122, 133);

	private final static Color MOJO_BACKGROUND_COLOR = new Color(45, 58, 79);

	private final static Color MOJO_LEFT_PANEL_COLOR = new Color(22, 28, 38);
	private final static Color MOJO_THEME_LOGIN_PANEL_COLOR = MOJO_LEFT_PANEL_COLOR;

	private final static Color MOJO_SELECTION_COLOR = new Color(102, 255, 255);

	private final static Color MOJO_BUTTON_FIRST_COLOR = new Color(55, 120, 106);
	private final static Color MOJO_BUTTON_FIRST_COLOR_MOUSE_OVER = new Color(121, 165, 155);

	private final static Color MOJO_BUTTON_SECOND_COLOR = new Color(113, 119, 128);
	private final static Color MOJO_BUTTON_SECOND_COLOR_MOUSE_OVER = new Color(160, 164, 170);

	private final static Color MOJO_BUTTON_THIRD_COLOR = new Color(99, 105, 112);
	private final static Color MOJO_BUTTON_THIRD_COLOR_MOUSE_OVER = new Color(151, 155, 159);

	private final static Color TEXT_LABEL_COLOR = Color.WHITE;

	private final static Color BUTTON_TEXT_COLOR = Color.WHITE;

	private final static Color ADD_COLOR = new Color(78, 154, 6);
	private final static Color DELETE_COLOR = Color.RED;//new Color(208, 15, 15);
	private final static Color UPDATE_COLOR = Color.BLUE;

	private final OS os;

	public MojoUITheme(final OS os)
	{
		this.os = os;
	}


	@Override
	public Color getHeaderTextColor()
	{
		return MOJO_TEXT_HEADER_COLOR;
	}

	@Override
	public Color getTextFieldColor()
	{
		switch(this.os)
		{
			case Linux:
				return LINUX_TEXT_FIELD_COLOR;

			case Windows:
				return WINDOWS_TEXT_FIELD_COLOR;

			case OSX:
				return OSX_TEXT_FIELD_COLOR;

			default:
				return DEFAULT_TEXT_FIELD_COLOR;
		}
	}

	@Override
	public Color getTextLabelColor()
	{
		return TEXT_LABEL_COLOR;
	}

	@Override
	public Color getButtonTextColor()
	{
		return BUTTON_TEXT_COLOR;
	}

	@Override
	public Color getBackgroundColor()
	{
		return MOJO_BACKGROUND_COLOR;
	}

	@Override
	public Color getLoginBackgroundColor()
	{
		return MOJO_THEME_LOGIN_PANEL_COLOR;
	}

	@Override
	public Color getLeftPanelBackgroundColor()
	{
		return MOJO_LEFT_PANEL_COLOR;
	}


	@Override
	public Color getClickableColor()
	{
		return MOJO_SELECTION_COLOR;
	}

	@Override
	public Color getButtonFirstColor()
	{
		return MOJO_BUTTON_FIRST_COLOR;
	}

	@Override
	public Color getButtonFirstColorMouseOver()
	{
		return MOJO_BUTTON_FIRST_COLOR_MOUSE_OVER;
	}

	@Override
	public Color getButtonSecondColor()
	{
		return MOJO_BUTTON_SECOND_COLOR;
	}

	@Override
	public Color getButtonSecondColorMouseOver()
	{
		return MOJO_BUTTON_SECOND_COLOR_MOUSE_OVER;
	}

	@Override
	public Color getButtonThirdColor()
	{
		return MOJO_BUTTON_THIRD_COLOR;
	}

	@Override
	public Color getButtonThirdColorMouseOver()
	{
		return MOJO_BUTTON_THIRD_COLOR_MOUSE_OVER;
	}

	@Override
	public Color pendingDeleteColor()
	{
		return DELETE_COLOR;
	}

	@Override
	public Color pendingAddColor()
	{
		return ADD_COLOR;
	}

	@Override
	public Color pendingUpdateColor()
	{
		return UPDATE_COLOR;
	}
}
