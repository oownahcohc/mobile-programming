enum TetrisState {
    Running(0), NewBlock(1), Finished(2);
    private final int value;
    private TetrisState(int value) { this.value = value; }
    public int value() { return value; }
}

interface ActionHandler {
    public void run(Tetris t, char key) throws Exception;
}
class OnLeft implements ActionHandler {
    public void run(Tetris t, char key) throws Exception { t.left = t.left - 1; }
}
class OnRight implements ActionHandler {
    public void run(Tetris t, char key) throws Exception { t.left = t.left + 1; }
}
class OnDown implements ActionHandler {
    public void run(Tetris t, char key) throws Exception { t.top = t.top + 1; }
}
class OnUp implements ActionHandler {
    public void run(Tetris t, char key) throws Exception { t.top = t.top - 1; }
}
class OnCw implements ActionHandler {
    public void run(Tetris t, char key) throws Exception {
        t.idxBlockDegree = (t.idxBlockDegree+1)%t.nBlockDegrees;
        t.currBlk = t.setOfBlockObjects[t.idxBlockType][t.idxBlockDegree];
    }
}
class OnCcw implements ActionHandler {
    public void run(Tetris t, char key) throws Exception {
        t.idxBlockDegree = (t.idxBlockDegree+t.nBlockDegrees-1)%t.nBlockDegrees;
        t.currBlk = t.setOfBlockObjects[t.idxBlockType][t.idxBlockDegree];
    }
}
class OnNewBlock implements ActionHandler {
    public void run(Tetris t, char key) throws Exception {
        t.oScreen = deleteFullLines(t.oScreen, t.currBlk, t.top, t.iScreenDy, t.iScreenDx, t.iScreenDw);
        t.iScreen.paste(t.oScreen, 0, 0);
        t.idxBlockType = key - '0';
        t.idxBlockDegree = 0;
        t.currBlk = t.setOfBlockObjects[t.idxBlockType][t.idxBlockDegree];
        t.top = 0;
        t.left = t.iScreenDw + t.iScreenDx/2 - (t.currBlk.get_dx()+1)/2;
    }
    private Matrix deleteFullLines(Matrix screen, Matrix blk, int top, int dy, int dx, int dw) throws Exception {
        Matrix line, zero, temp;
        if (blk == null) return screen;
        int cy, y, nDeleted = 0,nScanned = blk.get_dy();
        if (top + blk.get_dy() - 1 >= dy)
            nScanned -= (top + blk.get_dy() - dy);
        zero = new Matrix(1, dx);
        for (y = nScanned - 1; y >= 0 ; y--) {
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
}
class OnFinished implements ActionHandler {
    public void run(Tetris t, char key) throws Exception {
        System.out.println("OnFinished.run() called");
    }
}

public class Tetris {
/*
interface ActionHandler {
    public void run(char key) throws Exception;
}
class OnLeft implements ActionHandler {
    public void run(char key) throws Exception { left = left - 1; }
}
class OnRight implements ActionHandler {
    public void run(char key) throws Exception { left = left + 1; }
}
class OnDown implements ActionHandler {
    public void run(char key) throws Exception { top = top + 1; }
}
class OnUp implements ActionHandler {
    public void run(char key) throws Exception { top = top - 1; }
}
class OnCw implements ActionHandler {
    public void run(char key) throws Exception {
        idxBlockDegree = (idxBlockDegree+1)%nBlockDegrees;
        currBlk = setOfBlockObjects[idxBlockType][idxBlockDegree];
    }
}
class OnCcw implements ActionHandler {
    public void run(char key) throws Exception {
        idxBlockDegree = (idxBlockDegree+nBlockDegrees-1)%nBlockDegrees;
        currBlk = setOfBlockObjects[idxBlockType][idxBlockDegree];
    }
}
class OnNewBlock implements ActionHandler {
    public void run(char key) throws Exception {
        oScreen = deleteFullLines(oScreen, currBlk, top, iScreenDy, iScreenDx, iScreenDw);
        iScreen.paste(oScreen, 0, 0);
        idxBlockType = key - '0';
        idxBlockDegree = 0;
        currBlk = setOfBlockObjects[idxBlockType][idxBlockDegree];
        top = 0;
        left = iScreenDw + iScreenDx/2 - (currBlk.get_dx()+1)/2;
    }
}
class OnFinished implements ActionHandler {
    public void run(char key) throws Exception {
        System.out.println("OnFinished.run() called");
    }
}
*/
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
        state = TetrisState.NewBlock;	// The game should start with a new block needed!
        iScreen = new Matrix(arrayScreen);
        oScreen = new Matrix(iScreen);
    }
    private static OnLeft onLeft = new OnLeft();
    private static OnRight onRight = new OnRight();
    private static OnDown onDown = new OnDown();
    private static OnUp onUp = new OnUp();
    private static OnCw onCw = new OnCw();
    private static OnCcw onCcw = new OnCcw();
    private static OnNewBlock onNewBlock = new OnNewBlock();
    private static OnFinished onFinished = new OnFinished();
    public static void setOnLeftListener(ActionHandler handler) { onLeft = (OnLeft) handler; }
    public static void setOnRightListener(ActionHandler handler) { onRight = (OnRight) handler; }
    public static void setOnDownListener(ActionHandler handler) { onDown = (OnDown) handler; }
    public static void setOnUpListener(ActionHandler handler) { onUp = (OnUp) handler; }
    public static void setOnCwListener(ActionHandler handler) { onCw = (OnCw) handler; }
    public static void setOnCcwListener(ActionHandler handler) { onCcw = (OnCcw) handler; }
    public static void setOnNewBlockListener(ActionHandler handler) { onNewBlock = (OnNewBlock) handler; }
    public static void setOnFinishedListener(ActionHandler handler) { onFinished = (OnFinished) handler; }
    private void moveLeft(Tetris t, char key) throws Exception { onLeft.run(t, key); }
    private void moveRight(Tetris t, char key) throws Exception { onRight.run(t, key); }
    private void moveDown(Tetris t, char key) throws Exception { onDown.run(t, key); }
    private void moveUp(Tetris t, char key) throws Exception { onUp.run(t, key); }
    private void rotateCw(Tetris t, char key) throws Exception { onCw.run(t, key); }
    private void rotateCcw(Tetris t, char key) throws Exception { onCcw.run(t, key); }
    private void insertBlk(Tetris t, char key) throws Exception { onNewBlock.run(t, key); }
    private void endGame(Tetris t, char key) throws Exception { onFinished.run(t, key); }
    public TetrisState accept(char key) throws Exception {
        Matrix tempBlk;
        boolean collided;
        if (state == TetrisState.NewBlock) {
            insertBlk(this, key);
            state = TetrisState.Running;
            tempBlk = iScreen.clip(top, left, top+currBlk.get_dy(), left+currBlk.get_dx());
            tempBlk = tempBlk.add(currBlk);
            collided = tempBlk.anyGreaterThan(1);
            oScreen.paste(iScreen, 0, 0);
            oScreen.paste(tempBlk, top, left);
            if (collided == true) {
                endGame(this, key);
                state = TetrisState.Finished;
                return state;
            }
            return state;
        }
        switch(key) {
            case 'a': moveLeft(this, key); break; // move left
            case 'd': moveRight(this, key); break; // move right
            case 's': moveDown(this, key); break; // move down
            case 'w': rotateCw(this, key); break; // rotateCW
            case ' ': // drop the block
                do {
                    moveDown(this, key);
                    tempBlk = iScreen.clip(top, left, top+currBlk.get_dy(), left+currBlk.get_dx());
                    tempBlk = tempBlk.add(currBlk);
                } while (tempBlk.anyGreaterThan(1) == false);
                break;
            default: System.out.println("unknown key!");
        }
        tempBlk = iScreen.clip(top, left, top+currBlk.get_dy(), left+currBlk.get_dx());
        tempBlk = tempBlk.add(currBlk);
        if (tempBlk.anyGreaterThan(1)) {
            switch(key) {
                case 'a': moveRight(this, key); break; // undo: move right
                case 'd': moveLeft(this, key); break; // undo: move left
                case 's': moveUp(this, key); state = TetrisState.NewBlock; break; // undo: move up
                case 'w': rotateCcw(this, key); break; // undo: rotateCCW
                case ' ': moveUp(this, key);; state = TetrisState.NewBlock; break; // undo: move up
            }
            tempBlk = iScreen.clip(top, left, top+currBlk.get_dy(), left+currBlk.get_dx());
            tempBlk = tempBlk.add(currBlk);
        }
        oScreen.paste(iScreen, 0, 0);
        oScreen.paste(tempBlk, top, left);
        return state;
    }
    /*
    private void moveLeft(char key) throws Exception { onLeft.run(key); }
    private void moveRight(char key) throws Exception { onRight.run(key); }
    private void moveDown(char key) throws Exception { onDown.run(key); }
    private void moveUp(char key) throws Exception { onUp.run(key); }
    private void rotateCw(char key) throws Exception { onCw.run(key); }
    private void rotateCcw(char key) throws Exception { onCcw.run(key); }
    private void insertBlk(char key) throws Exception { onNewBlock.run(key); }
    private void endGame(char key) throws Exception { onFinished.run(key); }
    public TetrisState accept(char key) throws Exception {
        Matrix tempBlk;
        boolean collided;
        if (state == TetrisState.NewBlock) {
            insertBlk(key);
            state = TetrisState.Running;
            tempBlk = iScreen.clip(top, left, top+currBlk.get_dy(), left+currBlk.get_dx());
            tempBlk = tempBlk.add(currBlk);
            collided = tempBlk.anyGreaterThan(1);
            oScreen.paste(iScreen, 0, 0);
            oScreen.paste(tempBlk, top, left);
            if (collided == true) {
                endGame(key);
                state = TetrisState.Finished;
                return state;
            }
            return state;
        }
        switch(key) {
            case 'a': moveLeft(key); break; // move left
            case 'd': moveRight(key); break; // move right
            case 's': moveDown(key); break; // move down
            case 'w': rotateCw(key); break; // rotateCW
            case ' ': // drop the block
                do {
                    moveDown(key);
                    tempBlk = iScreen.clip(top, left, top+currBlk.get_dy(), left+currBlk.get_dx());
                    tempBlk = tempBlk.add(currBlk);
                } while (tempBlk.anyGreaterThan(1) == false);
                break;
            default: System.out.println("unknown key!");
        }
        tempBlk = iScreen.clip(top, left, top+currBlk.get_dy(), left+currBlk.get_dx());
        tempBlk = tempBlk.add(currBlk);
        if (tempBlk.anyGreaterThan(1)) {
            switch(key) {
                case 'a': moveRight(key); break; // undo: move right
                case 'd': moveLeft(key); break; // undo: move left
                case 's': moveUp(key); state = TetrisState.NewBlock; break; // undo: move up
                case 'w': rotateCcw(key); break; // undo: rotateCCW
                case ' ': moveUp(key);; state = TetrisState.NewBlock; break; // undo: move up
            }
            tempBlk = iScreen.clip(top, left, top+currBlk.get_dy(), left+currBlk.get_dx());
            tempBlk = tempBlk.add(currBlk);
        }
        oScreen.paste(iScreen, 0, 0);
        oScreen.paste(tempBlk, top, left);
        return state;
    }
    */
}

class TetrisException extends Exception {
    public TetrisException() { super("Tetris Exception"); }
    public TetrisException(String msg) { super(msg); }
}
