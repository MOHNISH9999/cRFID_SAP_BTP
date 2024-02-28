package com.company.crfid_sap_btp.rfid;

public class BarcodeEvent {

    String barcode;

    public BarcodeEvent(String barcode) {
        this.barcode = barcode;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }
}
