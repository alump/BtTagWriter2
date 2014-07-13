/*
 * BtTagGenerator.java (BT Tag Writer)
 *
 * https://github.com/alump/BtTagWriter
 *
 * Copyright 2011-2013 Sami Viitanen <sami.viitanen@gmail.com>
 * All rights reserved.
 */

package fi.siika.bttagwriter2.data;

import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.util.Log;

import java.io.UnsupportedEncodingException;

import fi.siika.bttagwriter2.writers.OutOfSpaceException;

/**
 * BtTagCreator is used to construct NdefMessages written to the tags
 *
 * @author Sami Viitanen <sami.viitanen@gmail.com>
 */
public class BtTagGenerator {

    /**
     * Debug TAG
     */
    private final static String TAG = BtTagGenerator.class.getSimpleName();

    /**
     * Let's use "0" (0x30) as our Record ID in Handover's Alt. Carrier
     * "Payload type"
     */
    private final static byte RECORD_ID_BYTE = 0x30;

    /**
     * Generate simple pairing message
     *
     * @param info Tag information written
     * @param sizeLimit Will try to keep size of message lower than this limit.
     *                  If -1 will not do any size check and will generate full size message.
     * @return NDEF message generated
     * @throws fi.siika.bttagwriter2.writers.OutOfSpaceException Size limit too small for content
     * @throws UnsupportedEncodingException Encoding issues
     */
    public static NdefMessage generateNdefMessageForBtTag(TagInformation info,
                                                          int sizeLimit) throws OutOfSpaceException, UnsupportedEncodingException {

        if (info.getType() == null) {
            throw new IllegalArgumentException("Type missing");
        }

        NdefRecord media = null;

        BtSecureSimplePairing.Data content = new BtSecureSimplePairing.Data();
        content.setName(info.name);
        content.setAddress(info.address);
        if(info.leDevice) {
            // TODO: Now this is always marked as peripheral. Not sure how to resolve this.
            content.setLeRole(BtSecureSimplePairing.Data.LeRole.PERIPHERAL_ONLY);
        }
        if (info.deviceClass != null) {
            content.setDeviceClass(info.deviceClass);
        }
        // TODO: Pin (if possible)

        final String mimeString = info.leDevice ? BtSecureSimplePairing.MIME_LE_TYPE : BtSecureSimplePairing.MIME_EP_TYPE;

        byte[] mime = mimeString.getBytes("UTF-8");

        int minSize = 4 + mime.length
                + BtSecureSimplePairing.MIN_SIZE_IN_BYTES;

        int mediaSizeLimit = 1024; // Safe max value

        if (sizeLimit > 0) {
            if (minSize > sizeLimit) {
                Log.e(TAG, "Not enough space!");
                throw new OutOfSpaceException(
                        "Tag is too small for NDEF content: " + minSize
                                + " > " + sizeLimit);
            }

            mediaSizeLimit = sizeLimit - 2 - mime.length;
        }

        // Only use record ID if we use full handover format
        byte[] recordId = null;
        if (info.getType() == TagType.HANDOVER) {
            recordId = new byte[]{RECORD_ID_BYTE};
        }

        media = new NdefRecord(NdefRecord.TNF_MIME_MEDIA, mime, recordId,
                BtSecureSimplePairing.generate(content, (short) (mediaSizeLimit - 4)));

        if (info.getType() == TagType.HANDOVER) {
            return new NdefMessage(new NdefRecord[]{
                    generateHandoverSelectRecord(), media});
        } else if (info.getType() == TagType.SIMPLIFIED) {
            return new NdefMessage(new NdefRecord[]{media});
        } else {
            Log.e(TAG, "Unsupported type: " + info.getType().toString());
            return new NdefMessage(new NdefRecord[]{media});
        }
    }

    /**
     * Get record ID of pairing data.
     *
     * @param type Type of tag
     * @return Null if not used, byte array of ID is not needed.
     */
    private static byte[] getRecordId(TagType type) {
        byte ret[] = null;
        if (type == TagType.HANDOVER) {
            ret = new byte[]{0x30};
        }
        return ret;
    }

    @SuppressWarnings( "deprecation" )
    private static NdefRecord generateHandoverSelectRecord() {
        byte[] ac = new NdefRecord(NdefRecord.TNF_WELL_KNOWN,
                NdefRecord.RTD_ALTERNATIVE_CARRIER, new byte[0],
                generateAlternativeCarrierData()).toByteArray();

        byte[] data = new byte[1 + ac.length];
        data[0] = 0x12;
        System.arraycopy(ac, 0, data, 1, ac.length);

        return new NdefRecord(NdefRecord.TNF_WELL_KNOWN,
                NdefRecord.RTD_HANDOVER_SELECT, new byte[0], data);
    }

    /**
     * Generates AC data used in Handover
     *
     * @return Alternative carrier data
     */
    private static byte[] generateAlternativeCarrierData() {
        //1st byte: 0x01 = active target
        //2nd byte: 0x01 = ndef record id
        //3rd byte: ID of record (eg. 0x30 == "0")
        //4th byte: 0x00 = RFU
        return new byte[] {0x01, 0x01, RECORD_ID_BYTE, 0x00};
    }
}
