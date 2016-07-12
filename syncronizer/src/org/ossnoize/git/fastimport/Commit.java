package org.ossnoize.git.fastimport;

import java.io.IOException;
import java.io.OutputStream;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ossnoize.git.fastimport.enumeration.GitFileType;
import org.ossnoize.git.fastimport.exception.InvalidPathException;

public class Commit implements Markable {
	private final static String COMMIT = "commit";
	private final static String AUTHOR = "author";
	private final static String COMMITTER = "committer";
	private final static String FROM_SP = "from ";
	private final static String MERGE_SP = "merge ";
	private final static SimpleDateFormat DATEFORMAT = new SimpleDateFormat("Z");
	private final static String headFormat = "refs/heads/{0}{1}";

	private Mark mark;
	private String authorName;
	private String authorEmail;
	private String commiterName;
	private String commiterEmail;
	private String reference;
	private Data comment;
	private MarkID from;
	private MarkID merge;
	private DataRef fromRef;
	private Map<String, FileOperation> listOfOperation;
	private Date commitDate;
	private boolean resumeFastImport;
	private boolean written;
  private GitAttributes filesAttributes;

	public Commit(String name, String email, String message, String reference, java.util.Date commitDate) throws IOException {
		if(null == message) {
			throw new NullPointerException("Message cannot be Null");
		}
		commiterName = name;
		commiterEmail = email;
		setComment(message);
		this.reference = reference;
		this.commitDate = commitDate;
		mark = new Mark();
		listOfOperation = new TreeMap<String, FileOperation>();
    filesAttributes = null;
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

	public void setFromRef(DataRef ref) {
		fromRef = ref;
	}
	
	public void setComment(String message) throws IOException {
		comment = new Data();
		comment.writeData(message.getBytes());
	}
	
	public void setMergeCommit(Commit previous) {
		if(null != previous) {
			merge = previous.getMarkID();
		} else {
			merge = null;
		}
	}
	
	public void addFileOperation(FileOperation ops) {
		listOfOperation.put(ops.getPath(), ops);
	}

	@Override
	public void writeTo(OutputStream out) throws IOException {
		if(written) {
			return;
    }
    if (null != filesAttributes) {
      try {
        Data attributeFile = new Data();
        attributeFile.writeData(filesAttributes.toString().getBytes("UTF-8"));
        Blob aMarkedBlob = new Blob(attributeFile);
        aMarkedBlob.writeTo(out);
        FileModification attributes = new FileModification(aMarkedBlob);
        attributes.setFileType(GitFileType.Normal);
        attributes.setPath(".gitattributes");
        this.addFileOperation(attributes);
      } catch (InvalidPathException ex) {
      }
    }
		StringBuilder commitMsg = new StringBuilder();
		commitMsg.append(COMMIT).append(" ").append(MessageFormat.format(headFormat, reference, "")).append('\n');
		out.write(commitMsg.toString().getBytes());
		mark.writeTo(out);
		commitMsg.setLength(0);
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
		if(null != fromRef) {
			out.write(FROM_SP.getBytes());
			fromRef.writeTo(out);
			out.write('\n');
		} else if(null != from) {
			out.write(FROM_SP.getBytes());
			from.writeTo(out);
			out.write('\n');
		} else if(resumeFastImport) {
			out.write(FROM_SP.getBytes());
			out.write(MessageFormat.format(headFormat, reference, "^0").getBytes());
			out.write('\n');
		}
		if(null != merge) {
			out.write(MERGE_SP.getBytes());
			merge.writeTo(out);
			out.write('\n');
		}
		for(FileOperation ops : listOfOperation.values()) {
			ops.writeTo(out);
		}
		out.write('\n');
		written = true;
	}

	@Override
	public MarkID getMarkID() {
		return mark.getID();
	}

	public void resumeOnTopOfRef() {
		resumeFastImport = true;
	}
	
	public List<FileOperation> getFileOperation() {
		return new ArrayList<FileOperation>(listOfOperation.values());
	}
	
	public String getReference() {
		return reference;
	}
  
  public boolean isWritten() {
    return written;
  }
  
  public GitAttributes getAttributes() {
    return filesAttributes;
  }
  
  public void setAttributes(GitAttributes attr) {
    filesAttributes = attr;
  }

  public Date getCommitDate() {
    return commitDate;
  }
  
  
}
