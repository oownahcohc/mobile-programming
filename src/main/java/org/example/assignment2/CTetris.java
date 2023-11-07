package org.example.assignment2;

import java.util.Arrays;

public class CTetris extends Tetris {

	public CTetris(int cy, int cx) throws Exception {
		super(cy, cx);
	}

	@Override
	public TetrisState accept(char key) throws Exception {
		TetrisState state = super.accept(key);
		if (state == TetrisState.NEW_BLOCK) {
			oScreen = deleteColorFullLines(oScreen, iScreenDw);
			iScreen.paste(oScreen, 0, 0);
		}
		return state;
	}

	public Matrix deleteColorFullLines(Matrix screen, int wallDepth) throws MatrixException {
		Matrix tempScreen = new Matrix(screen.clip(0, iScreenDw, iScreenDy, iScreenDx + wallDepth));
		tempScreen = deleteCompleteLines(tempScreen);
		screen.paste(tempScreen, 0, iScreenDw);
		return screen;
	}

	public Matrix deleteCompleteLines(Matrix screen) throws MatrixException {
		Matrix updatedScreen = new Matrix(screen);
		while (lineIsFull(updatedScreen)) {
			int fullLineIndex = getFirstFullLineIndex(updatedScreen);
			updatedScreen = collapseLine(updatedScreen, fullLineIndex);
		}
		return updatedScreen;
	}

	public boolean lineIsFull(Matrix screen) {
		try {
			getFirstFullLineIndex(screen);
			return true;
		} catch (IllegalStateException e) {
			return false;
		}
	}

	private int getFirstFullLineIndex(Matrix screen) {
		for (int rowIndex = screen.get_dy() - 1; rowIndex >= 0; rowIndex--) {
			if (isRowFull(screen, rowIndex)) {
				return rowIndex;
			}
		}
		throw new IllegalStateException("No full lines found.");
	}

	private boolean isRowFull(Matrix screen, int rowIndex) {
		for (int columnIndex = 0; columnIndex < screen.get_dx(); columnIndex++) {
			if (screen.get_array()[rowIndex][columnIndex] == 0) {
				return false;
			}
		}
		return true;
	}

	private Matrix collapseLine(Matrix screen, int fullLineIndex) throws MatrixException {
		Matrix newScreen = new Matrix(screen);
		int width = screen.get_dx();
		int[][] array = newScreen.get_array();

		for (int rowIndex = fullLineIndex; rowIndex > 0; rowIndex--) {
			System.arraycopy(array[rowIndex - 1], 0, array[rowIndex], 0, width);
		}

		// 맨 위의 행을 0으로 초기화 -> 새로운 빈 행 만들기
		Arrays.fill(array[0], 0);

		return newScreen;
	}

	@Override
	public Matrix get_oScreen() throws MatrixException {
		return oScreen.clip(0, iScreenDw - 1, iScreenDy + 1, iScreenDy);
	}
}
