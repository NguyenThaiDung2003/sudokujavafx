package com.sudoku.server;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class GenerateSudoku {
    private final int gridSize = 9;
    private final List<Integer> numberList = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9);

    public int[][] generate(Difficulty difficulty) {
        int grid[][] = new int[gridSize][gridSize];

        int cellsToRemove = ThreadLocalRandom.current().nextInt(difficulty.lowerBound, difficulty.upperBound + 1);
        //số ô cần bỏ theo độ khó, random từ lower đến upper
        fillGrid(grid);//lấp đầy grid bằng các số hợp lệ (1-9) cho đến khi hoàn thành hoặc không thể tìm thấy giải pháp hợp lệ.

        while (cellsToRemove > 0) {//loại ô ngẫu nhiên
            int row = ThreadLocalRandom.current().nextInt(0, gridSize);
            int column = ThreadLocalRandom.current().nextInt(0, gridSize);

            while (grid[row][column] == 0) {
                row = ThreadLocalRandom.current().nextInt(0, gridSize);
                column = ThreadLocalRandom.current().nextInt(0, gridSize);
            }
            int backup = grid[row][column];//lưu lại ô vừa xóa
            grid[row][column] = 0;

            int copy[][] = copyGrid(grid);//copy grid

            IntByReference counter = new IntByReference(0);
            findNumberOfSolutions(copy, counter);//tìm số giải pháp

            if (counter.value != 1) {//nếu nhiều giải pháp hoặc không có giải pháp,khôi phục lại
                grid[row][column] = backup;
            }
            cellsToRemove -= 1;//giảm số ô cần xóa
        }
        return grid;
    }

    public boolean solve(int grid[][]) {//giải câu đố sử dụng backtracking
        for (int row = 0; row < gridSize; row++) {
            for (int col = 0; col < gridSize; col++) {
                if (grid[row][col] == 0) {
                    for (int number = 1; number <= gridSize; number++) {
                        if (isValidNumber(grid, row, col, number)) {
                            grid[row][col] = number;

                            if (solve(grid)) {
                                return true;
                            } else {
                                grid[row][col] = 0;
                            }
                        }
                    }
                    return false;
                }
            }
        }
        return true; // sudoku solved
    }

    private boolean isGridFull(int grid[][]) {
        for (int i = 0; i < gridSize; i += 1) {
            for (int j = 0; j < gridSize; j += 1) {
                if (grid[i][j] == 0) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Fills the grid with ONLY valid digits.
     */
    private boolean fillGrid(int grid[][]) {
        for (int row = 0; row < gridSize; row += 1) {
            for (int column = 0; column < gridSize; column += 1) {
                if (grid[row][column] == 0) {
                    Collections.shuffle(numberList);
                    for (int numberToPlace : numberList) {
                        if (isValidNumber(grid, row, column, numberToPlace)) {
                            grid[row][column] = numberToPlace;

                            if (isGridFull(grid)) {
                                return true;
                            }

                            if (fillGrid(grid)) {
                                return true;
                            }
                        }
                    }
                    grid[row][column] = 0;
                    return false;
                }
            }
        }
        return false;
    }

    private boolean findNumberOfSolutions(int grid[][], IntByReference counter) {
        int row = -1;
        int column = -1;
        for (int i = 0; i < 81; i += 1) {
            row = i / 9;
            column = i % 9;

            if (grid[row][column] == 0) {
                for (int value = 1; value < 10; value += 1) {
                    if (!rowContains(grid, row, value) && !columnContains(grid, column, value) && !sectorContains(grid, row
                            , column, value)) {//check hàng cột,ô vuông
                        grid[row][column] = value;
                        if (isGridFull(grid)) {//hoàn thành thì +1, nếu không tiếp tục
                            counter.value += 1;
                            break;
                        } else if (findNumberOfSolutions(grid, counter)) {
                            return true;
                        }
                    }
                }
                break;
            }
        }
        grid[row][column] = 0;
        return false;
    }

    private int[][] copyGrid(int[][] grid) {
        int copy[][] = new int[gridSize][gridSize];

        for (int i = 0; i < gridSize; i += 1) {
            System.arraycopy(grid[i], 0, copy[i], 0, gridSize);
        }
        return copy;
    }

    private boolean isValidNumber(int grid[][], int row, int column, int value) {
        return !rowContains(grid, row, value) && !columnContains(grid, column, value) && !sectorContains(grid, row,
                column, value);//kiểm tra hợp lệ
    }

    private boolean rowContains(int grid[][], int row, int value) {
        for (int i = 0; i < gridSize; i += 1) {
            if (grid[row][i] == value) {
                return true;
            }
        }
        return false;
    }

    private boolean columnContains(int grid[][], int column, int value) {
        for (int i = 0; i < gridSize; i += 1) {
            if (grid[i][column] == value) {
                return true;
            }
        }
        return false;
    }

    private boolean sectorContains(int grid[][], int row, int column, int value) {
        int sectorRow = row - row % 3;
        int sectorColumn = column - column % 3;

        for (int i = sectorRow; i < sectorRow + 3; i += 1) {
            for (int j = sectorColumn; j < sectorColumn + 3; j += 1) {
                if (grid[i][j] == value) {
                    return true;
                }
            }
        }
        return false;
    }

    public enum Difficulty {//định nghia cac hang so la cap 2 so lowerBound va upperBound
        EASY(35, 40),
        NORMAL(46, 50),
        HARD(55, 60),
        EXPERT(60, 65);

        // number of cells to remove
        private final int lowerBound;
        private final int upperBound;

        Difficulty(int lowerBound, int upperBound) {
            this.lowerBound = lowerBound;
            this.upperBound = upperBound;
        }
    }

    /**
     * Helper class which is used so the int can be passed by reference and not by value.
     */
    private class IntByReference {
        private Integer value;

        public IntByReference(Integer value) {
            this.value = value;
        }
    }
}

