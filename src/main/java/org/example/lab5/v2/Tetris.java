enum TetrisState {
    Running(0), NewBlock(1), Finished(2);
    private final int value;
    private TetrisState(int value) { this.value = value; }
    public int value() { return value; }
}

interface ActionHandler {
    public boolean run(Tetris t, char key) throws Exception;
}

class OnLeft implements ActionHandler {
    public boolean run(Tetris t, char key) throws Exception {
        t.left = t.left - 1;
        return t.anyConflict(true);
    }
};

class OnRight implements ActionHandler {
    public boolean run(Tetris t, char key) throws Exception {
        t.left = t.left + 1;
        return t.anyConflict(true);
    }
};

class OnDown implements ActionHandler {
    public boolean run(Tetris t, char key) throws Exception {
        t.top = t.top + 1;
        return t.anyConflict(true);
    }
};

class OnUp implements ActionHandler {
    public boolean run(Tetris t, char key) throws Exception {
        t.top = t.top - 1;
        return t.anyConflict(true);
    }
};

class OnDrop implements ActionHandler {
    public boolean run(Tetris t, char key) throws Exception {
        do { t.top = t.top + 1; }
        while (t.anyConflict(false) == false);
        return t.anyConflict(true);
    }
};

class OnCw implements ActionHandler {
    public boolean run(Tetris t, char key) throws Exception {
        t.idxBlockDegree = (t.idxBlockDegree + 1) % t.nBlockDegrees;
        t.currBlk = t.setOfBlockObjects[t.idxBlockType][t.idxBlockDegree];
        return t.anyConflict(true);
    }
};

class OnCcw implements ActionHandler {
    public boolean run(Tetris t, char key) throws Exception {
        t.idxBlockDegree = (t.idxBlockDegree + t.nBlockDegrees - 1) % t.nBlockDegrees;
        t.currBlk = t.setOfBlockObjects[t.idxBlockType][t.idxBlockDegree];
        return t.anyConflict(true);
    }
};

class OnNewBlock implements ActionHandler {
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

class OnFinished implements ActionHandler {
    public boolean run(Tetris t, char key) throws Exception {
        System.out.println("OnFinished.run() called");
        return false;
    }
};

public class Tetris {
    static final TetrisState Finished = TetrisState.Finished;
    static final TetrisState NewBlock = TetrisState.NewBlock;
    static final TetrisState Running = TetrisState.Running;

