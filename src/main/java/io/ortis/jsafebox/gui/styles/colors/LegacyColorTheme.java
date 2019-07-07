package io.ortis.jsafebox.gui.styles.colors;

import io.ortis.jsafebox.OS;

import java.awt.*;

/**
 * @author Ortis
 */
public class LegacyColorTheme implements ColorTheme
{
	private final static Color LEGACY_TEXT_HEADER_COLOR = new Color(115, 122, 133);

	private final static Color LEGACY_BACKGROUND_COLOR = new Color(45, 58, 79);
	private final static Color LEGACY_OPTION_COLOR = new Color(22, 28, 38);
	private final static Color LEGACY_LEFT_PANEL_MOUSE_OVER_COLOR = new Color(36, 42, 51);
	private final static Color LEGACY_TOP_PANEL_COLOR = new Color(102, 102, 102);
	private final static Color LEGACY_SELECTION_COLOR = new Color(102, 255, 255);

	private final static Color LEGACY_BUTTON_FIRST_COLOR = new Color(55, 120, 106);
	private final static Color LEGACY_BUTTON_FIRST_COLOR_MOUSE_OVER = new Color(121, 165, 155);
	private final static Color LEGACY_BUTTON_SECOND_COLOR = new Color(113, 119, 128);
	private final static Color LEGACY_BUTTON_SECOND_COLOR_MOUSE_OVER = new Color(160, 164, 170);
	private final static Color LEGACY_BUTTON_THIRD_COLOR = new Color(99, 105, 112);
	private final static Color LEGACY_BUTTON_THIRD_COLOR_MOUSE_OVER = new Color(151, 155, 159);

	private final static Color TEXT_LABEL_COLOR = Color.WHITE;

	private final static Color BUTTON_TEXT_COLOR = Color.WHITE;

	private final static Color OK_COLOR = new Color(0, 204, 51);
	private final static Color KO_COLOR = new Color(255, 0, 0);

	private final OS os;

	public LegacyColorTheme(final OS os)
	{
		this.os = os;
	}


	@Override
	public Color getHeaderTextColor()
	{
		return LEGACY_TEXT_HEADER_COLOR;
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

			case Mac:
				return MAC_TEXT_FIELD_COLOR;

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
		return LEGACY_BACKGROUND_COLOR;
	}

	@Override
	public Color getTopPanelBackgroundColor()
	{
		return LEGACY_TOP_PANEL_COLOR;
	}

	@Override
	public Color getLeftPanelBackgroundColor()
	{
		return LEGACY_OPTION_COLOR;
	}

	@Override
	public Color getLeftPanelBackgroundMouseOverColor()
	{
		return LEGACY_LEFT_PANEL_MOUSE_OVER_COLOR;
	}

	@Override
	public Color getClickableColor()
	{
		return LEGACY_SELECTION_COLOR;
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

	@Override
	public Color getButtonFirstColor()
	{
		return LEGACY_BUTTON_FIRST_COLOR;
	}

	@Override
	public Color getButtonFirstColorMouseOver()
	{
		return LEGACY_BUTTON_FIRST_COLOR_MOUSE_OVER;
	}

	@Override
	public Color getButtonSecondColor()
	{
		return LEGACY_BUTTON_SECOND_COLOR;
	}

	@Override
	public Color getButtonSecondColorMouseOver()
	{
		return LEGACY_BUTTON_SECOND_COLOR_MOUSE_OVER;
	}

	@Override
	public Color getButtonThirdColor()
	{
		return LEGACY_BUTTON_THIRD_COLOR;
	}

	@Override
	public Color getButtonThirdColorMouseOver()
	{
		return LEGACY_BUTTON_THIRD_COLOR_MOUSE_OVER;
	}
}
