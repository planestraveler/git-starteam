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
package org.ossnoize.git.fastimport;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

public class Data implements FastImportObject {
	private static final String DATA = "data";
	private ByteArrayOutputStream Container;
	private File file;
	
	public Data() {
		Container = new ByteArrayOutputStream();
	}
	
	public Data(File f) {
		file = f;
	}
	
	public OutputStream getOutputStream() throws IOException {
		if(null != Container) {
			return Container;
		}
		return null;
	}
	
	public void writeData(byte[] array) throws IOException {
		Container.write(array);
	}
	
	@Override
	public void writeTo(OutputStream out) throws IOException {
		StringBuilder builder = new StringBuilder();
		builder.append(DATA).append(" ");
		if(null != Container) {
			builder.append(Container.size());
		} else {
			builder.append(file.length());
		}
		builder.append("\n");
		out.write(builder.toString().getBytes());
		if(null != Container) {
			Container.writeTo(out);
		} else {
			FileInputStream fin = new FileInputStream(file);
			byte[] buffer = new byte[1024*64];
			int read = fin.read(buffer);
			while(read >= 0) {
				out.write(buffer, 0, read);
				read = fin.read(buffer);
			}
			fin.close();
		}
		out.write('\n');
	}

}
