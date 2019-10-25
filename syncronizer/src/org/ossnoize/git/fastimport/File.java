package org.ossnoize.git.fastimport;

public class File
{
    private Blob blob;
    private Data data;
    private FileModification modification;

    public File(OutputStream out, Data filedata, String fileName){
        data = filedata;
        blob = new Blob(filedata);
        blob.writeTo(out);
        modification = new FileModification(blob);
        modification.setFileType(GitFileType.Normal);
        modification.setPath(filename);
    }

    public File(OutputStream out, Data fileData, Blob fileBlob, FileModification fileModification){
        data = fileData;
        blob = fileBlob;
        blob.writeTo(out);
        modification = fileModification;
    }

    public Blob getBlob(){
        return blob;
    }

    public Data getData(){
        return data;
    }

    public FileModification getModification() {
        return modification;
    }
}