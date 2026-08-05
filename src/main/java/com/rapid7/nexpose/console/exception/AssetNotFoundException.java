package com.rapid7.nexpose.console.exception;

/** Raised when a requested asset does not exist. */
public class AssetNotFoundException extends NexposeException {

    public AssetNotFoundException(String message) {
        super("NEXL-ASSET-001", message);
    }

    public AssetNotFoundException(String message, Throwable cause) {
        super("NEXL-ASSET-001", message, cause);
    }
}
