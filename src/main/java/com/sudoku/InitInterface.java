package com.sudoku;

import com.sudoku.server.GenerateSudoku;

import java.io.IOException;

public interface InitInterface {
    int[][] generateSudoku(GenerateSudoku.Difficulty difficulty) ;

    int[][] solveSudoku(int[][] grid) ;

    void writeToFile(String line) throws IOException;
}
