/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sync.githelper;

import java.util.regex.Pattern;

/**
 *
 * @author steve
 */
public enum GitAttributeKind {
  FilterLfs("filter=lfs", "filter=lfs"),
  DiffLfs("diff=lfs", "diff=lfs"),
  MergeLfs("merge=lfs", "merge=lfs"),
  CRLF("eol=crlf", "(eol=crlf|crlf|crlf=input)"),
  LF("eol=lf", "eol=lf"),
  Binary("-text", "-text"),
  Ident("ident", "ident"),
  NotAnAttribute("", ".*");
  
  private String Representation;
  private Pattern ParsingPattern;
  
  public String getAttributeType() {
    return Representation;
  }
  
  private GitAttributeKind(String rep, String pattern) {
    Representation = rep;
    ParsingPattern = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
  }
  
  public static GitAttributeKind fromRepresentation(String token) {
    for(GitAttributeKind attr : GitAttributeKind.values())
    {
      if(attr.ParsingPattern.matcher(token).matches()) {
        return attr;
      }
    }
    return NotAnAttribute;
  }
}
