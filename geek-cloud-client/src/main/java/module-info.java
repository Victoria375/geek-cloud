module ru.geekbrains.sep22.geekcloudclient {
    requires javafx.controls;
    requires javafx.fxml;
    requires ru.geekbrains;


    opens ru.geekbrains.sep22.geekcloudclient to javafx.fxml;
    exports ru.geekbrains.sep22.geekcloudclient;
}