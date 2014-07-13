/*
 * IOFailureException.java (BT Tag Writer)
 *
 * https://github.com/alump/BtTagWriter
 *
 * Copyright 2011-2013 Sami Viitanen <sami.viitanen@gmail.com>
 * All rights reserved.
 */
package fi.siika.bttagwriter2.writers;

import java.io.IOException;

import fi.siika.bttagwriter2.writers.WriteError;

/**
 *
 */
public class IOFailureException extends WriteException {

    public IOFailureException(WriteError errorCode, IOException source, String msg) {
        super(errorCode, source, msg);
    }

    public IOException getSource() {
        return (IOException) super.getSource();
    }

    public String getLongMessage() {
        String ret = this.getMessage();
        if (getSource() != null) {
            ret += ", source: " + getSource().getMessage();
        }
        return ret;
    }
}
