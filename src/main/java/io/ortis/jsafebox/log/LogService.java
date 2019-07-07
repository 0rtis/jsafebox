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
import java.util.logging.Logger;

/**
 * 
 * @author Ortis
 *
 */
public interface LogService
{

	boolean addListener(final LogListener listener);
	
	boolean removeListener(final LogListener listener);
	
	 Logger getLogger(final String name);
	 
	 String format(final LogRecord record);
}
