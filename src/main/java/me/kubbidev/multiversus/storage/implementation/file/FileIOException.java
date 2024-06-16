package me.kubbidev.multiversus.storage.implementation.file;

import java.io.IOException;

public class FileIOException extends IOException {

    public FileIOException(String fileName, Throwable cause) {
        super("Exception thrown whilst reading/writing file: " + fileName, cause);
    }

}