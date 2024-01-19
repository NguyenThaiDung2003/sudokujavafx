package com.sudoku.client;

import com.sudoku.InitInterface;
import com.sudoku.server.InitImpl;
import com.sudoku.server.GenerateSudoku;
import com.sudokugui.*;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressBar;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class SudokuController  {

    @FXML
    private StackPane stackPane;

    @FXML
    private VBox vBox;

    @FXML
    private HBox hBox;

    private Header header;
    private Grid grid;
    private ControlPanel controlPanel;

    private final InitInterface serverObject=new InitImpl();

    public void initialize() {
        createHeader();
        createGrid();
        createControlPanel(createDifficultyMenu());

        vBox.getChildren().add(0, header);
        hBox.getChildren().add(grid);
        hBox.getChildren().add(controlPanel);
    }
    /*
    Trong phương thức khởi tạo, mã tạo một phiên bản của lớp Header, được sử dụng để nhập tên người dùng
     thời gian và mức độ khó cũng như bắt đầu và dừng bộ hẹn giờ. Nó cũng đặt chiều rộng tối thiểu của tiêu đề và thêm một số phần đệm.

Tiếp theo, nó tạo một thể hiện của lớp Grid, được sử dụng để hiển thị câu đố Sudoku.
Phương thức createDifficultyMenu được gọi để tạo một menu cho phép người dùng chọn mức độ khó của câu đố.

Cuối cùng, phiên bản Header được thêm vào đầu vùng chứa VBox, phiên bản Grid được thêm vào giữa và phiên bản ControlPanel được thêm vào cuối vùng chứa HBox.
     */

    public void setupEvents() {
        vBox.getScene().addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            grid.handleKeys(event.getCode());
            event.consume();
        });
    }

    /**
     * Creates the header of the GUI.
     */
    private void createHeader() {
        header = new Header();
        header.changeDifficultyLabel("Normal");
        header.setMinWidth(900);//determines the width
        header.setPadding(new Insets(0, 30, 0, 50));//sets the padding
        header.startTime();
    }

    /**
     * Creates the Sudoku grid.
     */
    private void createGrid() {
        grid = new Grid(header, new Runnable() {
            /**
             * Writes the current game information to a log file.
             */
            @Override
            public void run() {
                String line = "Username: " + header.getUsername() + ", Time: " + header.getElapsedTime() + ", " +
                        "Difficulty: " + header.getDifficultyLabel() + ", Solved";
                try {
                    serverObject.writeToFile(line);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        grid.setAlignment(Pos.CENTER_LEFT);

        int sudokuGrid[][] = new int[0][];

        try {
            sudokuGrid = serverObject.generateSudoku(GenerateSudoku.Difficulty.NORMAL);
        } catch (Exception e) {
            e.printStackTrace();
        }

        grid.loadGrid(sudokuGrid);
    }

    /**
     * Creates a menu for selecting the difficulty level of the Sudoku puzzle.
     *
     * @return a menu containing the different difficulty levels
     */
    private DifficultyMenu createDifficultyMenu() {

        // create a new DifficultyMenu
        final DifficultyMenu difficultyMenu = new DifficultyMenu();

        // set the action for every menu item
        for (MenuItem item : difficultyMenu.getItems()) {
            item.setOnAction(event -> {
                // create a new Task to generate a new Sudoku puzzle
                final Task<Void> loadNewSudokuTask = new Task<Void>() {
                    @Override
                    protected Void call() throws Exception {
                        // get the selected difficulty level
                        GenerateSudoku.Difficulty difficulty = null;
                        switch (item.getText()) {
                            case "Easy":
                                difficulty = GenerateSudoku.Difficulty.EASY;
                                header.changeDifficultyLabel("Easy");
                                break;
                            case "Normal":
                                difficulty = GenerateSudoku.Difficulty.NORMAL;
                                header.changeDifficultyLabel("Normal");
                                break;
                            case "Hard":
                                difficulty = GenerateSudoku.Difficulty.HARD;
                                header.changeDifficultyLabel("Hard");
                                break;
                            case "Expert":
                                difficulty = GenerateSudoku.Difficulty.EXPERT;
                                header.changeDifficultyLabel("Expert");
                                break;
                        }

                        // send information to the server if a new Sudoku puzzle is requested and the current one is not solved
                        if (!grid.isSolved()) {
                            String line = "Username: " + header.getUsername() + ", Time: " + header.getElapsedTime() + ", " +
                                    "Difficulty: " + header.getDifficultyLabel() + ", Not Solved";
                            serverObject.writeToFile(line);
                        }

                        // generate a new Sudoku puzzle
                        int newSudokuGrid[][] = serverObject.generateSudoku(difficulty);

                        // update the grid with the new puzzle
                        Platform.runLater(() -> {
                            grid.loadGrid(newSudokuGrid);
                            grid.removeAllBackgrounds(true);
                        });

                        // stop the timer and reset it
                        header.stopTime();
                        header.resetTime();
                        header.startTime();
                        return null;
                    }
                };

                // create a ProgressBar and a Label to display the progress and status of the task
                final ProgressBar progressBar = new ProgressBar();
                progressBar.progressProperty().bind(loadNewSudokuTask.progressProperty());
                final Label statusLabel = new Label("Loading");
                statusLabel.textProperty().bind(loadNewSudokuTask.messageProperty());

                // create a VBox to hold the ProgressBar and Label
                final VBox progressBox = new VBox(5, statusLabel, progressBar);
                progressBox.setAlignment(Pos.CENTER);

                // add the VBox to the StackPane
                stackPane.getChildren().add(progressBox);

                // start the task in a new thread
                new Thread(loadNewSudokuTask).start();

                // remove the VBox from the StackPane when the task completes
                loadNewSudokuTask.setOnSucceeded(event1 -> stackPane.getChildren().remove(progressBox));
            });
        }

        return difficultyMenu;
    }


    /**
     * Creates a control panel for the Sudoku game.
     *
     * @param difficultyMenu the menu for selecting the difficulty level
     */

    private void createControlPanel(DifficultyMenu difficultyMenu) {
        controlPanel = new ControlPanel(difficultyMenu, new Callback() {

            @Override
            public void numpadCall(int num) {
                grid.handleNumpad(num);
            }

            @Override
            public void notesCall() {
                grid.toggleNotes();
            }

            @Override
            public void eraseCall() {
                grid.handleErase();
            }

            @Override
            public void undoCall() {
                grid.handleUndo();
            }

            @Override
            public void redoCall() {
                grid.handleRedo();
            }

            @Override
            public void solveCall() {
                if(!grid.isSolved()) {
                    String line =
                            "Username: " + header.getUsername() + ", Time: " + header.getElapsedTime() + ", " +
                                    "Difficulty: " + header.getDifficultyLabel() + ", Used Solver";
                    try {
                        serverObject.writeToFile(line);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                int board[][] = grid.getInitialGrid();
                try {
                    board = serverObject.solveSudoku(board);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                grid.removeAllBackgrounds(true);
                grid.loadGrid(board);
            }

        });
    }
}
