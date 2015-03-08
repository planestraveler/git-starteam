/*****************************************************************************
 * All public interface based on Starteam API are a property of Borland, 
 * those interface are reproduced here only for testing purpose. You should
 * never use those interface to create a competitive product to the Starteam
 * Server. 
 * 
 * The implementation is given AS-IS and should not be considered a reference 
 * to the API. The behavior on a lots of method and class will not be the
 * same as the real API. The reproduction only seek to mimic some basic 
 * operation. You will not found anything here that can be deduced by using
 * the real API.
 * 
 * Fake-Starteam is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *****************************************************************************/
package com.starbase.starteam;

/**
 *
 * @author steve
 */
public class NoSuchPropertyException extends RuntimeException {
  
  public NoSuchPropertyException(String message)
  {
    super(message);
    this.property = "Undefined";
    this.typename = "Undefined";
  }
  
  public NoSuchPropertyException(String typename, String property)
  {
    super();
    this.typename = typename;
    this.property = property;
    this.propertyId = 0;
  }
  
  public NoSuchPropertyException(String typename, int propertyId)
  {
    this.typename = typename;
    this.property = "Undefined";
    this.propertyId = propertyId;
  }

  @Override
  public String getMessage() {
    return super.getMessage();
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if(obj instanceof NoSuchPropertyException)
    {
      NoSuchPropertyException other = (NoSuchPropertyException)obj;
      return other.typename.equals(typename) &&
        other.property.equals(property) && 
        other.propertyId == propertyId;
    }
    return false;
  }
  
  public String getPropertyName() {
    return property;
  }
  
  public int getPropertyID() {
    return propertyId;
  }
  
  private String typename;
  private String property;
  private int propertyId;
}
