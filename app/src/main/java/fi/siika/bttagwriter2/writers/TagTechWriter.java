/*
 * TagTechWriter.java (BT Tag Writer)
 *
 * https://github.com/alump/BtTagWriter
 *
 * Copyright 2011-2013 Sami Viitanen <sami.viitanen@gmail.com>
 * All rights reserved.
 */
package fi.siika.bttagwriter2.writers;

import android.nfc.NdefMessage;
import android.nfc.Tag;

import java.io.UnsupportedEncodingException;

import fi.siika.bttagwriter2.data.BtTagGenerator;
import fi.siika.bttagwriter2.data.TagInformation;

/**
 * Interface class for all different specific technology writers
 */
public abstract class TagTechWriter {

    private final static byte TLV_NDEF_MESSAGE = 3;

    /**
     * Interface called to write information to given tag
     *
     * @param tag Tag where information is written
     * @param info Information written
     * @throws WriteException Exception if write fails
     */
    public abstract void writeToTag(Tag tag, TagInformation info)
            throws WriteException;

    /**
     * Put specific close functionality behind this function
     * @param tag Tag which connection is closde
     *
     */
    public abstract void close(Tag tag) throws Exception;

    /**
     * Generate payload with single ndef message. Adds TLV frame for it.
     *
     * @param info      Information used to generate payload
     * @param sizeLimit Limit in bytes
     * @return Payload in byte array
     * @throws WriteException Payload generating issues
     * @throws UnsupportedEncodingException Encoding issues
     */
    protected static byte[] generatePayload(TagInformation info,
                                            int sizeLimit) throws WriteException,
            UnsupportedEncodingException {

        final int SPACE_TAKEN_BY_TLV = 2;

        NdefMessage ndefMessage = BtTagGenerator.generateNdefMessageForBtTag(
                info, (short) (sizeLimit - SPACE_TAKEN_BY_TLV));

        if (ndefMessage == null) {
            throw new OutOfSpaceException("Not enough space for payload");
        }

        // Construct the payload
        byte[] message = ndefMessage.toByteArray();
        int msgLen = message.length;

        if ((msgLen + 2) > sizeLimit) {
            throw new OutOfSpaceException("Not enough space for message");
        }

        byte[] payload = new byte[msgLen + SPACE_TAKEN_BY_TLV];
        payload[0] = TLV_NDEF_MESSAGE;
        payload[1] = (byte) (msgLen);
        System.arraycopy(message, 0, payload, SPACE_TAKEN_BY_TLV, msgLen);
        return payload;
    }

}
