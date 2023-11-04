import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Random;

public class Main {
    static final TetrisState Finished = TetrisState.Finished;
    static final TetrisState NewBlock = TetrisState.NewBlock;
    static final TetrisState Running = TetrisState.Running;

    private static int[][][][] setOfBlockArrays = { // [7][4][?][?]
            {
                    {
                            {1, 1},
                            {1, 1}
                    },
                    {
                            {1, 1},
                            {1, 1}
                    },
                    {
                            {1, 1},
                            {1, 1}
                    },
                    {
                            {1, 1},
                            {1, 1}
                    }
            },
            {
                    {
                            {0, 1, 0},
                            {1, 1, 1},
                            {0, 0, 0},
                    },
                    {
                            {0, 1, 0},
                            {0, 1, 1},
                            {0, 1, 0},
                    },
                    {
                            {0, 0, 0},
                            {1, 1, 1},
                            {0, 1, 0},
                    },
                    {
                            {0, 1, 0},
                            {1, 1, 0},
                            {0, 1, 0},
                    },
            },
            {
                    {
                            {1, 0, 0},
                            {1, 1, 1},
                            {0, 0, 0},
                    },
                    {
                            {0, 1, 1},
                            {0, 1, 0},
                            {0, 1, 0},
                    },
                    {
                            {0, 0, 0},
                            {1, 1, 1},
                            {0, 0, 1},
                    },
                    {
                            {0, 1, 0},
                            {0, 1, 0},
                            {1, 1, 0},
                    },
            },
            {
                    {
                            {0, 0, 1},
                            {1, 1, 1},
                            {0, 0, 0},
                    },
                    {
                            {0, 1, 0},
                            {0, 1, 0},
                            {0, 1, 1},
                    },
                    {
                            {0, 0, 0},
                            {1, 1, 1},
                            {1, 0, 0},
                    },
                    {
                            {1, 1, 0},
                            {0, 1, 0},
                            {0, 1, 0},
                    },
            },
            {
                    {
                            {0, 1, 0},
                            {1, 1, 0},
                            {1, 0, 0},
                    },
                    {
                            {1, 1, 0},
                            {0, 1, 1},
                            {0, 0, 0},
                    },
                    {
                            {0, 1, 0},
                            {1, 1, 0},
                            {1, 0, 0},
                    },
                    {
                            {1, 1, 0},
                            {0, 1, 1},
                            {0, 0, 0},
                    },
            },
            {
                    {
                            {0, 1, 0},
                            {0, 1, 1},
                            {0, 0, 1},
                    },
                    {
                            {0, 0, 0},
                            {0, 1, 1},
                            {1, 1, 0},
                    },
                    {
                            {0, 1, 0},
                            {0, 1, 1},
                            {0, 0, 1},
                    },
                    {
                            {0, 0, 0},
                            {0, 1, 1},
                            {1, 1, 0},
                    },
            },
            {
                    {
                            {0, 0, 0, 0},
                            {1, 1, 1, 1},
                            {0, 0, 0, 0},
                            {0, 0, 0, 0},
                    },
                    {
                            {0, 1, 0, 0},
                            {0, 1, 0, 0},
                            {0, 1, 0, 0},
                            {0, 1, 0, 0},
                    },
                    {
                            {0, 0, 0, 0},
                            {1, 1, 1, 1},
                            {0, 0, 0, 0},
                            {0, 0, 0, 0},
                    },
                    {
                            {0, 1, 0, 0},
                            {0, 1, 0, 0},
                            {0, 1, 0, 0},
                            {0, 1, 0, 0},
                    },
            },
    }; // end of setOfBlockArrays
    private static BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
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
    public static void main(String[] args) throws Exception {
        char key;
        TetrisState state;
        Tetris.init(setOfBlockArrays);

        OnLeft myOnLeft = new OnLeft() {
            public boolean run(Tetris t, char key) throws Exception {
                t.left = t.left - 1;
                return t.anyConflict(true);
            }
        };
        OnRight myOnRight = new OnRight() {
            public boolean run(Tetris t, char key) throws Exception {
                t.left = t.left + 1;
                return t.anyConflict(true);
            }
        };
        OnDown myOnDown = new OnDown() {
            public boolean run(Tetris t, char key) throws Exception {
                t.top = t.top + 1;
                return t.anyConflict(true);
            }
        };
        OnUp myOnUp = new OnUp() {
            public boolean run(Tetris t, char key) throws Exception {
                t.top = t.top - 1;
                return t.anyConflict(true);
            }
        };
        OnDrop myOnDrop = new OnDrop() {
            public boolean run(Tetris t, char key) throws Exception {
                do { t.top = t.top + 1; }
                while (t.anyConflict(false) == false);
                return t.anyConflict(true);
            }
        };
        OnCw myOnCw = new OnCw() {
            public boolean run(Tetris t, char key) throws Exception {
                t.idxBlockDegree = (t.idxBlockDegree + 1) % t.nBlockDegrees;
                t.currBlk = t.setOfBlockObjects[t.idxBlockType][t.idxBlockDegree];
                return t.anyConflict(true);
            }
        };

        OnCcw myOnCcw = new OnCcw() {
            public boolean run(Tetris t, char key) throws Exception {
                t.idxBlockDegree = (t.idxBlockDegree + t.nBlockDegrees - 1) % t.nBlockDegrees;
                t.currBlk = t.setOfBlockObjects[t.idxBlockType][t.idxBlockDegree];
                return t.anyConflict(true);
            }
        };
        OnNewBlock myOnNewBlock = new OnNewBlock() {
            public boolean run(Tetris t, char key) throws Exception {
                t.oScreen = deleteFullLines(t.oScreen, t.currBlk, t.top, t.iScreenDy, t.iScreenDx, t.iScreenDw);
                t.iScreen = new Matrix(t.oScreen);
                t.idxBlockType = key - '0';
                t.idxBlockDegree = 0;
                t.currBlk = t.setOfBlockObjects[t.idxBlockType][t.idxBlockDegree];
                t.top = 0;
                t.left = t.iScreenDw + t.iScreenDx / 2 - (t.currBlk.get_dx()+1) / 2;
                return t.anyConflict(true);
            }
            private Matrix deleteFullLines(Matrix screen, Matrix blk, int top, int dy, int dx, int dw) throws Exception {
                Matrix line, zero, temp;
                if (blk == null) // called right after the game starts.
                    return screen; // no lines to be deleted
                int cy, y, nDeleted = 0, nScanned = blk.get_dy();
                if (top + blk.get_dy() - 1 >= dy)
                    nScanned -= (top + blk.get_dy() - dy);
                zero = new Matrix(1, dx);
                for (y = nScanned - 1; y >= 0; y--) {
                    cy = top + y + nDeleted;
                    line = screen.clip(cy, 0, cy + 1, screen.get_dx());
                    if (line.sum() == screen.get_dx()) {
                        temp = screen.clip(0, 0, cy, screen.get_dx());
                        screen.paste(temp, 1, 0);
                        screen.paste(zero, 0, dw);
                        nDeleted++;
                    }
                }
                return screen;
            }
        };
        OnFinished myOnFinished = new OnFinished() {
            public boolean run(Tetris t, char key) throws Exception {
                System.out.println("OnFinished.run() called");
                return false;
            }
        };
        Tetris.setOperation('a', Running, myOnLeft, Running, myOnRight, Running);
        Tetris.setOperation('d', Running, myOnRight, Running, myOnLeft, Running);
        Tetris.setOperation('s', Running, myOnDown, Running, myOnUp, NewBlock);
        Tetris.setOperation('w', Running, myOnCw, Running, myOnCcw, Running);
        Tetris.setOperation(' ', Running, myOnDrop, Running, myOnUp, NewBlock);
        Tetris.setOperation('0', NewBlock, myOnNewBlock, Running, myOnFinished, Finished);
        Tetris.setOperation('1', NewBlock, myOnNewBlock, Running, myOnFinished, Finished);
        Tetris.setOperation('2', NewBlock, myOnNewBlock, Running, myOnFinished, Finished);
        Tetris.setOperation('3', NewBlock, myOnNewBlock, Running, myOnFinished, Finished);
        Tetris.setOperation('4', NewBlock, myOnNewBlock, Running, myOnFinished, Finished);
        Tetris.setOperation('5', NewBlock, myOnNewBlock, Running, myOnFinished, Finished);
        Tetris.setOperation('6', NewBlock, myOnNewBlock, Running, myOnFinished, Finished);

        Tetris board = new Tetris(15, 10);
        Random random = new Random();
        key = (char) ('0' + random.nextInt(7));
        board.accept(key);
        board.printScreen(); System.out.println();

        while ((key = getKey()) != 'q') {
            state = board.accept(key);
            board.printScreen(); System.out.println();
            if (state == NewBlock) {
                key = (char) ('0' + random.nextInt(7));
                state = board.accept(key);
                board.printScreen(); System.out.println();
                if (state == Finished) break; // Game Over!
            }
        }
        System.out.println("Program terminated!");
    }
}
