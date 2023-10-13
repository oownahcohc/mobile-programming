package org.example.assignment1;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

import static org.example.assignment1.Tetrimino.*;

public class Main {

    public static void main(String[] args) throws Exception {
        TetrisController tetrisController = new TetrisController(new View.Input(), new View.Output());
        tetrisController.run();
    }
}

class TetrisController {

    private static final char STOP_SIGNAL = 'q';

    private final View.Input input;
    private final View.Output output;

    public TetrisController(View.Input input, View.Output output) {
        this.input = input;
        this.output = output;
    }

    public void run() throws MatrixException {
        TetrisGame tetrisGame = new TetrisGame(new TetrisRandomNumberGenerator());
        tetrisGame.init();
        output.drawMatrix(tetrisGame.getDisplayMatrix());

        char key;
        while ((key = input.getKey()) != STOP_SIGNAL) {
            tetrisGame.manipulateBlockOnScreenByKey(key);
            tetrisGame.handleWallCollision(key);
            output.drawMatrix(tetrisGame.getDisplayMatrix());
            tetrisGame.handleNewBlockNeeded();
            checkGameOver(tetrisGame);
            output.drawMatrix(tetrisGame.getDisplayMatrix());
        }
    }

    private void checkGameOver(TetrisGame tetrisGame) {
        if (tetrisGame.isOver()) {
            output.printGameOver();
        }
    }
}

class TetrisGame {

    private static final char LEFT_KEY = 'a';
    private static final char RIGHT_KEY = 'd';
    private static final char DOWN_KEY = 's';
    private static final char ROTATE_KEY = 'w';
    private static final char DROP_KEY = ' ';

    private static final Block BLOCK = new Block();

    private final RandomNumberGenerator randomNumberGenerator;
    private Screen display;
    private Screen gameScreen;
    private Screen currentBlockOnScreen;
    private TetrisBlock currentBlock;
    private int clockwiseRotateDegree = 0;
    private boolean isOver = false;

    public TetrisGame(RandomNumberGenerator randomNumberGenerator) {
        this.randomNumberGenerator = randomNumberGenerator;
    }

    public void init() throws MatrixException {
        gameScreen = Screen.createGameScreen();
        Tetrimino randomTetrimino = getByRandom(randomNumberGenerator.generate());
        currentBlock = new TetrisBlock(randomTetrimino, BLOCK.getDefaultBlockByType(randomTetrimino));
        currentBlockOnScreen = gameScreen.getScreenAfterAddCurrentBlock(currentBlock.getBlockMatrix());
        display = gameScreen.copy();
        display.pasteCurrentBlockOnGameScreen(currentBlockOnScreen.getMatrix(), gameScreen.getTop(), gameScreen.getLeft());
    }

    public void manipulateBlockOnScreenByKey(char key) throws MatrixException {
        switch (key) {
            case LEFT_KEY: // moveLeft
                gameScreen.moveLeft();
                break;
            case RIGHT_KEY: // moveRight
                gameScreen.moveRight();
                break;
            case DOWN_KEY: // moveDown
                gameScreen.moveDown();
                break;
            case ROTATE_KEY: // rotate
                clockwiseRotateDegree = (clockwiseRotateDegree + 1) % 4;
                Tetrimino currentTetrimino = currentBlock.getTetrimino();
                Matrix rotateTetrimino = BLOCK.getBlockByTypeAndRotateNumber(currentTetrimino, clockwiseRotateDegree);
                currentBlock = new TetrisBlock(currentTetrimino, rotateTetrimino);
                break;
            case DROP_KEY: // dropDown
                currentBlockOnScreen = gameScreen.dropDown(currentBlockOnScreen.getMatrix(), currentBlock.getBlockMatrix());
                break;
        }
        currentBlockOnScreen = gameScreen.getScreenAfterAddCurrentBlock(currentBlock.getBlockMatrix());
    }

