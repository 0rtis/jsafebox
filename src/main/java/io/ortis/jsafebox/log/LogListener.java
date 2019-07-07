/*
 *  Copyright (c) 2019 by Adequate Systems, LLC. All Rights Reserved.
 *
 *  See LICENSE.PDF https://github.com/mochimodev/mochimo/blob/master/LICENSE.PDF
 *
 *  **** NO WARRANTY ****
 *
 */
package io.ortis.jsafebox.log;

import java.util.logging.LogRecord;

/**
 * 
 * @author Ortis
 *
 */
public interface LogListener
{
	void onLog(final LogRecord record);
}
