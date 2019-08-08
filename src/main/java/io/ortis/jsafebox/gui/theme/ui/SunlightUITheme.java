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
public class SunlightUITheme implements UITheme
{

	private final static Color SUNLIGHT_THEME_TEXT_HEADER_COLOR = new Color(115, 122, 133);
	private final static Color SUNLIGHT_THEME_BACKGROUND_COLOR = new Color(32, 32, 32);

	private final static Color SUNLIGHT_THEME_LEFT_PANEL_COLOR = new Color(255, 255, 255);
	private final static Color SUNLIGHT_THEME_LOGIN_PANEL_COLOR = SUNLIGHT_THEME_BACKGROUND_COLOR;
	private final static Color SUNLIGHT_THEME_SELECTION_COLOR = new Color(255, 85, 0);

	private final static Color SUNLIGHT_THEME_BUTTON_FIRST_COLOR = SUNLIGHT_THEME_SELECTION_COLOR;
	private final static Color SUNLIGHT_THEME_BUTTON_FIRST_COLOR_MOUSE_OVER = new Color(255, 125, 40);

	private final static Color SUNLIGHT_THEME_BUTTON_SECOND_COLOR = new Color(156, 160, 169);
	private final static Color SUNLIGHT_THEME_BUTTON_SECOND_COLOR_MOUSE_OVER = new Color(189, 191, 197);

	private final static Color SUNLIGHT_THEME_BUTTON_THIRD_COLOR = new Color(99, 105, 112);
	private final static Color SUNLIGHT_THEME_BUTTON_THIRD_COLOR_MOUSE_OVER = new Color(151, 155, 159);

	private final static Color TEXT_LABEL_COLOR = Color.WHITE;

	private final static Color BUTTON_TEXT_COLOR = Color.WHITE;

	private final static Color ADD_COLOR = new Color(78, 154, 6);
	private final static Color DELETE_COLOR = Color.RED;
	private final static Color UPDATE_COLOR = Color.BLUE;

	private final OS os;

	public SunlightUITheme(final OS os)
	{
		this.os = os;
	}

	@Override
	public Color getHeaderTextColor()
	{
		return SUNLIGHT_THEME_TEXT_HEADER_COLOR;
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
	public Color getButtonFirstColor()
	{
		return SUNLIGHT_THEME_BUTTON_FIRST_COLOR;
	}

	@Override
	public Color getButtonFirstColorMouseOver()
	{
		return SUNLIGHT_THEME_BUTTON_FIRST_COLOR_MOUSE_OVER;
	}

	@Override
	public Color getButtonSecondColor()
	{
		return SUNLIGHT_THEME_BUTTON_SECOND_COLOR;
	}

	@Override
	public Color getButtonSecondColorMouseOver()
	{
		return SUNLIGHT_THEME_BUTTON_SECOND_COLOR_MOUSE_OVER;
	}

	@Override
	public Color getButtonThirdColor()
	{
		return SUNLIGHT_THEME_BUTTON_THIRD_COLOR;
	}

	@Override
	public Color getButtonThirdColorMouseOver()
	{
		return SUNLIGHT_THEME_BUTTON_THIRD_COLOR_MOUSE_OVER;
	}

	@Override
	public Color getBackgroundColor()
	{
		return SUNLIGHT_THEME_BACKGROUND_COLOR;
	}


	@Override
	public Color getLoginBackgroundColor()
	{
		return SUNLIGHT_THEME_LOGIN_PANEL_COLOR;
	}

	@Override
	public Color getLeftPanelBackgroundColor()
	{
		return SUNLIGHT_THEME_LEFT_PANEL_COLOR;
	}


	@Override
	public Color getClickableColor()
	{
		return SUNLIGHT_THEME_SELECTION_COLOR;
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
