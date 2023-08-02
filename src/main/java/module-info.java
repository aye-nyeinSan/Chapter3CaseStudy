module com.example.ch3casestudy.chapter3casestudy {
    requires javafx.controls;
    requires javafx.fxml;
    requires pdfbox;


    opens com.example.ch3casestudy to javafx.fxml;
    opens com.example.ch3casestudy.controller to javafx.fxml;
    exports com.example.ch3casestudy;
    exports com.example.ch3casestudy.model;
    exports com.example.ch3casestudy.helpers;
    exports com.example.ch3casestudy.controller;
}