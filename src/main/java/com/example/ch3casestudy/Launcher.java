package com.example.ch3casestudy;

import javafx.application.Application;
import javafx.application.HostServices;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Launcher extends Application {
    public static Stage stage;

    public static HostServices hs;
    @Override
    public void start(Stage stage) throws Exception {
        this.stage =stage;
        hs = getHostServices();
        FXMLLoader fxmlLoader = new FXMLLoader(Launcher.class.getResource("main-view.fxml"));
        Scene scene= new Scene(fxmlLoader.load());
        this.stage.setTitle("Indexer");
        this.stage.setResizable(false);
        this.stage.setScene(scene);
        this.stage.show();
    }
    static void main(String[] args){ launch(args);}
}
