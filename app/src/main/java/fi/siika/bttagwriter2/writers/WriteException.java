/*
 * WriteException.java (BT Tag Writer)
 *
 * https://github.com/alump/BtTagWriter
 *
 * Copyright 2011-2013 Sami Viitanen <sami.viitanen@gmail.com>
 * All rights reserved.
 */

package fi.siika.bttagwriter2.writers;

import fi.siika.bttagwriter2.writers.WriteError;

/**
 * Exception used when write fails
 */
public class WriteException extends Exception {
    private final WriteError errorCode;
    private final Exception source;

    public WriteException(WriteError error) {
        this(error, null, "Tag write failed.");
    }

    public WriteException(WriteError error, String message) {
        this(error, null, message);
    }

    public WriteException(WriteError error, Exception source, String message) {
        super(message);
        this.source = source;
        errorCode = error;
    }

    public WriteError getErrorCode() {
        return errorCode;
    }

    public Exception getSource() {
        return source;
    }
}
