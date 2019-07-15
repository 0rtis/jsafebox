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

package io.ortis.jsafebox.gui.theme.fonts;

import java.awt.*;

/**
 * @author Ortis
 */
public class SafeFont implements FontTheme
{
	private final static Font SAFE_HEADER_FONT = new Font("Dialog", Font.BOLD, 18);
	private final static Font SAFE_META_DATA_FIELD_FONT = new Font("Dialog", Font.BOLD, 14);
	private final static Font SAFE_FIELD_FONT = new Font("Dialog", Font.PLAIN, 14);

	private final static Font SAFE_NUMERICAL_INPUT_FIELD_FONT = new Font("Monospaced", Font.PLAIN, 12);
	private final static Font SAFE_NUMERICAL_FIELD_FONT = new Font("Monospaced", Font.PLAIN, 14);
	private final static Font SAFE_BIG_NUMERICAL_FIELD_FONT = new Font("Monospaced", Font.PLAIN, 24);

	private final static Font SAFE_TEXT_FIELD_FONT = new Font("Dialog", Font.PLAIN, 12);

	private final static Font SAFE_FIRST_BUTTON_FONT = new Font("Dialog", Font.BOLD, 18);
	private final static Font SAFE_SECOND_BUTTON_FONT = new Font("Dialog", Font.PLAIN, 18);
	private final static Font SAFE_THIRD_BUTTON_FONT = new Font("Dialog", Font.PLAIN, 18);

	private final static Font SAFE_TAB_LABEL_FONT = new Font("Dialog", Font.PLAIN, 18);

	private final static Font SAFE_LOGIN_OPEN_FONT = new Font("Dialog", Font.BOLD, 16);
	private final static Font SAFE_LOGIN_CREATE_FONT = new Font("Dialog", Font.BOLD, 14);


	@Override
	public Font getHeaderFont()
	{
		return SAFE_HEADER_FONT;
	}

	@Override
	public Font getMetaDataFieldFont()
	{
		return SAFE_META_DATA_FIELD_FONT;
	}

	@Override
	public Font getFieldFont()
	{
		return SAFE_FIELD_FONT;
	}

	@Override
	public Font getNumericalFieldFont()
	{
		return SAFE_NUMERICAL_FIELD_FONT;
	}

	@Override
	public Font getBigNumericalFieldFont()
	{
		return SAFE_BIG_NUMERICAL_FIELD_FONT;
	}

	@Override
	public Font getNumericalInputFieldFont()
	{
		return SAFE_NUMERICAL_INPUT_FIELD_FONT;
	}

	@Override
	public Font getRawTextFont()
	{
		return SAFE_TEXT_FIELD_FONT;
	}

	@Override
	public Font getFirstButtonFont()
	{
		return SAFE_FIRST_BUTTON_FONT;
	}

	@Override
	public Font getSecondButtonFont()
	{
		return SAFE_SECOND_BUTTON_FONT;
	}

	@Override
	public Font getThirdButtonFont()
	{
		return SAFE_THIRD_BUTTON_FONT;
	}


	@Override
	public Font getTabLabelFont()
	{
		return SAFE_TAB_LABEL_FONT;
	}

	@Override
	public Font getLoginOpenFont()
	{
		return SAFE_LOGIN_OPEN_FONT;
	}

	@Override
	public Font getLoginCreateFont()
	{
		return SAFE_LOGIN_CREATE_FONT;
	}
}
