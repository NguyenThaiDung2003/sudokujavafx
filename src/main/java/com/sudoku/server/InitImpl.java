package com.sudoku.server;

import com.sudoku.InitInterface;

import java.io.FileWriter;
import java.io.IOException;

public class InitImpl implements InitInterface {
    //lớp thực thi interface
    private final GenerateSudoku generateSudoku;

    public InitImpl()  {
        generateSudoku = new GenerateSudoku();
    }

    @Override
    public int[][] generateSudoku(GenerateSudoku.Difficulty difficulty)  {
        return generateSudoku.generate(difficulty);
    }//nhận tham số Độ khó và trả về một mảng int biểu thị câu đố Sudoku.

    @Override
    public int[][] solveSudoku(int[][] grid)  {
        generateSudoku.solve(grid);
        return grid;
    }//lấy một mảng int biểu thị một câu đố Sudoku và trả về câu đố đã giải.

    @Override
    public void writeToFile(String line) throws IOException {
        FileWriter fileWriter = new FileWriter("log.txt", true);
        fileWriter.write(line + "\n");
        fileWriter.close();
    }//ghi một chuỗi vào một tệp có tên log.txt.
}
