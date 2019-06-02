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
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * @author Ortis
 */
public interface FontTheme
{
	Font getHeaderFont();

	Font getMetaDataFieldFont();

	Font getFieldFont();


	Font getNumericalFieldFont();

	Font getBigNumericalFieldFont();

	Font getNumericalInputFieldFont();

	Font getRawTextFont();

	Font getFirstButtonFont();

	Font getSecondButtonFont();

	Font getThirdButtonFont();

	Font getTabLabelFont();

	Font getLoginOpenFont();

	Font getLoginCreateFont();


	default boolean isSupported()
	{
		final List<Font> fonts = new ArrayList<>();

		fonts.add(getMetaDataFieldFont());
		fonts.add(getFieldFont());
		fonts.add(getNumericalFieldFont());
		fonts.add(getRawTextFont());
		fonts.add(getFirstButtonFont());
		fonts.add(getSecondButtonFont());
		fonts.add(getThirdButtonFont());
		fonts.add(getLoginOpenFont());
		fonts.add(getLoginCreateFont());

		final GraphicsEnvironment g = GraphicsEnvironment.getLocalGraphicsEnvironment();
		final String[] availableFontNames = g.getAvailableFontFamilyNames();

		for(final Font font : fonts)
		{
			boolean found = false;
			final String fontName = font.getName();
			for(final String availableFontName : availableFontNames)

				if(fontName.equals(availableFontName))
				{
					found = true;
					break;
				}

			if(!found)
			{
				System.out.println("Font " + fontName + " not available");
				return false;
			}
		}

		return true;
	}

	static FontTheme of(final String fontTheme)
	{
		if(fontTheme == null)
			return null;

		final String fontThemeLabel = fontTheme.trim().toUpperCase(Locale.ENGLISH);
		if(WindowsFont.class.getSimpleName().toUpperCase(Locale.ENGLISH).equals(fontThemeLabel))
			return new WindowsFont();
		else if(UbuntuFont.class.getSimpleName().toUpperCase(Locale.ENGLISH).equals(fontThemeLabel))
			return new UbuntuFont();
		else if(SafeFont.class.getSimpleName().toUpperCase(Locale.ENGLISH).equals(fontThemeLabel))
			return new SafeFont();
		else if(NullFont.class.getSimpleName().toUpperCase(Locale.ENGLISH).equals(fontThemeLabel))
			return new NullFont();

		return null;
	}
}
