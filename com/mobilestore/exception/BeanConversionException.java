package com.mobilestore.exception;

public class BeanConversionException extends SCException {
	private String errorMessage;

	public BeanConversionException(String errorMessage) {
		super(errorMessage);
		this.errorMessage = errorMessage;
	}
	public BeanConversionException( String errorMessage , Throwable cause) {
		super(errorMessage , cause);
		this.errorMessage = errorMessage;
	}

}
