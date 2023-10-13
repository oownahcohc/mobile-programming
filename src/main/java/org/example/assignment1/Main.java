package org.example.assignment1;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

import static org.example.assignment1.Tetrimino.*;
import static org.example.assignment1.Tetrimino.T_MINO;

public class Main {
    private static final int iScreenDy = 15;
    private static final int iScreenDx = 10;
    private static final int iScreenDw = 4; // large enough to cover the largest block

    private static int[][] createArrayScreen(int dy, int dx, int dw) {
        int y, x;
        int[][] array = new int[dy + dw][dx + 2 * dw];
        for (y = 0; y < array.length; y++)
            for (x = 0; x < dw; x++)
                array[y][x] = 1;
        for (y = 0; y < array.length; y++)
            for (x = dw + dx; x < array[0].length; x++)
                array[y][x] = 1;
        for (y = dy; y < array.length; y++)
            for (x = 0; x < array[0].length; x++)
                array[y][x] = 1;
        return array;
    }

    public static void drawMatrix(Matrix m) {
        int dy = m.get_dy();
        int dx = m.get_dx();
        int[][] array = m.get_array();
        for (int y = 0; y < dy; y++) {
            for (int x = 0; x < dx; x++) {
                if (array[y][x] == 0) System.out.print("□ ");
                else if (array[y][x] == 1) System.out.print("■ ");
                else System.out.print("X ");
            }
            System.out.println();
        }
        System.out.println();
    }

    private static final BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
    private static String line = null;
    private static int nKeys = 0;

    private static char getKey() throws IOException {
        char ch;
        if (nKeys != 0) {
            ch = line.charAt(line.length() - nKeys);
            nKeys--;
            return ch;
        }
        do {
            line = br.readLine();
            nKeys = line.length();
        } while (nKeys == 0);
        ch = line.charAt(0);
        nKeys--;
        return ch;
    }

    private static int idxBlockDegree = 0;

    public static void main(String[] args) throws Exception {

        boolean newBlockNeeded = false;
        int top = 0;
        int left = iScreenDw + iScreenDx / 2 - 2;
        int[][] arrayScreen = createArrayScreen(iScreenDy, iScreenDx, iScreenDw);
        char key;

        RandomNumberGenerator randomNumberGenerator = new TetrisRandomNumberGenerator();
        Tetrimino randomTetrimino = getByRandom(randomNumberGenerator.generate());
        Block block = new Block();
        Matrix currentBlock = block.getDefaultBlockByType(randomTetrimino);

        Matrix iScreen = new Matrix(arrayScreen);
        Matrix tempBlk = iScreen.clip(top, left, top + currentBlock.get_dy(), left + currentBlock.get_dx());
        tempBlk = tempBlk.add(currentBlock);
        Matrix oScreen = new Matrix(iScreen);
        oScreen.paste(tempBlk, top, left);
        drawMatrix(oScreen);

        while ((key = getKey()) != 'q') {
            switch (key) {
                case 'a':
                    left--;
                    break; // move left
                case 'd':
                    left++;
                    break; // move right
                case 's':
                    top++;
                    break; // move down
                case 'w':
                    idxBlockDegree = (idxBlockDegree + 1) % 4;
                    currentBlock = block.getBlockByTypeAndRotateNumber(randomTetrimino, idxBlockDegree);
                    break; // rotate the block clockwise
                case ' ':
                    while (!tempBlk.anyGreaterThan(1)) {
                        top++;
                        tempBlk = iScreen.clip(top, left, top+currentBlock.get_dy(), left+currentBlock.get_dx());
                        tempBlk = tempBlk.add(currentBlock);
                    }
                    break; // drop the block
                default:
                    System.out.println("unknown key!");
            }
            tempBlk = iScreen.clip(top, left, top + currentBlock.get_dy(), left + currentBlock.get_dx());
            tempBlk = tempBlk.add(currentBlock);

            if (tempBlk.anyGreaterThan(1)) {
                switch (key) {
                    case 'a':
                        left++;
                        break; // undo: move right
                    case 'd':
                        left--;
                        break; // undo: move left
                    case 's', ' ':
                        top--;
                        newBlockNeeded = true;
                        break; // undo: move up
                    case 'w':
                        idxBlockDegree = (idxBlockDegree - 1) % 4;
                        break; // undo: rotate the block counter-clockwise
                }
                tempBlk = iScreen.clip(top, left, top + currentBlock.get_dy(), left + currentBlock.get_dx());
                tempBlk = tempBlk.add(currentBlock);
            }
            oScreen = new Matrix(iScreen);
            oScreen.paste(tempBlk, top, left);
            drawMatrix(oScreen);

            if (newBlockNeeded) {
                iScreen = new Matrix(oScreen);
                top = 0;
                left = iScreenDw + iScreenDx / 2 - 2;
                newBlockNeeded = false;
                currentBlock = block.getDefaultBlockByType(getByRandom(randomNumberGenerator.generate()));
                tempBlk = iScreen.clip(top, left, top + currentBlock.get_dy(), left + currentBlock.get_dx());
                tempBlk = tempBlk.add(currentBlock);
                if (tempBlk.anyGreaterThan(1)) {
                    System.out.println("Game Over!");
                    System.exit(0);
                }
                oScreen = new Matrix(iScreen);
                oScreen.paste(tempBlk, top, left);
                drawMatrix(oScreen);
            }
        }
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
            {0, 1, 0},
            {0, 1, 0},
            {1, 1, 0},
    }, new int[][]{
            {0, 1, 0},
            {0, 1, 0},
            {1, 1, 0},
    }, new int[][]{
            {0, 1, 0},
            {0, 1, 0},
            {1, 1, 0},
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

