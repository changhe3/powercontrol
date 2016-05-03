package sample;

import gnu.io.CommPortIdentifier;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.Enumeration;

import static sample.Controller.*;

public class Main extends Application {

    final TableView<Device> tableView = new TableView<>();
    final TextField addDeviceMAC = new TextField();
    final TextField addDeviceRemark = new TextField();
    final Button addButton = new Button("add");
    final Button removeButton = new Button("remove");
    final Button toggleButton = new Button("turn on/off");
    final Button findPortButton = new Button("Refresh Ports");
    final ChoiceBox<String> choiceBox = new ChoiceBox<>();
    final Button setPortButton = new Button("Set Serial Port");
    final Label portLabel = new Label();


    {
        addDeviceMAC.setPromptText("Enter the MAC address of your device");
        addDeviceRemark.setPromptText("Enter the name of your device");
    }

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("sample.fxml"));
        primaryStage.setTitle("Power Control");

        GridPane pane = new GridPane();
        ColumnConstraints c1 = new ColumnConstraints(), c2 = new ColumnConstraints();
        c1.setPercentWidth(50);
        c2.setPercentWidth(50);
        pane.getColumnConstraints().addAll(c1, c2);

        Scene scene = new Scene(pane);
        pane.setPrefSize(800, 600);

        pane.add(tableView, 0, 0, 1, 3);
        pane.setHgap(10);
        pane.setVgap(10);

        tableView.setEditable(false);
        tableView.setItems(devices);

        TableColumn<Device, String> macColumn = new TableColumn<>();
        macColumn.setText("MAC Address");
        macColumn.setCellValueFactory(param ->
                Bindings.createStringBinding(() -> Long.toUnsignedString(param.getValue().getMac(), 16).toUpperCase(),
                        param.getValue().macProperty()));

        TableColumn<Device, String> remarkColumn = new TableColumn<>();
        remarkColumn.setText("Remark");
        remarkColumn.setCellValueFactory(new PropertyValueFactory<>("remark"));

        TableColumn<Device, String> statusColumn = new TableColumn<>();
        statusColumn.setText("Status");
        statusColumn.setCellValueFactory(param ->
                Bindings.createStringBinding(() ->
                        param.getValue().getIsOn() ? "on" : "off", param.getValue().isOnProperty()));

        TableColumn<Device, String> powerColumn = new TableColumn<>();
        powerColumn.setText("Power");
        powerColumn.setCellValueFactory(param -> Bindings.createStringBinding(() -> String.format("%.4f", param.getValue().getPower()), param.getValue().powerProperty()));

        tableView.getColumns().addAll(macColumn, remarkColumn, statusColumn, powerColumn);


        addButton.setOnAction(this::handleAddButton);
        removeButton.setOnAction(this::handleRemoveButton);
        toggleButton.setOnAction(this::handleToggleButton);
        findPortButton.setOnAction(this::handleFindPort);

        VBox vBox = new VBox(10);
        HBox hBox = new HBox(20);
        hBox.getChildren().addAll(addButton, removeButton);
        vBox.getChildren().addAll(addDeviceMAC, addDeviceRemark, hBox);
        pane.add(vBox, 1, 0);
        pane.add(toggleButton, 1, 1);

        HBox hBox2 = new HBox(20);
        VBox vBox2 = new VBox(10);
        hBox2.getChildren().addAll(choiceBox, findPortButton);
        vBox2.getChildren().addAll(hBox2, setPortButton);

        VBox vBox3 = new VBox(10);
        vBox3.getChildren().addAll(new Label("Current Port: "), portLabel);

        portLabel.textProperty().bind(port);

        setPortButton.setOnAction(event -> {
            String p = choiceBox.getValue();
            port.setValue(p);
        });

        pane.add(vBox2, 1, 2);
        pane.add(vBox3, 0, 3);

        findPortButton.fire();

        Task<Integer> task = new Task<Integer>() {
            @Override
            protected Integer call() throws Exception {
                int iterations;
                for (iterations = 0; true; iterations++) {
                    try {
                        Thread.sleep(delay);
                        update();
                    } catch (InterruptedException e) {
                        return iterations;
                    }
                }
            }
        };

        new Thread(task).start();

        final LongProperty lastUpdate = new SimpleLongProperty(0);
        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (now - lastUpdate.get() > 1000) {
                    try {
                        if (updates.isEmpty()) return;
                        Controller.Update update = updates.take();
                        update.oldValue.setPower(update.newValue.getPower());
                    } catch (InterruptedException e) {
                        System.err.println("interrupted");
                        return;
                    }
                }
                lastUpdate.set(now);
            }
        };

        timer.start();

        primaryStage.setOnCloseRequest(event -> task.cancel());

        primaryStage.setScene(scene);
        primaryStage.show();
    }

    void handleAddButton(ActionEvent event) {
        long macAddr = 0;
        String addr = addDeviceMAC.getText();
        try {
            if (addr.length() > 16) throw new NumberFormatException();
            macAddr = Long.parseUnsignedLong(addr, 16);
        } catch (NumberFormatException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("java.lang.NumberFormatException");
            alert.setHeaderText(addr + " is not a legal hexadecimal string of 64 bits!");
            alert.show();
            return;
        }
        String remark = addDeviceRemark.getText();
        devices.add(new Device(macAddr, remark, false, 0.0f));
        addDeviceMAC.clear();
        addDeviceRemark.clear();
    }

    void handleRemoveButton(ActionEvent event) {
        Device selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        if (selected.getIsOn()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Error");
            alert.setHeaderText("You must turn your device off before removing it!");
            alert.show();
            return;
        }

        devices.remove(selected);
        tableView.getSelectionModel().clearSelection();
    }

    void handleToggleButton(ActionEvent event) {
        Device selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        selected.setIsOn(!selected.getIsOn());
        //update(selected);
    }

    void handleFindPort(ActionEvent event) {
        choiceBox.getItems().clear();
        Enumeration<CommPortIdentifier> ids = CommPortIdentifier.getPortIdentifiers();
        while (ids.hasMoreElements()) {
            CommPortIdentifier id = ids.nextElement();
            if (id.getPortType() == CommPortIdentifier.PORT_SERIAL) choiceBox.getItems().add(id.getName());
        }
    }
}
