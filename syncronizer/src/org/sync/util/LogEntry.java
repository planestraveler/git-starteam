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
package org.sync.util;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import org.ossnoize.git.fastimport.DataRef;
import org.ossnoize.git.fastimport.enumeration.GitFileType;
import org.sync.util.enumeration.FileStatusStyle;

public class LogEntry {
	public enum TypeOfModification {
		Addition("A"),
		Modification("M"),
		Delete("D"),
		Rename("R[0-9]{1,3}"),
		Copy("C[0-9]{1,3}");
		
		private Pattern isOfTypePattern;
		private TypeOfModification(String pattern) {
			isOfTypePattern = Pattern.compile(pattern);
		}

		public static TypeOfModification getFromRepresentation(String token) {
			for(TypeOfModification type : values()) {
				if(type.isOfTypePattern.matcher(token).matches()) {
					return type;
				}
			}
			throw new UnsupportedOperationException("Unknown type of entry <" + token + ">");
		}
	}
	
	public class FileEntry {
		private String path;
		private String renamed;
		private TypeOfModification modification;
		private GitFileType fromType;
		private GitFileType toType;
		private int diffRatio;
		private SmallRef fromSha;
		private SmallRef toSha;
		
		private FileEntry(String path, String renamed, TypeOfModification type, int diffRatio, GitFileType fromType, GitFileType toType, SmallRef fromSha, SmallRef toSha) {
			this.path = path;
			this.renamed = renamed;
			this.diffRatio = diffRatio;
			this.modification = type;
			this.fromType = fromType;
			this.toType = toType;
			this.fromSha = fromSha;
			this.toSha = toSha;
		}
		
		private FileEntry(String path, TypeOfModification mod, GitFileType fromType, GitFileType toType, SmallRef fromSha, SmallRef toSha) {
			this.path = path;
			this.modification = mod;
			this.diffRatio = -1;
			this.fromType = fromType;
			this.toType = toType;
			this.fromSha = fromSha;
			this.toSha = toSha;
		}

		public String getPath() {
			return path;
		}
		
		public String renamedTo() {
			return renamed;
		}
		
		public TypeOfModification getTypeOfModification() {
			return modification;
		}
		
		public int getDiffRatio() {
			return diffRatio;
		}
		
		public boolean hasTypeChange() {
			return fromType != toType;
		}
		
		public GitFileType getFromType() {
			return fromType;
		}
		
		public GitFileType getToType() {
			return toType;
		}
		
		@Override
		public String toString() {
			String base = ":" + fromType.getOctalRepresentation() + " " + toType.getOctalRepresentation() + 
					" " + fromSha.getRef() + "... " + toSha.getRef() + "... ";
			if(getTypeOfModification() == TypeOfModification.Copy || getTypeOfModification() == TypeOfModification.Rename)
				return base + "\t" + getTypeOfModification() + "\t" + getPath() + "\t" + renamedTo();
			return base + "\t" + getTypeOfModification() + "\t" + getPath();
		}
	}
	
	private DataRef commitRef;
	private String author;
	private java.util.Date timeOfCommit;
	private String comment;
	private ArrayList<FileEntry> files;
	
	public LogEntry(DataRef commitRef) {
		this.commitRef = commitRef;
		this.files = new ArrayList<FileEntry>();
		this.comment = "";
	}
	
	public void parseStatusLine(FileStatusStyle style, String line) {
		if(style == FileStatusStyle.GitRaw) {
			if(!line.startsWith(":")) {
				throw new UnsupportedOperationException("The line <" + line + "> does not seem to be a valid git status line");
			}
			StringTokenizer tokenList = new StringTokenizer(line, " ");
			int tokenId = 0;
			GitFileType fromType = null;
			GitFileType toType = null;
			SmallRef fromRef = null;
			SmallRef toRef = null;
			TypeOfModification type = null;
			int ratio = 100;
			String pathA = "";
			String pathB = "";
			while(tokenList.hasMoreTokens()) {
				String token; 
				switch(tokenId) {
				case 0:
					token = tokenList.nextToken(" ");
					fromType = GitFileType.fromOctal(token.substring(1).trim()); // skip the first :
					break;
				case 1:
					token = tokenList.nextToken(" ");
					toType = GitFileType.fromOctal(token.trim());
					break;
				case 2:
					token = tokenList.nextToken(" ");
					fromRef = new SmallRef(token.replaceAll("\\p{Punct}", "").trim());
					break;
				case 3:
					token = tokenList.nextToken(" ");
					toRef = new SmallRef(token.replaceAll("\\p{Punct}", "").trim());
					break;
				case 4:
					token = tokenList.nextToken("\t").trim();
					type = TypeOfModification.getFromRepresentation(token);
					if(type == TypeOfModification.Copy || type == TypeOfModification.Rename) {
						ratio = Integer.parseInt(token.substring(1));
					}
					break;
				case 5:
					pathA = tokenList.nextToken("\t");
					break;
				case 6:
					pathB = tokenList.nextToken("\t");
					break;
				default:
					throw new UnsupportedOperationException("unexpected token <" + tokenList.nextToken() + ">");
				}
				++tokenId;
			}
			if(type == TypeOfModification.Copy || type == TypeOfModification.Rename) {
				files.add(new FileEntry(pathA, pathB, type, ratio, fromType, toType, fromRef, toRef));
			} else {
				files.add(new FileEntry(pathA, type, fromType, toType, fromRef, toRef));
			}
		} else {
			throw new UnsupportedOperationException("Style " + style + " is not yet supported");
		}
	}
	
	public DataRef getCommitRef() {
		return commitRef;
	}
	
	public void setAuthor(String author) {
		this.author = author;
	}
	
	public String getAuthor() {
		return author;
	}
	
	public void appendComment(String comment) {
		this.comment += comment;  
	}
	
	public String getComment() {
		return comment.trim();
	}
	
	public void setTimeOfCommit(java.util.Date timeOfCommit) {
		this.timeOfCommit = timeOfCommit;
	}
	
	public java.util.Date getTimeOfCommit() {
		return timeOfCommit;
	}
	
	public List<FileEntry> getFilesEntry() {
		return files;
	}
}
