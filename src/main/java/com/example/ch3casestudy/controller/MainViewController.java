package com.example.ch3casestudy.controller;

import com.example.ch3casestudy.Launcher;
import com.example.ch3casestudy.model.FileFreq;
import com.example.ch3casestudy.model.PDFdocument;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

public class MainViewController {
    LinkedHashMap<String, ArrayList<FileFreq>> uniqueSets;

    //private Region dropRegion;
    @FXML
    private ListView<File> inputListView;

   // private ProgressBar progressBar;
    @FXML
    private Button startButton;

    @FXML
    private ListView outputlistView;
    @FXML
    private MenuItem closeBtn;
    @FXML
    private MenuItem deleteBtn;
    @FXML
    private MenuItem aboutBtn;


    public void initialize(){

        closeBtn.setOnAction(event ->{
            Launcher.stage.close();
                }
        );
        deleteBtn.setOnAction(event ->
        {   inputListView.getItems().remove(inputListView.getSelectionModel().getSelectedItem());
            
        });
        
        aboutBtn.setOnAction(actionEvent ->
                {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setHeaderText(null);
                    alert.setTitle("Word Counter");
                    alert.setContentText("Words are split and count its frequency.");
                    alert.showAndWait();

                });

        inputListView.setOnDragOver(dragEvent -> {
                Dragboard db = dragEvent.getDragboard();
                final boolean isAccepted = db.getFiles().get(0).getName().toLowerCase().endsWith(".pdf");
                if(db.hasFiles() && isAccepted){
                    dragEvent.acceptTransferModes(TransferMode.COPY);
                }else {dragEvent.consume();
                }//  it prevents other event handlers (if any) from being notified of the button click event.

        });
        inputListView.setCellFactory(param -> new ListCell<>() { //Custom Cell in ListView
            @Override
            protected void updateItem(File file, boolean empty) {
                super.updateItem(file, empty);
                if (empty || file == null) {
                    setText(null);
                } else {
                    setText(file.getName());
                }
            }
        });

        inputListView.setOnDragDropped(event -> {
            Dragboard dragboard = event.getDragboard();
            boolean success = false;
            if (dragboard.hasFiles()) {
                success = true;
                int total_files = dragboard.getFiles().size();
              //  String  filepath;
                for (int i = 0; i < total_files; i++) {
                    File file = dragboard.getFiles().get(i);
                     //filepath = dragboard.getFiles().get(i).getAbsolutePath();
                    inputListView.getItems().add(file);
                }
            }
            event.setDropCompleted(success);
            event.consume();
        });

        startButton.setOnAction(event -> {
            Parent bgRoot = Launcher.stage.getScene().getRoot();
            Task<Void> processTask = new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    ProgressIndicator pi = new ProgressIndicator(); //Loading indicator
                    VBox box = new VBox(pi);
                    box.setAlignment(Pos.CENTER);
                    Launcher.stage.getScene().setRoot(box);

                    //Multi-Threading
                    ExecutorService executorService = Executors.newFixedThreadPool(4);
                    final ExecutorCompletionService<Map<String, FileFreq>> completionService = new
                            ExecutorCompletionService<>(executorService);
                    List<File> inputListViewItems = inputListView.getItems();
                    int total_files = inputListViewItems.size();
                    Map<String, FileFreq>[] wordMap = new Map[total_files];
                    for (File inputListViewItem : inputListViewItems) {
                         String  filepath = inputListViewItem.getAbsolutePath();
                        try {
                            PDFdocument p = new PDFdocument(filepath);
                            completionService.submit(new WordMapPageTask(p));

                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }
                    for (int i = 0; i < total_files; i++) {
                        try {
                            Future<Map<String, FileFreq>> future = completionService.take();
                            wordMap[i] = future.get();

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    //Merging All words into output listview
                    try {
                        WordMapMergeTask merger = new WordMapMergeTask(wordMap);
                        Future<LinkedHashMap<String, ArrayList<FileFreq>>> future = executorService.submit(merger);

                        uniqueSets = future.get();
                        outputlistView.getItems().addAll(uniqueSets.keySet());
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        executorService.shutdown();
                    }
                    return null;
                }
            };
            processTask.setOnSucceeded(e->{

                Launcher.stage.getScene().setRoot(bgRoot);
            });
            Thread thread = new Thread(processTask);
            thread.setDaemon(true);
            thread.start();

        } );
       //PopUpView
        outputlistView.setOnMouseClicked(event -> {
            ArrayList<FileFreq> listOfLinks = uniqueSets.get(outputlistView.getSelectionModel().getSelectedItem());

            ListView<FileFreq> popupListView = new ListView<>(); //PopUpView
            LinkedHashMap<FileFreq, String> lookupTable = new LinkedHashMap<>();

            for (FileFreq fileFreq : listOfLinks) {
                lookupTable.put(fileFreq, fileFreq.getPath());
               // System.out.println(fileFreq.getPath());//C:\Users\aye29\OneDrive\Desktop\T4G Personal Profile Rubi.pdf
                popupListView.getItems().add(fileFreq);
            }
            popupListView.setPrefHeight(popupListView.getItems().size() * 35);
            popupListView.setOnMouseClicked(innerEvent -> {
                Launcher.hs.showDocument("file:///" + lookupTable.get(popupListView.getSelectionModel().getSelectedItem()));
                popupListView.getScene().getWindow().hide();
            });
            popupListView.setOnKeyPressed(e->
            {
                KeyCode key =e.getCode();
                if(key==KeyCode.ESCAPE){
                    popupListView.getScene().getWindow().hide();
                }
            });
            Popup popup = new Popup();
//            TextField textField = new TextField();
//            textField.setPrefHeight(0);
//            textField.setPrefWidth(0);
//            textField.setPadding(new Insets(0));
            popup.getContent().addAll(popupListView);
            popup.setHeight(popupListView.getItems().size() * 35);
            popup.show(Launcher.stage);
//        textField.requestFocus();
        });

         }


}
