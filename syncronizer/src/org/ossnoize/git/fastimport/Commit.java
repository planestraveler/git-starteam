package org.ossnoize.git.fastimport;

import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Commit implements Markable {
	private final static String COMMIT = "commit";
	private final static String AUTHOR = "author";
	private final static String COMMITTER = "committer";
	private final static String FROM_SP = "from ";
	private final static String MERGE_SP = "merge ";
	private final static SimpleDateFormat DATEFORMAT = new SimpleDateFormat("Z");

	private Mark mark;
	private String authorName;
	private String authorEmail;
	private String commiterName;
	private String commiterEmail;
	private String reference;
	private Data comment;
	private MarkID from;
	private MarkID merge;
	private List<FileOperation> listOfOperation;
	private Date commitDate;

	public Commit(String name, String email, String message, String reference, java.util.Date commitDate) throws IOException {
		if(null == message) {
			throw new NullPointerException("Message cannot be Null");
		}
		commiterName = name;
		commiterEmail = email;
		comment = new Data();
		comment.writeData(message.getBytes());
		this.reference = reference;
		this.commitDate = commitDate;
		mark = new Mark();
		listOfOperation = new ArrayList<FileOperation>();
	}

	public void setAuthor(String name, String email) {
		authorName = name;
		authorEmail = email;
	}
	
	public void setFromCommit(Commit previous) {
		if(null != previous) {
			from = previous.getMarkID();
		} else {
			from = null;
		}
	}
	
	public void setMergeCommit(Commit previous) {
		if(null != previous) {
			merge = previous.getMarkID();
		} else {
			merge = null;
		}
	}
	
	public void addFileOperation(FileOperation ops) {
		listOfOperation.add(ops);
	}

	@Override
	public void writeTo(OutputStream out) throws IOException {
		StringBuilder commitMsg = new StringBuilder();
		commitMsg.append(COMMIT).append(" ").append(reference).append('\n');
		out.write(commitMsg.toString().getBytes());
		mark.writeTo(out);
		commitMsg = new StringBuilder();
		if(null != authorName  && null != authorEmail) {
			commitMsg.append(AUTHOR).append(' ').append(authorName).append(' ')
					 .append('<').append(authorEmail).append('>').append(' ')
					 .append(commitDate.getTime() / 1000).append(' ').append(DATEFORMAT.format(commitDate))
					 .append('\n');
		}
		commitMsg.append(COMMITTER).append(' ').append(commiterName).append(' ')
				 .append('<').append(commiterEmail).append('>').append(' ')
				 .append(commitDate.getTime() / 1000).append(' ').append(DATEFORMAT.format(commitDate))
				 .append('\n');
		out.write(commitMsg.toString().getBytes());
		comment.writeTo(out);
		if(null != from) {
			out.write(FROM_SP.getBytes());
			from.writeTo(out);
			out.write('\n');
		}
		if(null != merge) {
			out.write(MERGE_SP.getBytes());
			merge.writeTo(out);
			out.write('\n');
		}
		for(FileOperation ops : listOfOperation) {
			ops.writeTo(out);
		}
		out.write('\n');
	}

	@Override
	public MarkID getMarkID() {
		return mark.getID();
	}
}
