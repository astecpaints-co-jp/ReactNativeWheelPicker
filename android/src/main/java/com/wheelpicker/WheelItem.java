package com.wheelpicker;

public class WheelItem {
    private String label;
    private boolean disabled;

    public WheelItem(String label, boolean disabled) {
        this.label = label;
        this.disabled = disabled;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }
}
