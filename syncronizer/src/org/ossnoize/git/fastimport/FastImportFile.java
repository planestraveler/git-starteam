package org.ossnoize.git.fastimport;

import java.io.IOException;

import org.ossnoize.git.fastimport.enumeration.GitFileType;
import org.ossnoize.git.fastimport.exception.InvalidPathException;
import java.io.OutputStream;

public class FastImportFile
{
    private Blob blob;
    private Data data;
    private FileModification modification;

    public FastImportFile(OutputStream out, Data filedata, String fileName){
        try {
            data = filedata;
            blob = new Blob(filedata);
            blob.writeTo(out);
            modification = new FileModification(blob);
            modification.setFileType(GitFileType.Normal);
            modification.setPath(fileName);
        } catch (InvalidPathException ex) {
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    public FastImportFile(OutputStream out, Data fileData, Blob fileBlob, FileModification fileModification) {
        try{
            data = fileData;
            blob = fileBlob;
            blob.writeTo(out);
            modification = fileModification;
        } catch (IOException e){
            e.printStackTrace();
      }
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