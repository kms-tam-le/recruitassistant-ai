package com.kms.aplus.recruitassistant.storage;

public class FileNotFoundException extends StorageException {
	
	private static final long serialVersionUID = 1L;

	public FileNotFoundException(String message) {
        super(message);
    }

    public FileNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}