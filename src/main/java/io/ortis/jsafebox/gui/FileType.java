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

package io.ortis.jsafebox.gui;

import java.awt.*;
import java.util.Locale;

/**
 * @author Ortis
 */
public enum FileType
{
	Image, Text, Audio, Video, Unknown;

	public static FileType parse( String serial)
	{
		if(serial==null)
			return null;

		serial = serial.toUpperCase(Locale.ENGLISH);

		for(final FileType ft : FileType.values())
			if(ft.name().toUpperCase(Locale.ENGLISH).equals(serial))
				return ft;

			return null;
	}
}
