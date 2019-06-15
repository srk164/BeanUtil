package com.mobilestore.exception;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SCException extends Exception 
{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -1525642739463591688L;
	
	protected Date timestamp;
	protected String errorMessage;

	public SCException ()
	{
		timestamp = new Date();
	}
	
	public SCException(String errorMessage) 
	{
		super(errorMessage);
		timestamp = new Date();
		this.errorMessage = errorMessage;
	}
	
	public SCException( String errorMessage , Throwable cause) {
		super(errorMessage , cause);
		this.errorMessage = errorMessage;
		timestamp = new Date();
		if (cause != null)
		{
			errorMessage = errorMessage + " /n Casue Message: "+cause.getMessage();
		}
	}
	
	public String getErrorMessage()
	{
		return errorMessage;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() 
	{
		DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");
		String strDate = dateFormat.format(timestamp);
		return  strDate+ " " + this.getClass().getName()+" ["
				+ (errorMessage != null ? "errorMessage=" + errorMessage : "")
				+ "]";
	}
}
