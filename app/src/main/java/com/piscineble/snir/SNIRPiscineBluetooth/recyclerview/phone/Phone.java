package com.piscineble.snir.SNIRPiscineBluetooth.recyclerview.phone;

public class Phone {
    private String text, number;

    public Phone() {

    }

    public Phone(String text, String number) {
        this.text = text;
        this.number = number;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }
}
