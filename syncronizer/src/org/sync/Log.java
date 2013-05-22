/*****************************************************************************
    This file is part of Git-Starteam.

    Git-Starteam is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Git-Starteam is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Git-Starteam.  If not, see <http://www.gnu.org/licenses/>.
******************************************************************************/
package org.sync;

import java.text.SimpleDateFormat;
import java.util.Date;

public final class Log {

	// prevent instantiation
	private Log() {}

	// SimpleDateFormat is not threadsafe
	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	private static synchronized StringBuilder newEntry() {
		Date date = new Date();
		StringBuilder b = new StringBuilder();
		return b.append(DATE_FORMAT.format(date)).append(' ');
	}

	/**
	 * Logs msg to stderr
	 */
	public static void log(String msg) {
		System.err.println(newEntry().append(msg));
	}

	/**
	 * Logs formatted message to stderr
	 */
	public static void logf(String fmt, Object...objects) {
		System.err.println(newEntry().append(String.format(fmt, objects)));
	}

	/**
	 * Logs msg to stdout
	 */
	public static void out(String msg) {
		System.out.println(newEntry().append(msg));
	}

}
