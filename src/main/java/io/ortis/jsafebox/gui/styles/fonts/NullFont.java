/*
 *  Copyright (c) 2019 by Adequate Systems, LLC. All Rights Reserved.
 *
 *  See LICENSE.PDF https://github.com/mochimodev/mochimo/blob/master/LICENSE.PDF
 *
 *  **** NO WARRANTY ****
 *
 */

package io.ortis.jsafebox.gui.styles.fonts;

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
