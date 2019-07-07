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
public class WindowsFont implements FontTheme
{
	private final static Font WINDOWS_HEADER_FONT = new Font("Segoe UI", Font.BOLD, 18);
	private final static Font WINDOWS_META_DATA_FIELD_FONT = new Font("Segoe UI", Font.BOLD, 14);
	private final static Font WINDOWS_FIELD_FONT = new Font("Segoe UI", Font.PLAIN, 14);

	private final static Font WINDOWS_NUMERICAL_INPUT_FIELD_FONT = new Font("Consolas", Font.PLAIN, 12);
	private final static Font WINDOWS_NUMERICAL_FIELD_FONT = new Font("Consolas", Font.PLAIN, 14);
	private final static Font WINDOWS_BIG_NUMERICAL_FIELD_FONT = new Font("Consolas", Font.PLAIN, 24);

	private final static Font WINDOWS_RAW_TEXT_FIELD_FONT = new Font("Segoe UI", Font.PLAIN, 12);

	private final static Font WINDOWS_FIRST_BUTTON_FONT = new Font("Tahoma", Font.PLAIN, 18);
	private final static Font WINDOWS_SECOND_BUTTON_FONT = new Font("Tahoma", Font.PLAIN, 14);
	private final static Font WINDOWS_THIRD_BUTTON_FONT = new Font("Tahoma", Font.PLAIN, 14);

	private final static Font WINDOWS_TAB_LABEL_FONT = new Font("Segoe UI", Font.PLAIN, 18);

	private final static Font WINDOWS_LOGIN_OPEN_FONT = new Font("Segoe UI", Font.BOLD, 16);
	private final static Font WINDOWS_LOGIN_CREATE_FONT = new Font("Segoe UI", Font.BOLD, 14);


	@Override
	public Font getHeaderFont()
	{
		return WINDOWS_HEADER_FONT;
	}

	@Override
	public Font getMetaDataFieldFont()
	{
		return WINDOWS_META_DATA_FIELD_FONT;
	}

	@Override
	public Font getFieldFont()
	{
		return WINDOWS_FIELD_FONT;
	}

	@Override
	public Font getNumericalFieldFont()
	{
		return WINDOWS_NUMERICAL_FIELD_FONT;
	}

	@Override
	public Font getBigNumericalFieldFont()
	{
		return WINDOWS_BIG_NUMERICAL_FIELD_FONT;
	}

	@Override
	public Font getNumericalInputFieldFont()
	{
		return WINDOWS_NUMERICAL_INPUT_FIELD_FONT;
	}

	@Override
	public Font getRawTextFont()
	{
		return WINDOWS_RAW_TEXT_FIELD_FONT;
	}

	@Override
	public Font getFirstButtonFont()
	{
		return WINDOWS_FIRST_BUTTON_FONT;
	}

	@Override
	public Font getSecondButtonFont()
	{
		return WINDOWS_SECOND_BUTTON_FONT;
	}

	@Override
	public Font getThirdButtonFont()
	{
		return WINDOWS_THIRD_BUTTON_FONT;
	}

	@Override
	public Font getTabLabelFont()
	{
		return WINDOWS_TAB_LABEL_FONT;
	}

	@Override
	public Font getLoginOpenFont()
	{
		return WINDOWS_LOGIN_OPEN_FONT;
	}

	@Override
	public Font getLoginCreateFont()
	{
		return WINDOWS_LOGIN_CREATE_FONT;
	}
}