    public static int iScreenDw;
    private static int nBlockTypes;
    public static int nBlockDegrees;
    public static Matrix[][] setOfBlockObjects;
    private static Matrix[][] createSetOfBlocks(int[][][][] setOfArrays) throws Exception {
        int ntypes = setOfArrays.length;
        int ndegrees = setOfArrays[0].length;
        Matrix[][] setOfObjects = new Matrix[nBlockTypes][nBlockDegrees];
        for (int t = 0; t < ntypes; t++)
            for (int d = 0; d < ndegrees; d++)
                setOfObjects[t][d] = new Matrix(setOfArrays[t][d]);
        return setOfObjects;
    }
    private static int max(int a, int b) { return (a > b ? a : b); }
    private static int findLargestBlockSize(int[][][][] setOfArrays) {
        int size, max_size = 0;
        for (int t = 0; t < nBlockTypes; t++) {
            for (int d = 0; d < nBlockDegrees; d++) {
                size = setOfArrays[t][d].length;
                max_size = max(max_size, size);
            }
        }
        return max_size;
    }
    public static void init(int[][][][] setOfBlockArrays) throws Exception { // initialize static variables
        nBlockTypes = setOfBlockArrays.length;
        nBlockDegrees = setOfBlockArrays[0].length;
        setOfBlockObjects = createSetOfBlocks(setOfBlockArrays);
        iScreenDw = findLargestBlockSize(setOfBlockArrays);
    }
    static class TetrisOperation {
        private char key;
        private ActionHandler hDo, hUndo;
        private TetrisState preState, postStateWDo, postStateWUndo;
        public TetrisOperation(char ch, TetrisState s0, ActionHandler h1,
                               TetrisState s1, ActionHandler h2, TetrisState s2) {
            key = ch;
            hDo = h1;  hUndo = h2;
            preState = s0;  postStateWDo = s1;  postStateWUndo = s2;
        }
    };
    private static final int MAX_TET_OPS = 100;
    private static int nops;
    private static TetrisOperation operations[] = new TetrisOperation[MAX_TET_OPS];
    private static int findOperationByKey(char key) {
        for (int id = 0; operations[id] != null; id++) {
            if (operations[id].key == key) {
                return id;
            }
        }
        return -1;
    }
    public static void setOperation(char key, TetrisState preState, ActionHandler hDo, TetrisState postStateWDo,
                                    ActionHandler hUndo, TetrisState postStateWUndo) {
        int idx = findOperationByKey(key);
        if (idx >= 0)
            operations[idx] = new TetrisOperation(key, preState, hDo, postStateWDo, hUndo, postStateWUndo);
        else {
            if (nops == MAX_TET_OPS) {
                System.out.println("Tetris.operations[] is full.");
                return;
            }
            operations[nops] = new TetrisOperation(key, preState, hDo, postStateWDo, hUndo, postStateWUndo);
            nops++;
        }
    }
    private static void setDefaultOperations() {
        OnLeft myOnLeft = new OnLeft();
        OnRight myOnRight = new OnRight();
        OnDown myOnDown = new OnDown();
        OnUp myOnUp = new OnUp();
        OnDrop myOnDrop = new OnDrop();
        OnCw myOnCw = new OnCw();
        OnCcw myOnCcw = new OnCcw();
        OnNewBlock myOnNewBlock = new OnNewBlock();
        OnFinished myOnFinished = new OnFinished();
        setOperation('a', Running, myOnLeft, Running, myOnRight, Running);
        setOperation('d', Running, myOnRight, Running, myOnLeft, Running);
        setOperation('s', Running, myOnDown, Running, myOnUp, NewBlock);
        setOperation('w', Running, myOnCw, Running, myOnCcw, Running);
        setOperation(' ', Running, myOnDrop, Running, myOnUp, NewBlock);
        setOperation('0', NewBlock, myOnNewBlock, Running, myOnFinished, Finished);
        setOperation('1', NewBlock, myOnNewBlock, Running, myOnFinished, Finished);
        setOperation('2', NewBlock, myOnNewBlock, Running, myOnFinished, Finished);
        setOperation('3', NewBlock, myOnNewBlock, Running, myOnFinished, Finished);
        setOperation('4', NewBlock, myOnNewBlock, Running, myOnFinished, Finished);
        setOperation('5', NewBlock, myOnNewBlock, Running, myOnFinished, Finished);
        setOperation('6', NewBlock, myOnNewBlock, Running, myOnFinished, Finished);
    }
    public int iScreenDy;	// height of the background screen (excluding walls)
    public int iScreenDx;  // width of the background screen (excluding walls)
    public int top;		// y of the top left corner of the current block
    public int left;		// x of the top left corner of the current block
    public Matrix iScreen;	// input screen (as background)
    public Matrix oScreen;	// output screen
    public Matrix currBlk;	// current block
    public int idxBlockType;	// index for the current block type
    public int idxBlockDegree; // index for the current block degree
    private TetrisState state;		// game state
    private int[][] createArrayScreen(int dy, int dx, int dw) {
        int y, x;
        int[][] array = new int[dy + dw][dx + 2*dw];
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
    public void printScreen() {	Matrix screen = oScreen; // copied from oScreen
        int dy = screen.get_dy();
        int dx = screen.get_dx();
        int dw = iScreenDw;
        int array[][] = screen.get_array();
        for (int y = 0; y < dy - dw + 1; y++) {
            for (int x = dw - 1; x < dx - dw + 1; x++) {
                if (array[y][x] == 0) System.out.print("□ ");
                else if (array[y][x] == 1) System.out.print("■ ");
                else System.out.print("XX ");
            }
            System.out.println();
        }
    }
    private void printMatrix(Matrix blk) { // for debugging purposes
        int dy = blk.get_dy();
        int dx = blk.get_dx();
        int array[][] = blk.get_array();
        for (int y=0; y < dy; y++) {
            for (int x=0; x < dx; x++) {
                if (array[y][x] == 0) System.out.print("□ ");
                else if (array[y][x] == 1) System.out.print("■ ");
                else System.out.print("XX ");
            }
            System.out.println();
        }
    }
    public Tetris(int cy, int cx) throws Exception { // initialize dynamic variables
        if (cy < iScreenDw || cx < iScreenDw)
            throw new TetrisException("too small screen");
        iScreenDy = cy;
        iScreenDx = cx;
        int[][] arrayScreen = createArrayScreen(iScreenDy, iScreenDx, iScreenDw);
        state = NewBlock;	// The game should start with a new block needed!
        iScreen = new Matrix(arrayScreen);
        oScreen = new Matrix(iScreen);
    }
    public boolean anyConflict(boolean updateNeeded) throws Exception {
        boolean anyConflict;
        Matrix tempBlk;
        tempBlk = iScreen.clip(top, left, top + currBlk.get_dy(),left + currBlk.get_dx());
        tempBlk = tempBlk.add(currBlk);
        if (updateNeeded == true) {
            oScreen.paste(iScreen, 0, 0);
            oScreen.paste(tempBlk, top, left);
        }
        anyConflict = tempBlk.anyGreaterThan(1);
        return anyConflict;
    }
    public TetrisState accept(char key) throws Exception {
        if (nops == 0) setDefaultOperations();
        int idx = findOperationByKey(key);
        if (idx == -1) {
            System.out.println("unknown key!");
            return state;
        }
        TetrisOperation hop = operations[idx];
        if (state != hop.preState)
            throw new TetrisException("state != hop.preState");
        if (hop.hDo.run(this, key) == false) // no conflicts!
            state = hop.postStateWDo;
        else { // a conflict occurs!
            hop.hUndo.run(this, key);
            state = hop.postStateWUndo;
        }
        return state;
    }
}

class TetrisException extends Exception {
    public TetrisException() { super("Tetris Exception"); }
    public TetrisException(String msg) { super(msg); }
}
