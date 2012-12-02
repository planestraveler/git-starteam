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

import java.io.IOException;
import java.io.OutputStream;

import org.ossnoize.git.fastimport.enumeration.GitFileType;

public class FileModification extends FileOperation {
	private enum FileModificationType {
		Inline,
		Referenced;
	}

	private FastImportObject Content;
	private FileModificationType Type;
	private GitFileType FileType;

	public FileModification(Data data) {
		Content = data;
		Type = FileModificationType.Inline;
	}
	
	public FileModification(Blob data) {
		Content = data;
		Type = FileModificationType.Referenced;
	}
	
	@Override
	public void writeTo(OutputStream out) throws IOException {
		if(null == FileType) {
			throw new NullPointerException("File type cannot be null");
		}
		if(null == Path) {
			throw new NullPointerException("Path cannot be null");
		}
		StringBuilder builder = new StringBuilder();
		builder.append('M').append(' ').append(FileType.getOctalRepresentation());
		if(Type == FileModificationType.Inline) {
			builder.append(' ').append("inline");
			builder.append(' ').append(Path).append("\n");
			out.write(builder.toString().getBytes());
			Content.writeTo(out);
		} else if (Type == FileModificationType.Referenced) {
			if(Content instanceof Markable) {
				Markable marked = (Markable)Content;
				builder.append(' ').append(marked.getMarkID());
				builder.append(' ').append(Path).append("\n");
				out.write(builder.toString().getBytes());
			} else {
				throw new IOException("The content is not a Blob or a markable Git object");
			}
		} else {
			throw new IOException("Unknown File modification type " + Type);
		}
	}

	public void setFileType(GitFileType type) {
		FileType = type;
	}

	@Override
	public boolean isInline() {
		return Type == FileModificationType.Inline;
	}

	@Override
	public MarkID getMark() {
		if(Content instanceof Markable) {
			return ((Markable)Content).getMarkID();
		}
		return null;
	}
	
}