    public void handleWallCollision(char key) throws MatrixException {
        if (currentBlockOnScreen.isWallCollision()) {
            switch (key) {
                case LEFT_KEY:
                    gameScreen.moveRight();
                    break; // undo: move right
                case RIGHT_KEY:
                    gameScreen.moveLeft();
                    break; // undo: move left
                case DOWN_KEY, DROP_KEY:
                    gameScreen.moveUp();
                    break; // undo: move up
                case ROTATE_KEY:
                    clockwiseRotateDegree = (clockwiseRotateDegree - 1) % 4;
                    Tetrimino currentTetrimino = currentBlock.getTetrimino();
                    Matrix rotateTetrimino = BLOCK.getBlockByTypeAndRotateNumber(currentTetrimino, clockwiseRotateDegree);
                    currentBlock = new TetrisBlock(currentTetrimino, rotateTetrimino);
                    break; // undo: rotate the block counter-clockwise
            }
            currentBlockOnScreen = gameScreen.getScreenAfterAddCurrentBlock(currentBlock.getBlockMatrix());
        }
        display = gameScreen.copy();
        display.pasteCurrentBlockOnGameScreen(currentBlockOnScreen.getMatrix(), gameScreen.getTop(), gameScreen.getLeft());
    }

    public void handleNewBlockNeeded() throws MatrixException {
        if (gameScreen.isNewBlockNeed()) {
            gameScreen = display.copy();
            gameScreen.fullLineDelete();
            gameScreen.initScreenSetting();

            Tetrimino randomTetrimino = getByRandom(randomNumberGenerator.generate());
            currentBlock = new TetrisBlock(randomTetrimino, BLOCK.getDefaultBlockByType(randomTetrimino));
            currentBlockOnScreen = gameScreen.getScreenAfterAddCurrentBlock(currentBlock.getBlockMatrix());

            if (currentBlockOnScreen.isWallCollision()) {
                isOver = true;
            }

            display = gameScreen.copy();
            display.pasteCurrentBlockOnGameScreen(currentBlockOnScreen.getMatrix(), gameScreen.getTop(), gameScreen.getLeft());
        }
    }

    public Matrix getDisplayMatrix() {
        return display.getMatrix();
    }

    public boolean isOver() {
        return isOver;
    }
}

class TetrisBlock {

    private final Tetrimino tetrimino;
    private final Matrix blockMatrix;

    public TetrisBlock(Tetrimino tetrimino, Matrix blockMatrix) {
        this.tetrimino = tetrimino;
        this.blockMatrix = blockMatrix;
    }

    public Tetrimino getTetrimino() {
        return tetrimino;
    }

    public Matrix getBlockMatrix() {
        return blockMatrix;
    }
}

class Screen {

    private static final int SCREEN_HEIGHT = 15;
    private static final int SCREEN_WIDTH = 10;
    private static final int SCREEN_BORDER_WIDTH = 4; // large enough to cover the largest block
    private static final int SCREEN_TOP = 0;
    private static final int SCREEN_CENTER = SCREEN_BORDER_WIDTH + SCREEN_WIDTH / 2 - 2;

    private final Matrix matrix;
    private int top = SCREEN_TOP;
    private int left = SCREEN_CENTER;
    private boolean isNewBlockNeed = false;

    public Screen(Matrix matrix) {
        this.matrix = matrix;
    }

    public static Screen createGameScreen() throws MatrixException {
        int[][] arrayScreen = createArrayScreen(SCREEN_HEIGHT, SCREEN_WIDTH, SCREEN_BORDER_WIDTH);
        return new Screen(new Matrix(arrayScreen));
    }

    private static int[][] createArrayScreen(int dy, int dx, int dw) {
        int[][] array = new int[dy + dw][dx + 2 * dw];
        for (int y = 0; y < array.length; y++)
            for (int x = 0; x < dw; x++)
                array[y][x] = 1;
        for (int y = 0; y < array.length; y++)
            for (int x = dw + dx; x < array[0].length; x++)
                array[y][x] = 1;
        for (int y = dy; y < array.length; y++)
            for (int x = 0; x < array[0].length; x++)
                array[y][x] = 1;
        return array;
    }

    public Screen copy() throws MatrixException {
        Matrix copy = new Matrix(matrix);
        return new Screen(copy);
    }

    public void pasteCurrentBlockOnGameScreen(Matrix currentBlock, int top, int left) throws MatrixException {
        matrix.paste(currentBlock, top, left);
    }

