package org.ortis.jsafebox.gui.styles.colors;



import org.ortis.jsafebox.OS;

import javax.swing.*;
import java.awt.*;

/**
 * @author Ortis
 */
public class DarkColorTheme implements ColorTheme
{

	private final static Color DARK_THEME_TEXT_HEADER_COLOR = new Color(115, 122, 133);
	private final static Color DARK_THEME_BACKGROUND_COLOR = new Color(32, 32, 32);
	private final static Color DARK_THEME_OPTION_COLOR = new Color(0, 0, 0);
	private final static Color DARK_THEME_LEFT_PANEL_MOUSE_OVER_COLOR = new Color(15, 15, 15);
	private final static Color DARK_THEME_TOP_PANEL_COLOR = new Color(45, 45, 45);// new Color(178, 184, 196);
	private final static Color DARK_THEME_SELECTION_COLOR = new Color(0, 89, 255);
	private final static Color DARK_THEME_BUTTON_FIRST_COLOR = DARK_THEME_SELECTION_COLOR;
	private final static Color DARK_THEME_BUTTON_FIRST_COLOR_MOUSE_OVER = new Color(85, 144, 255);
	private final static Color DARK_THEME_BUTTON_SECOND_COLOR = new Color(156, 160, 169);
	;
	private final static Color DARK_THEME_BUTTON_SECOND_COLOR_MOUSE_OVER = new Color(189, 191, 197);
	private final static Color DARK_THEME_BUTTON_THIRD_COLOR = new Color(99, 105, 112);
	private final static Color DARK_THEME_BUTTON_THIRD_COLOR_MOUSE_OVER = new Color(151, 155, 159);

	private final static Color TEXT_LABEL_COLOR = Color.WHITE;

	private final static Color BUTTON_TEXT_COLOR = Color.WHITE;

	private final static Color OK_COLOR = new Color(0, 204, 51);
	private final static Color KO_COLOR = new Color(255, 0, 0);


	private final OS os;

	public DarkColorTheme(final OS os)
	{
		this.os = os;
	}

	@Override
	public Color getHeaderTextColor()
	{
		return DARK_THEME_TEXT_HEADER_COLOR;
	}

	@Override
	public Color getTextFieldColor()
	{
		switch(this.os)
		{
			case Linux:
				return ColorTheme.LINUX_TEXT_FIELD_COLOR;

			case Windows:
				return ColorTheme.WINDOWS_TEXT_FIELD_COLOR;

			case Mac:
				return ColorTheme.MAC_TEXT_FIELD_COLOR;

			default:
				return ColorTheme.DEFAULT_TEXT_FIELD_COLOR;
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
		return DARK_THEME_BUTTON_FIRST_COLOR;
	}

	@Override
	public Color getButtonFirstColorMouseOver()
	{
		return DARK_THEME_BUTTON_FIRST_COLOR_MOUSE_OVER;
	}

	@Override
	public Color getButtonSecondColor()
	{
		return DARK_THEME_BUTTON_SECOND_COLOR;
	}

	@Override
	public Color getButtonSecondColorMouseOver()
	{
		return DARK_THEME_BUTTON_SECOND_COLOR_MOUSE_OVER;
	}

	@Override
	public Color getButtonThirdColor()
	{
		return DARK_THEME_BUTTON_THIRD_COLOR;
	}

	@Override
	public Color getButtonThirdColorMouseOver()
	{
		return DARK_THEME_BUTTON_THIRD_COLOR_MOUSE_OVER;
	}

	@Override
	public Color getBackgroundColor()
	{
		return DARK_THEME_BACKGROUND_COLOR;
	}

	@Override
	public Color getTopPanelBackgroundColor()
	{
		return DARK_THEME_TOP_PANEL_COLOR;
	}

	@Override
	public Color getLeftPanelBackgroundColor()
	{
		return DARK_THEME_OPTION_COLOR;
	}

	@Override
	public Color getLeftPanelBackgroundMouseOverColor()
	{
		return DARK_THEME_LEFT_PANEL_MOUSE_OVER_COLOR;
	}

	@Override
	public Color getClickableColor()
	{
		return DARK_THEME_SELECTION_COLOR;
	}

	@Override
	public Color getOkColor()
	{
		return OK_COLOR;
	}

	@Override
	public Color getKoColor()
	{
		return KO_COLOR;
	}
}
