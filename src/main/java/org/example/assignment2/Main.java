package org.example.assignment2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Random;

public class Main {

	public static void main(String[] args) throws Exception {
		TetrisController tetrisController = new TetrisController(new View.Input(), new View.Output());
		tetrisController.run();
	}
}

class TetrisController {

	private static final Random RANDOM = new Random();
	private final View.Input input;
	private final View.Output output;

	public TetrisController(View.Input input, View.Output output) {
		this.input = input;
		this.output = output;
	}

	public void run() throws Exception {
		char key;
		CTetris.init(SET_OF_BLOCK_ARRAYS);
		CTetris board = new CTetris(15, 10);
		key = (char) ('0' + RANDOM.nextInt(7));
		board.accept(key);
		output.drawMatrix(board.get_oScreen());

		while ((key = input.getKey()) != 'q') {
			TetrisState state = board.accept(key);
			output.drawMatrix(board.get_oScreen());
			if (state == TetrisState.NEW_BLOCK) {
				key = (char) ('0' + RANDOM.nextInt(7));
				state = board.accept(key);
				output.drawMatrix(board.get_oScreen());
				if (state == TetrisState.FINISHED)
					break; // Game Over!
			}
		}
		output.printGameOver();
	}

	private static final int[][][][] SET_OF_BLOCK_ARRAYS = { // [7][4][?][?]
		{
			{
				{10, 10},
				{10, 10}
			},
			{
				{10, 10},
				{10, 10}
			},
			{
				{10, 10},
				{10, 10}
			},
			{
				{10, 10},
				{10, 10}
			}
		},
		{
			{
				{0, 20, 0},
				{20, 20, 20},
				{0, 0, 0},
			},
			{
				{0, 20, 0},
				{0, 20, 20},
				{0, 20, 0},
			},
			{
				{0, 0, 0},
				{20, 20, 20},
				{0, 20, 0},
			},
			{
				{0, 20, 0},
				{20, 20, 0},
				{0, 20, 0},
			},
		},
		{
			{
				{30, 0, 0},
				{30, 30, 30},
				{0, 0, 0},
			},
			{
				{0, 30, 30},
				{0, 30, 0},
				{0, 30, 0},
			},
			{
				{0, 0, 0},
				{30, 30, 30},
				{0, 0, 30},
			},
			{
				{0, 30, 0},
				{0, 30, 0},
				{30, 30, 0},
			},
		},
		{
			{
				{0, 0, 40},
				{40, 40, 40},
				{0, 0, 0},
			},
			{
				{0, 40, 0},
				{0, 40, 0},
				{0, 40, 40},
			},
			{
				{0, 0, 0},
				{40, 40, 40},
				{40, 0, 0},
			},
			{
				{40, 40, 0},
				{0, 40, 0},
				{0, 40, 0},
			},
		},
		{
			{
				{0, 50, 0},
				{50, 50, 0},
				{50, 0, 0},
			},
			{
				{50, 50, 0},
				{0, 50, 50},
				{0, 0, 0},
			},
			{
				{0, 50, 0},
				{50, 50, 0},
				{50, 0, 0},
			},
			{
				{50, 50, 0},
				{0, 50, 50},
				{0, 0, 0},
			},
		},
		{
			{
				{0, 60, 0},
				{0, 60, 60},
				{0, 0, 60},
			},
			{
				{0, 0, 0},
				{0, 60, 60},
				{60, 60, 0},
			},
			{
				{0, 60, 0},
				{0, 60, 60},
				{0, 0, 60},
			},
			{
				{0, 0, 0},
				{0, 60, 60},
				{60, 60, 0},
			},
		},
		{
			{
				{0, 0, 0, 0},
				{70, 70, 70, 70},
				{0, 0, 0, 0},
				{0, 0, 0, 0},
			},
			{
				{0, 70, 0, 0},
				{0, 70, 0, 0},
				{0, 70, 0, 0},
				{0, 70, 0, 0},
			},
			{
				{0, 0, 0, 0},
				{70, 70, 70, 70},
				{0, 0, 0, 0},
				{0, 0, 0, 0},
			},
			{
				{0, 70, 0, 0},
				{0, 70, 0, 0},
				{0, 70, 0, 0},
				{0, 70, 0, 0},
			},
		},
	}; // end of setOfBlockArrays
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

		public void drawMatrix(Matrix m) {
			int dy = m.get_dy();
			int dx = m.get_dx();
			int[][] array = m.get_array();
			for (int y = 0; y < dy; y++) {
				for (int x = 0; x < dx; x++) {
					switch (array[y][x]) {
						case 0 -> System.out.print("□ ");
						case 1 -> System.out.print("X ");
						case 10 -> System.out.print("● ");
						case 20 -> System.out.print("■ ");
						case 30 -> System.out.print("♣ ");
						case 40 -> System.out.print("♥ ");
						case 50 -> System.out.print("▲ ");
						case 60 -> System.out.print("▼ ");
						case 70 -> System.out.print("♠ ");
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
