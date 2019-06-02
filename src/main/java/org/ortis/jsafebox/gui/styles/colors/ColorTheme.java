/*
 *  Copyright (c) 2019 by Adequate Systems, LLC. All Rights Reserved.
 *
 *  See LICENSE.PDF https://github.com/mochimodev/mochimo/blob/master/LICENSE.PDF
 *
 *  **** NO WARRANTY ****
 *
 */

package org.ortis.jsafebox.gui.styles.colors;

import javax.swing.*;
import java.awt.*;

/**
 * @author Ortis
 */
public interface ColorTheme
{
	Color WINDOWS_TEXT_FIELD_COLOR = Color.WHITE;
	Color LINUX_TEXT_FIELD_COLOR = Color.BLACK;
	Color MAC_TEXT_FIELD_COLOR = Color.WHITE;
	Color DEFAULT_TEXT_FIELD_COLOR = Color.WHITE;

	Color getHeaderTextColor();

	Color getTextFieldColor();

	Color getTextLabelColor();

	Color getButtonTextColor();

	Color getBackgroundColor();

	Color getTopPanelBackgroundColor();

	Color getLeftPanelBackgroundColor();

	Color getLeftPanelBackgroundMouseOverColor();

	Color getClickableColor();

	Color getOkColor();

	Color getKoColor();

	Color getButtonFirstColor();

	Color getButtonFirstColorMouseOver();

	Color getButtonSecondColor();

	Color getButtonSecondColorMouseOver();

	Color getButtonThirdColor();

	Color getButtonThirdColorMouseOver();
}
