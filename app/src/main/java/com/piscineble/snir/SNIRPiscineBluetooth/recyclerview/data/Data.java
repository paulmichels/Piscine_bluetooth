package com.piscineble.snir.SNIRPiscineBluetooth.recyclerview.data;

public class Data {
    private String type, status;
    double value;

    public Data() {
    }

    public Data(String type, double value, String status) {
        this.type = type;
        this.value = value;
        this.status = status;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
