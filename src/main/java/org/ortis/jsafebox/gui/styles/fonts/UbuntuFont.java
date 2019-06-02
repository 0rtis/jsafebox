/*
 *  Copyright (c) 2019 by Adequate Systems, LLC. All Rights Reserved.
 *
 *  See LICENSE.PDF https://github.com/mochimodev/mochimo/blob/master/LICENSE.PDF
 *
 *  **** NO WARRANTY ****
 *
 */

package org.ortis.jsafebox.gui.styles.fonts;

import java.awt.*;

/**
 * @author Ortis
 */
public class UbuntuFont implements FontTheme
{
	private final static Font UBUNTU_HEADER_FONT = new Font("Ubuntu", Font.BOLD, 18);
	private final static Font UBUNTU_META_DATA_FIELD_FONT = new Font("Ubuntu", Font.BOLD, 14);
	private final static Font UBUNTU_FIELD_FONT = new Font("Ubuntu", Font.PLAIN, 14);

	private final static Font UBUNTU_NUMERICAL_INPUT_FIELD_FONT = new Font("Ubuntu Mono", Font.PLAIN, 14);
	private final static Font UBUNTU_NUMERICAL_FIELD_FONT = new Font("Ubuntu Mono", Font.PLAIN, 16);
	private final static Font UBUNTU_BIG_NUMERICAL_FIELD_FONT = new Font("Ubuntu Mono", Font.PLAIN, 26);


	private final static Font UBUNTU_RAW_TEXT_FIELD_FONT = new Font("Ubuntu", Font.PLAIN, 12);

	private final static Font UBUNTU_FIRST_BUTTON_FONT = new Font("Ubuntu", Font.PLAIN, 20);
	private final static Font UBUNTU_SECOND_BUTTON_FONT = new Font("Ubuntu", Font.PLAIN, 16);
	private final static Font UBUNTU_THIRD_BUTTON_FONT = new Font("Ubuntu", Font.PLAIN, 16);

	private final static Font UBUNTU_TAB_LABEL_FONT = new Font("Ubuntu", Font.PLAIN, 18);

	private final static Font UBUNTU_LOGIN_OPEN_FONT = new Font("Ubuntu", Font.BOLD, 16);
	private final static Font UBUNTU_LOGIN_CREATE_FONT = new Font("Ubuntu", Font.BOLD, 14);


	@Override
	public Font getHeaderFont()
	{
		return UBUNTU_HEADER_FONT;
	}

	@Override
	public Font getMetaDataFieldFont()
	{
		return UBUNTU_META_DATA_FIELD_FONT;
	}

	@Override
	public Font getFieldFont()
	{
		return UBUNTU_FIELD_FONT;
	}

	@Override
	public Font getNumericalFieldFont()
	{
		return UBUNTU_NUMERICAL_FIELD_FONT;
	}

	@Override
	public Font getBigNumericalFieldFont()
	{
		return UBUNTU_BIG_NUMERICAL_FIELD_FONT;
	}


	@Override
	public Font getNumericalInputFieldFont()
	{
		return UBUNTU_NUMERICAL_INPUT_FIELD_FONT;
	}

	@Override
	public Font getRawTextFont()
	{
		return UBUNTU_RAW_TEXT_FIELD_FONT;
	}

	@Override
	public Font getFirstButtonFont()
	{
		return UBUNTU_FIRST_BUTTON_FONT;
	}

	@Override
	public Font getSecondButtonFont()
	{
		return UBUNTU_SECOND_BUTTON_FONT;
	}

	@Override
	public Font getThirdButtonFont()
	{
		return UBUNTU_THIRD_BUTTON_FONT;
	}

	@Override
	public Font getTabLabelFont()
	{
		return UBUNTU_TAB_LABEL_FONT;
	}

	@Override
	public Font getLoginOpenFont()
	{
		return UBUNTU_LOGIN_OPEN_FONT;
	}

	@Override
	public Font getLoginCreateFont()
	{
		return UBUNTU_LOGIN_CREATE_FONT;
	}
}
