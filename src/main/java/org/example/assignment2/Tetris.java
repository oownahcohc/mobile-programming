package org.example.assignment2;

import java.util.stream.IntStream;

public class Tetris {

	private static final char LEFT_KEY = 'a';
	private static final char RIGHT_KEY = 'd';
	private static final char DOWN_KEY = 's';
	private static final char ROTATE_KEY = 'w';
	private static final char DROP_KEY = ' ';

	protected static int iScreenDw;        // large enough to cover the largest block
	protected static int nBlockTypes;        // number of block types (typically 7)
	protected static int nBlockDegrees;    // number of block degrees (typically 4)
	protected static Matrix[][] setOfBlockObjects;    // Matrix object arrays of all blocks

	protected final int iScreenDy;    // height of the background screen (excluding walls)
	protected final int iScreenDx;  // width of the background screen (excluding walls)
	protected TetrisState state;        // game state
	protected int top;        // y of the top left corner of the current block
	protected int left;        // x of the top left corner of the current block
	protected Matrix iScreen;    // input screen (as background)
	protected Matrix oScreen;    // output screen
	protected Matrix currBlk;    // current block
	protected int idxBlockType;    // index for the current block type
	protected int idxBlockDegree; // index for the current block degree

	public Tetris(int cy, int cx) throws Exception { // initialize dynamic variables
		if (cy < iScreenDw || cx < iScreenDw) {
			throw new TetrisException("too small screen");
		}
		iScreenDy = cy;
		iScreenDx = cx;
		int[][] arrayScreen = createArrayScreen();
		state = TetrisState.NEW_BLOCK;    // The game should start with a new block needed!
		iScreen = new Matrix(arrayScreen);
		oScreen = new Matrix(iScreen);
	}

	private int[][] createArrayScreen() {
		int[][] array = new int[iScreenDy + iScreenDw][iScreenDx + 2 * iScreenDw];
		for (int y = 0; y < array.length; y++) {
			for (int x = 0; x < iScreenDw; x++) {
				array[y][x] = 1;
			}
		}
		for (int y = 0; y < array.length; y++) {
			for (int x = iScreenDw + iScreenDx; x < array[0].length; x++) {
				array[y][x] = 1;
			}
		}
		for (int y = iScreenDy; y < array.length; y++) {
			for (int x = 0; x < array[0].length; x++) {
				array[y][x] = 1;
			}
		}
		return array;
	}

	public static void init(int[][][][] setOfBlockArrays) throws Exception { // initialize static variables
		nBlockTypes = setOfBlockArrays.length;
		nBlockDegrees = setOfBlockArrays[0].length;
		setOfBlockObjects = createSetOfBlocks(setOfBlockArrays);
		iScreenDw = findLargestBlockSize(setOfBlockArrays);
	}

	private static Matrix[][] createSetOfBlocks(int[][][][] setOfArrays) throws Exception {
		Matrix[][] setOfBlocks = new Matrix[nBlockTypes][nBlockDegrees];
		for (int blockTypeIndex = 0; blockTypeIndex < nBlockTypes; blockTypeIndex++) {
			for (int blockDegreeIndex = 0; blockDegreeIndex < nBlockDegrees; blockDegreeIndex++) {
				int[][] tetriminoArray = setOfArrays[blockTypeIndex][blockDegreeIndex];
				setOfBlocks[blockTypeIndex][blockDegreeIndex] = new Matrix(tetriminoArray);
			}
		}
		return setOfBlocks;
	}

	private static int findLargestBlockSize(int[][][][] setOfArrays) {
		int max_size = 0;
		for (int t = 0; t < nBlockTypes; t++) {
			for (int d = 0; d < nBlockDegrees; d++) {
				int size = setOfArrays[t][d].length;
				max_size = Math.max(max_size, size);
			}
		}
		return max_size;
	}

