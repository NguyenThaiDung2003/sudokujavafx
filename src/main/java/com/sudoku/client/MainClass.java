package com.sudoku.client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;

public class MainClass extends Application {
    public static void main(String args[]) {
        launch();
    }//khởi chạy một ứng dụng javafx đọc lập

    @Override
    public void start(Stage stage) throws IOException {

        FXMLLoader fxmlLoader = new FXMLLoader(MainClass.class.getResource("sudoku.fxml"));
        //tải tệp sudoku.fxml
        Scene scene = new Scene(fxmlLoader.load(), 920, 600);//tạo scene
        SudokuController sudokuController = fxmlLoader.getController();//tạo controller từ file
        sudokuController.setupEvents();//thiết lập các trình xử lý sự kiện cho các nút và trường văn bản

        stage.setTitle("Sudoku");//tiêu đề
        stage.setResizable(false);//thay đổi kích thước
        stage.getIcons().add(new Image(getClass().getResourceAsStream("icon.png")));
        stage.setScene(scene);
        stage.show();
    }
}