    public Screen getScreenAfterAddCurrentBlock(Matrix currentBlock) throws MatrixException {
        Matrix currentBlockOnScreen = matrix.clip(top, left, top + currentBlock.get_dy(), left + currentBlock.get_dx());
        currentBlockOnScreen = currentBlockOnScreen.add(currentBlock);
        return new Screen(currentBlockOnScreen);
    }

    public void initScreenSetting() {
        top = SCREEN_TOP;
        left = SCREEN_CENTER;
        isNewBlockNeed = false;
    }

    public void moveLeft() {
        left--;
    }

    public void moveRight() {
        left++;
    }

    public void moveDown() {
        top++;
    }

    public void moveUp() {
        top--;
        isNewBlockNeed = true;
    }

    public Screen dropDown(Matrix currentBlockOnScreen, Matrix currentBlock) throws MatrixException {
        while (!currentBlockOnScreen.anyGreaterThan(1)) {
            top++;
            currentBlockOnScreen = matrix.clip(top, left, top+currentBlock.get_dy(), left+currentBlock.get_dx());
            currentBlockOnScreen = currentBlockOnScreen.add(currentBlock);
        }
        return new Screen(currentBlockOnScreen);
    }

    public boolean isWallCollision() {
        return matrix.anyGreaterThan(1);
    }

    public void fullLineDelete() throws MatrixException {
        int playAreaTop = 0; // 플레이 영역의 상단 경계
        int playAreaBottom = matrix.get_dy() - SCREEN_BORDER_WIDTH; // 플레이 영역의 하단 경계

        for (int y = playAreaTop; y < playAreaBottom; y++) {
            Matrix line = matrix.clip(y, SCREEN_BORDER_WIDTH, y + 1, matrix.get_dx() - SCREEN_BORDER_WIDTH);
            if (line.sum() == line.get_dx()) { // Check if the line is full
                if (y > playAreaTop) { // If it's not the topmost line
                    Matrix above = matrix.clip(playAreaTop, SCREEN_BORDER_WIDTH, y, matrix.get_dx() - SCREEN_BORDER_WIDTH); // Get the part above the line
                    matrix.paste(above, playAreaTop + 1, SCREEN_BORDER_WIDTH); // Paste it one line below
                }
                // Clear the topmost line of the play area
                Matrix clear = new Matrix(1, line.get_dx());
                for(int x = 0; x < clear.get_dx(); x++)
                    clear.get_array()[0][x] = 0;
                matrix.paste(clear, playAreaTop, SCREEN_BORDER_WIDTH);
            }
        }
    }

    public Matrix getMatrix() {
        return matrix;
    }

    public boolean isNewBlockNeed() {
        return isNewBlockNeed;
    }

    public int getTop() {
        return top;
    }

    public int getLeft() {
        return left;
    }
}

interface RandomNumberGenerator {
    int generate();
}

class TetrisRandomNumberGenerator implements RandomNumberGenerator {

    private static final int TETRIS_BOUND = 7;
    private static final Random RANDOM = new Random();

    public int generate() {
        return RANDOM.nextInt(TETRIS_BOUND);
    }
}

class View {

    private View() {
        throw new IllegalStateException("Util class");
    }

    static class Input {

        private static final BufferedReader BR = new BufferedReader(new InputStreamReader(System.in));
        private static final int EMPTY = 0;

        private String userInputLine = "";
        private int remainKey = 0;

        public char getKey() {
            char ch;

            if (remainKey != EMPTY) {
                ch = userInputLine.charAt(userInputLine.length() - remainKey);
                remainKey--;
                return ch;
            }

            do {
                try {
                    userInputLine = BR.readLine();
                } catch (IOException e) {
                    throw new RuntimeException(e.getMessage());
                }
                remainKey = userInputLine.length();
            } while (remainKey == EMPTY);

            ch = userInputLine.charAt(0);
            remainKey--;

            return ch;
        }
    }

    static class Output {

        public void drawMatrix(Matrix matrix) {
            int[][] array = matrix.get_array();
            for (int y = 0; y < matrix.get_dy(); y++) {
                for (int x = 0; x < matrix.get_dx(); x++) {
                    if (array[y][x] == 0) {
                        System.out.print("□ ");
                    } else if (array[y][x] == 1) {
                        System.out.print("■ ");
                    } else {
                        System.out.print("X ");
                    }
                }
                System.out.println();
            }
            System.out.println();
        }

