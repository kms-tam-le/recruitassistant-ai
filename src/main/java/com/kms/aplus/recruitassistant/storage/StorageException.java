package com.kms.aplus.recruitassistant.storage;

public class StorageException extends RuntimeException {
	
	private static final long serialVersionUID = 8097454738917929954L;

	public StorageException(String message) {
        super(message);
    }

    public StorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
