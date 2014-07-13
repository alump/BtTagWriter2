/*
 * OutOfSpaceException.java (BT Tag Writer)
 *
 * https://github.com/alump/BtTagWriter
 *
 * Copyright 2011-2013 Sami Viitanen <sami.viitanen@gmail.com>
 * All rights reserved.
 */
package fi.siika.bttagwriter2.writers;

import fi.siika.bttagwriter2.writers.WriteError;

/**
 *
 */
public class OutOfSpaceException extends WriteException {

    public OutOfSpaceException(String msg, Exception source) {
        super(WriteError.TOO_SMALL, source, msg);
    }

    public OutOfSpaceException(String msg) {
        this(msg, null);
    }
}
