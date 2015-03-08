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

/**
 * Note: this class has a natural ordering that is inconsistent with equals.
 * @author Steve Tousignant <s.tousignant@gmail.com>
 *
 */
public final class CommitInformation implements Comparable<CommitInformation> {

	private long time;
	private int uid;
	private String comment;
	private String path;
	private boolean fileDelete;

	public CommitInformation(long time, int uid, String comment, String path) {
		this.time = time;
		this.uid = uid;
		this.comment = comment.trim();
		this.path = path;
		this.fileDelete = false;
	}
	
	public long getTime() {
		return time;
	}
	
	public int getUid() {
		return uid;
	}
	
	public String getPath() {
		return path;
	}
	
	public String getComment() {
		return comment;
	}
	
	/**
	 * This method will return if the 2 commit information that are compared are equivalent
	 * @return true if the uid are the same and the 2 comment correlate;
	 */
	public boolean equivalent(CommitInformation info) {
		return uid == info.uid &&
				(comment.length() == 0 || info.comment.length() == 0 || info.comment.equalsIgnoreCase(comment));
	}
	
	@Override
	public String toString() {
		return "CommitInfo: " + getTime() + " - " + getUid() + " - " + getComment() + " - " + getPath();
	}

	@Override
	public boolean equals(Object obj) {
		if(obj instanceof CommitInformation) {
			CommitInformation info = (CommitInformation) obj;
			return time == info.getTime() && uid == info.getUid() && comment.equalsIgnoreCase(info.getComment());
		}
		return false;
	}

	@Override
	public int compareTo(CommitInformation o) {
		if(time == o.time) {
			if(uid == o.uid) {
				if(comment.length() == 0) {
					return path.compareTo(o.getPath());
				} else if (o.comment.length() == 0) {
					return path.compareTo(o.getPath());
				} else if (comment.equalsIgnoreCase(o.comment)) {
					return path.compareTo(o.getPath());
				}
				return comment.compareTo(o.comment);
			} else if (uid > o.uid) {
				return 1;
			}
			return -1;
		} else if (time > o.time) {
			return 1;
		}
		return -1;
	}

	public void setFileDelete(boolean b) {
		fileDelete = b;
	}

	public boolean isFileDelete() {
		return fileDelete;
	}
}
