package org.ossnoize.git.fastimport;

import org.ossnoize.git.fastimport.enumeration.GitFileType;
import java.io.OutputStream;

public class FastImportFile
{
    private Blob blob;
    private Data data;
    private FileModification modification;

    public FastImportFile(OutputStream out, Data filedata, String fileName){
        data = filedata;
        blob = new Blob(filedata);
        blob.writeTo(out);
        modification = new FileModification(blob);
        modification.setFileType(GitFileType.Normal);
        modification.setPath(fileName);
    }

    public FastImportFile(OutputStream out, Data fileData, Blob fileBlob, FileModification fileModification){
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