        public void printGameOver() {
            System.out.println("Game Over!");
            System.exit(0);
        }
    }
}

enum Tetrimino {

    I_MINO(new int[][]{
            {0, 0, 1, 0},
            {0, 0, 1, 0},
            {0, 0, 1, 0},
            {0, 0, 1, 0},
    }, new int[][] {
            {0, 0, 0, 0},
            {0, 0, 0, 0},
            {1, 1, 1, 1},
            {0, 0, 0, 0},
    }, new int[][] {
            {0, 1, 0, 0},
            {0, 1, 0, 0},
            {0, 1, 0, 0},
            {0, 1, 0, 0},
    }, new int[][] {
            {0, 0, 0, 0},
            {1, 1, 1, 1},
            {0, 0, 0, 0},
            {0, 0, 0, 0},
    }),
    O_MINO(new int[][]{
            {1, 1},
            {1, 1},
    }, new int[][]{
            {1, 1},
            {1, 1},
    }, new int[][]{
            {1, 1},
            {1, 1},
    }, new int[][]{
            {1, 1},
            {1, 1},
    }),
    Z_MINO(new int[][]{
            {1, 1, 0},
            {0, 1, 1},
            {0, 0, 0},
    }, new int[][]{
            {0, 0, 1},
            {0, 1, 1},
            {0, 1, 0},
    }, new int[][]{
            {0, 0, 0},
            {1, 1, 0},
            {0, 1, 1},
    }, new int[][]{
            {0, 1, 0},
            {1, 1, 0},
            {1, 0, 0},
    }),
    S_MINO(new int[][]{
            {0, 1, 1},
            {1, 1, 0},
            {0, 0, 0},
    }, new int[][]{
            {0, 1, 0},
            {0, 1, 1},
            {0, 0, 1},
    }, new int[][]{
            {0, 0, 0},
            {0, 1, 1},
            {1, 1, 0},
    }, new int[][]{
            {1, 0, 0},
            {1, 1, 0},
            {0, 1, 0},
    }),
    J_MINO(new int[][]{
            {0, 1, 0},
            {0, 1, 0},
            {1, 1, 0},
    }, new int[][]{
            {1, 0, 0},
            {1, 1, 1},
            {0, 0, 0},
    }, new int[][]{
            {0, 1, 1},
            {0, 1, 0},
            {0, 1, 0},
    }, new int[][]{
            {0, 0, 0},
            {1, 1, 1},
            {0, 0, 1},
    }),
    L_MINO(new int[][]{
            {0, 1, 0},
            {0, 1, 0},
            {0, 1, 1},
    }, new int[][]{
            {0, 0, 0},
            {1, 1, 1},
            {1, 0, 0},
    }, new int[][]{
            {1, 1, 0},
            {0, 1, 0},
            {0, 1, 0},
    }, new int[][]{
            {0, 0, 1},
            {1, 1, 1},
            {0, 0, 0},
    }),
    T_MINO(new int[][]{
            {1, 1, 1},
            {0, 1, 0},
            {0, 0, 0},
    }, new int[][]{
            {0, 0, 1},
            {0, 1, 1},
            {0, 0, 1},
    }, new int[][]{
            {0, 0, 0},
            {0, 1, 0},
            {1, 1, 1},
    }, new int[][]{
            {1, 0, 0},
            {1, 1, 0},
            {1, 0, 0},
    });

    private final int[][] defaultBlock;
    private final int[][] threeClockwiseBlock;
    private final int[][] sixClockwiseBlock;
    private final int[][] nineClockwiseBlock;

    Tetrimino(int[][] defaultBlock, int[][] threeClockwiseBlock, int[][] sixClockwiseBlock, int[][] nineClockwiseBlock) {
        this.defaultBlock = defaultBlock;
        this.threeClockwiseBlock = threeClockwiseBlock;
        this.sixClockwiseBlock = sixClockwiseBlock;
        this.nineClockwiseBlock = nineClockwiseBlock;
    }

