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
package org.sync.util.test;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.junit.Test;
import org.sync.util.TempFileManager;

public class TempTrackerTest {

	@Test
	public void test() throws IOException {
		File temp = TempFileManager.getInstance().createTempFile("test", ".txt");
		assertEquals(true, temp.exists());
		
		FileWriter writer = new FileWriter(temp);
		writer.write("test document");
		writer.close();
		assertEquals(1, TempFileManager.getInstance().tempFileCount());
		
		TempFileManager.getInstance().deleteTempFiles();
		
		assertEquals(0, TempFileManager.getInstance().tempFileCount());
	}

}
