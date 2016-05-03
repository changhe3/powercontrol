package sample;

import com.digi.xbee.api.RemoteXBeeDevice;
import com.digi.xbee.api.XBeeDevice;
import com.digi.xbee.api.exceptions.XBeeException;
import com.digi.xbee.api.models.XBee64BitAddress;
import com.digi.xbee.api.models.XBeeMessage;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class Controller {

    static long delay = 50;
    static ObservableList<Device> devices = FXCollections.synchronizedObservableList(FXCollections.observableArrayList());
    static BlockingQueue<Update> updates = new ArrayBlockingQueue<Update>(10000);
    static ObservableList<String> ports = FXCollections.observableArrayList();
    static SimpleStringProperty port = new SimpleStringProperty("");

    static void update() {
        if (port.get().isEmpty()) return;
        XBeeDevice xbee = new XBeeDevice(port.get(), 9600);
        //System.err.println("updating");
        for (Device device : devices) {
            RemoteXBeeDevice dest = new RemoteXBeeDevice(xbee, new XBee64BitAddress(Long.toUnsignedString(device.getMac(), 16)));
            try {
                xbee.open();
//                try {
//                    Thread.sleep(500);
//                } catch (InterruptedException e) {}
                xbee.sendData(dest, device.getIsOn() ? new byte[]{1} : new byte[]{0});
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    //e.printStackTrace();
                }

                XBeeMessage response = xbee.readDataFrom(dest, 5000);
                Float power = ByteBuffer.wrap(response.getData()).order(ByteOrder.LITTLE_ENDIAN).getFloat();
                //System.err.println(power);
                if (power == device.getPower()) return;
                Device copy = new Device(device);
                copy.setPower(power);
                //System.err.println("stuck?");
                updates.offer(new Update(device, copy));
            } catch (XBeeException e) {
                e.printStackTrace();
            } finally {
                xbee.close();
            }
        }
    }

    @Deprecated
    static void update(Device device) {
        XBeeDevice xbee = new XBeeDevice(port.get(), 9600);
        RemoteXBeeDevice dest = new RemoteXBeeDevice(xbee, new XBee64BitAddress(Long.toUnsignedString(device.getMac(), 16)));
        try {
            xbee.open();
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
            }
            xbee.sendData(dest, device.getIsOn() ? new byte[]{1} : new byte[]{0});
        } catch (XBeeException e) {
            e.printStackTrace();
        } finally {
            xbee.close();
        }
    }

    static class Update {
        Device oldValue;
        Device newValue;

        Update(Device oldValue, Device newValue) {
            this.oldValue = oldValue;
            this.newValue = newValue;
        }
    }
}