    public static Tetrimino getByRandom(int random) {
        return Arrays.stream(values())
                .filter(tetrimino -> tetrimino.ordinal() == random)
                .findFirst()
                .orElseThrow();
    }

    public int[][] getDefaultBlock() {
        return defaultBlock;
    }

    public int[][] getThreeClockwiseBlock() {
        return threeClockwiseBlock;
    }

    public int[][] getSixClockwiseBlock() {
        return sixClockwiseBlock;
    }

    public int[][] getNineClockwiseBlock() {
        return nineClockwiseBlock;
    }
}

class Block {

    private static final Map<Tetrimino, List<Matrix>> TETRIMINO_FACTORY = new EnumMap<>(Tetrimino.class);

    static {
        try {
            TETRIMINO_FACTORY.put(I_MINO, List.of(
                    new Matrix(I_MINO.getDefaultBlock()),
                    new Matrix(I_MINO.getThreeClockwiseBlock()),
                    new Matrix(I_MINO.getSixClockwiseBlock()),
                    new Matrix(I_MINO.getNineClockwiseBlock())
            ));
        } catch (MatrixException e) {
            throw new RuntimeException(e);
        }
        try {
            TETRIMINO_FACTORY.put(O_MINO, List.of(
                    new Matrix(O_MINO.getDefaultBlock()),
                    new Matrix(O_MINO.getThreeClockwiseBlock()),
                    new Matrix(O_MINO.getSixClockwiseBlock()),
                    new Matrix(O_MINO.getNineClockwiseBlock())
            ));
        } catch (MatrixException e) {
            throw new RuntimeException(e);
        }
        try {
            TETRIMINO_FACTORY.put(Z_MINO, List.of(
                    new Matrix(Z_MINO.getDefaultBlock()),
                    new Matrix(Z_MINO.getThreeClockwiseBlock()),
                    new Matrix(Z_MINO.getSixClockwiseBlock()),
                    new Matrix(Z_MINO.getNineClockwiseBlock())
            ));
        } catch (MatrixException e) {
            throw new RuntimeException(e);
        }
        try {
            TETRIMINO_FACTORY.put(S_MINO, List.of(
                    new Matrix(S_MINO.getDefaultBlock()),
                    new Matrix(S_MINO.getThreeClockwiseBlock()),
                    new Matrix(S_MINO.getSixClockwiseBlock()),
                    new Matrix(S_MINO.getNineClockwiseBlock())
            ));
        } catch (MatrixException e) {
            throw new RuntimeException(e);
        }
        try {
            TETRIMINO_FACTORY.put(J_MINO, List.of(
                    new Matrix(J_MINO.getDefaultBlock()),
                    new Matrix(J_MINO.getThreeClockwiseBlock()),
                    new Matrix(J_MINO.getSixClockwiseBlock()),
                    new Matrix(J_MINO.getNineClockwiseBlock())
            ));
        } catch (MatrixException e) {
            throw new RuntimeException(e);
        }
        try {
            TETRIMINO_FACTORY.put(L_MINO, List.of(
                    new Matrix(L_MINO.getDefaultBlock()),
                    new Matrix(L_MINO.getThreeClockwiseBlock()),
                    new Matrix(L_MINO.getSixClockwiseBlock()),
                    new Matrix(L_MINO.getNineClockwiseBlock())
            ));
        } catch (MatrixException e) {
            throw new RuntimeException(e);
        }
        try {
            TETRIMINO_FACTORY.put(T_MINO, List.of(
                    new Matrix(T_MINO.getDefaultBlock()),
                    new Matrix(T_MINO.getThreeClockwiseBlock()),
                    new Matrix(T_MINO.getSixClockwiseBlock()),
                    new Matrix(T_MINO.getNineClockwiseBlock())
            ));
        } catch (MatrixException e) {
            throw new RuntimeException(e);
        }
    }

    public Matrix getBlockByTypeAndRotateNumber(Tetrimino tetrimino, int rotateNumber) {
        return TETRIMINO_FACTORY
                .get(tetrimino)
                .get(rotateNumber);
    }

    public Matrix getDefaultBlockByType(Tetrimino tetrimino) {
        return TETRIMINO_FACTORY
                .get(tetrimino)
                .get(0);
    }
}