	public TetrisState accept(char key) throws Exception {
		Matrix tempBlk;
		if (state == TetrisState.NEW_BLOCK) {
			deleteFullLines();
			setRandomTetriminoOnCurrentBlock(key);
			initScreenSetting();

			tempBlk = iScreen.clip(top, left, top + currBlk.get_dy(), left + currBlk.get_dx());
			int maxValue = findMaxValueInMatrices(tempBlk, currBlk);
			tempBlk = tempBlk.add(currBlk);
			oScreen.paste(iScreen, 0, 0);
			oScreen.paste(tempBlk, top, left);

			if (tempBlk.anyGreaterThan(maxValue)) {
				state = TetrisState.FINISHED;
				return state;
			}
			return state; // should require a key input
		}

		switch (key) {
			case LEFT_KEY -> left--;
			case RIGHT_KEY -> left++;
			case DOWN_KEY -> top++;
			case ROTATE_KEY -> {
				idxBlockDegree = (idxBlockDegree + 1) % nBlockDegrees;
				currBlk = setOfBlockObjects[idxBlockType][idxBlockDegree];
			}
			case DROP_KEY -> {
				Matrix clip = iScreen.clip(top, left, top + currBlk.get_dy(), left + currBlk.get_dx());
				int maxValue = findMaxValueInMatrices(clip, currBlk);
				while (!clip.anyGreaterThan(maxValue)) {
					top++;
					clip = iScreen.clip(top, left, top + currBlk.get_dy(), left + currBlk.get_dx());
					maxValue = findMaxValueInMatrices(clip, currBlk);
					clip = clip.add(currBlk);
				}
			}
			default -> System.out.println("unknown key!");
		}
		tempBlk = iScreen.clip(top, left, top + currBlk.get_dy(), left + currBlk.get_dx());
		int maxValue = findMaxValueInMatrices(tempBlk, currBlk);
		tempBlk = tempBlk.add(currBlk);

		if (tempBlk.anyGreaterThan(maxValue)) {
			switch (key) {
				case LEFT_KEY -> left++;
				case RIGHT_KEY -> left--;
				case DOWN_KEY, DROP_KEY -> {
					top--;
					state = TetrisState.NEW_BLOCK;
				}
				case ROTATE_KEY -> {
					idxBlockDegree = (idxBlockDegree + nBlockDegrees - 1) % nBlockDegrees;
					currBlk = setOfBlockObjects[idxBlockType][idxBlockDegree];
				}
			}
			tempBlk = iScreen.clip(top, left, top + currBlk.get_dy(), left + currBlk.get_dx());
			tempBlk = tempBlk.add(currBlk);
		}
		oScreen.paste(iScreen, 0, 0);
		oScreen = new Matrix(iScreen);
		oScreen.paste(tempBlk, top, left);
		return state;
	}

	private void deleteFullLines() throws Exception {
		oScreen = fullLineDelete(oScreen, currBlk, top, iScreenDy, iScreenDx, iScreenDw);
		iScreen.paste(oScreen, 0, 0);
	}

	protected Matrix fullLineDelete(Matrix screen, Matrix blk, int top, int dy, int dx, int dw) throws Exception {
		return screen;
	}

	private void setRandomTetriminoOnCurrentBlock(char key) {
		idxBlockType = key - '0'; // copied from key
		idxBlockDegree = 0;
		currBlk = setOfBlockObjects[idxBlockType][idxBlockDegree];
	}

	private void initScreenSetting() {
		top = 0;
		state = TetrisState.RUNNING;
		left = iScreenDw + iScreenDx / 2 - (currBlk.get_dx() + 1) / 2;
	}

	private int findMaxValueInMatrices(Matrix clipMatrix, Matrix blockMatrix) {
		validateMatricesSizeMatch(clipMatrix, blockMatrix);
		return IntStream.range(0, clipMatrix.get_dy())
			.flatMap(y ->
				IntStream.range(0, clipMatrix.get_dx())
					.map(x -> Math.max(
						clipMatrix.get_array()[y][x],
						blockMatrix.get_array()[y][x]))
			)
			.max()
			.orElseThrow(() -> new IllegalStateException("Unable to find the maximum value in matrices"));
	}

	private void validateMatricesSizeMatch(Matrix clipMatrix, Matrix blockMatrix) {
		if (!(blockMatrix.get_dy() == clipMatrix.get_dy() && blockMatrix.get_dx() == clipMatrix.get_dx())) {
			throw new IllegalArgumentException("Matrices sizes do not match");
		}
	}

	public Matrix get_oScreen() throws MatrixException {
		return oScreen;
	}
}

enum TetrisState {

	RUNNING(0),
	NEW_BLOCK(1),
	FINISHED(2),
	;

	private final int value;

	TetrisState(int value) {
		this.value = value;
	}

	public int value() {
		return value;
	}
}

class TetrisException extends Exception {

	public TetrisException() {
		super("Tetris Exception");
	}

	public TetrisException(String msg) {
		super(msg);
	}
}
