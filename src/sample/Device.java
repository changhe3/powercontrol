package sample;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;

/**
 * Created by apple on 5/1/16.
 */
public class Device implements Cloneable {

    private final SimpleLongProperty mac;
    private final SimpleStringProperty remark;
    private final SimpleBooleanProperty isOn;
    private final SimpleFloatProperty power;

    Device() {
        mac = new SimpleLongProperty(0);
        remark = new SimpleStringProperty(null);
        isOn = new SimpleBooleanProperty(false);
        power = new SimpleFloatProperty(0.0f);
    }

    Device(long mac, String remark, boolean isOn, float power) {
        this.mac = new SimpleLongProperty(mac);
        this.remark = new SimpleStringProperty(remark);
        this.isOn = new SimpleBooleanProperty(isOn);
        this.power = new SimpleFloatProperty(power);
    }

    Device(Device device) {
        this(device.getMac(), device.getRemark(), device.getIsOn(), device.getPower());
    }

    public long getMac() {
        return mac.get();
    }

    public void setMac(long mac) {
        this.mac.set(mac);
    }

    public SimpleLongProperty macProperty() {
        return mac;
    }

    public String getRemark() {
        return remark.get();
    }

    public void setRemark(String remark) {
        this.remark.set(remark);
    }

    public SimpleStringProperty remarkProperty() {
        return remark;
    }

    public boolean getIsOn() {
        return isOn.get();
    }

    public void setIsOn(boolean isOn) {
        this.isOn.set(isOn);
    }

    public SimpleBooleanProperty isOnProperty() {
        return isOn;
    }

    public float getPower() {
        return power.get();
    }

    public void setPower(float power) {
        this.power.set(power);
    }

    public SimpleFloatProperty powerProperty() {
        return power;
    }
}
