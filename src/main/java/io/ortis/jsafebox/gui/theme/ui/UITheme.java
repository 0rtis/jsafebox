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

import java.awt.*;

/**
 * @author Ortis
 */
public interface UITheme
{
	Color WINDOWS_TEXT_FIELD_COLOR = Color.WHITE;
	Color LINUX_TEXT_FIELD_COLOR = Color.BLACK;
	Color OSX_TEXT_FIELD_COLOR = Color.WHITE;
	Color DEFAULT_TEXT_FIELD_COLOR = Color.WHITE;

	Color getHeaderTextColor();

	Color getTextFieldColor();

	Color getTextLabelColor();

	Color getButtonTextColor();

	Color getBackgroundColor();

	Color getLeftPanelBackgroundColor();

	Color getClickableColor();

	Color getButtonFirstColor();

	Color getButtonFirstColorMouseOver();

	Color getButtonSecondColor();

	Color getButtonSecondColorMouseOver();

	Color getButtonThirdColor();

	Color getButtonThirdColorMouseOver();

	Color pendingDeleteColor();

	Color pendingAddColor();

	Color pendingUpdateColor();
}
