package ai.prob.viterbi;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import javafx.scene.paint.Color;

public class Maze {
	final static Charset ENCODING = StandardCharsets.UTF_8;
	public int width;
	public int height;
	// the map
	private char[][] grid;

	public static Maze readFromFile(String filename) {
		Maze m = new Maze();
		try {
			List<String> lines = readFile(filename);
			m.height = lines.size();

			int y = 0;
			m.grid = new char[m.height][];
			for (String line : lines) {
				m.width = line.length();
				m.grid[m.height - y - 1] = new char[m.width];
				for (int x = 0; x < line.length(); x++) {
					m.grid[m.height - y - 1][x] = line.charAt(x);
				}
				y++;
			}

			return m;
		} catch (IOException E) {
			return null;
		}
	}

	private static List<String> readFile(String fileName) throws IOException {
		Path path = Paths.get(fileName);
		return Files.readAllLines(path, ENCODING);
	}

	public char getChar(int x, int y) {
		return grid[y][x];
	}

	public boolean isTile(int x, int y) {
		return getChar(x, y) == 'r' || getChar(x, y) == 'g'
				|| getChar(x, y) == 'b' || getChar(x, y) == 'y';
	}

	public Color getColor(int x, int y) {
		char c = this.getChar(x, y);
		switch (c) {
		case 'r':
			return Color.LIGHTPINK;
		case 'g':
			return Color.LIGHTGREEN;
		case 'b':
			return Color.LIGHTBLUE;
		case 'y':
			return Color.LIGHTYELLOW;
		default:
			return Color.LIGHTGREY;
		}
	}

	public boolean isLegal(int x, int y) {
		if (x >= 0 && x < width && y >= 0 && y < height) {
			return isTile(x, y);
		}
		return false;
	}

	public String toString() {
		String s = "";
		for (int y = height - 1; y >= 0; y--) {
			for (int x = 0; x < width; x++) {
				s += grid[y][x];
			}
			s += "\n";
		}
		return s;
	}
}
