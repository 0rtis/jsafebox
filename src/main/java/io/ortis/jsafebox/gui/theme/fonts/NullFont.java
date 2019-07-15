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
public class NullFont implements FontTheme
{
	private final static Font NULL_HEADER_FONT = new Font(null, Font.BOLD, 18);
	private final static Font NULL_META_DATA_FIELD_FONT = new Font(null, Font.BOLD, 14);
	private final static Font NULL_FIELD_FONT = new Font(null, Font.PLAIN, 14);

	private final static Font NULL_NUMERICAL_INPUT_FIELD_FONT = new Font(null, Font.PLAIN, 12);
	private final static Font NULL_NUMERICAL_FIELD_FONT = new Font(null, Font.PLAIN, 14);
	private final static Font NULL_BIG_NUMERICAL_FIELD_FONT = new Font(null, Font.PLAIN, 24);

	private final static Font NULL_TEXT_FIELD_FONT = new Font(null, Font.PLAIN, 12);

	private final static Font NULL_FIRST_BUTTON_FONT = new Font(null, Font.BOLD, 18);
	private final static Font NULL_SECOND_BUTTON_FONT = new Font(null, Font.PLAIN, 18);
	private final static Font NULL_THIRD_BUTTON_FONT = new Font(null, Font.PLAIN, 18);

	private final static Font NULL_TAB_LABEL_FONT = new Font(null, Font.PLAIN, 18);

	private final static Font NULL_LOGIN_OPEN_FONT = new Font(null, Font.BOLD, 16);
	private final static Font NULL_LOGIN_CREATE_FONT = new Font(null, Font.BOLD, 14);


	@Override
	public Font getHeaderFont()
	{
		return NULL_HEADER_FONT;
	}

	@Override
	public Font getMetaDataFieldFont()
	{
		return NULL_META_DATA_FIELD_FONT;
	}

	@Override
	public Font getFieldFont()
	{
		return NULL_FIELD_FONT;
	}

	@Override
	public Font getNumericalFieldFont()
	{
		return NULL_NUMERICAL_FIELD_FONT;
	}

	@Override
	public Font getBigNumericalFieldFont()
	{
		return NULL_BIG_NUMERICAL_FIELD_FONT;
	}

	@Override
	public Font getNumericalInputFieldFont()
	{
		return NULL_NUMERICAL_INPUT_FIELD_FONT;
	}

	@Override
	public Font getRawTextFont()
	{
		return NULL_TEXT_FIELD_FONT;
	}

	@Override
	public Font getFirstButtonFont()
	{
		return NULL_FIRST_BUTTON_FONT;
	}

	@Override
	public Font getSecondButtonFont()
	{
		return NULL_SECOND_BUTTON_FONT;
	}

	@Override
	public Font getThirdButtonFont()
	{
		return NULL_THIRD_BUTTON_FONT;
	}

	@Override
	public Font getTabLabelFont()
	{
		return NULL_TAB_LABEL_FONT;
	}

	@Override
	public Font getLoginOpenFont()
	{
		return NULL_LOGIN_OPEN_FONT;
	}

	@Override
	public Font getLoginCreateFont()
	{
		return NULL_LOGIN_CREATE_FONT;
	}
}
