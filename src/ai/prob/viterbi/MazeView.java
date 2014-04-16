package ai.prob.viterbi;

import java.util.ArrayList;

import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;

@SuppressWarnings("all")
public class MazeView extends Group {

	private int pixelsPerSquare;
	public Maze maze;
	private ArrayList<Node> pieces;

	private int numCurrentAnimations;

	public Rectangle[][] squares;

	private static Color[] colors = { Color.RED, Color.ORANGE, Color.BLACK,
			Color.BROWN, Color.DARKGOLDENROD, Color.GREEN, Color.BLUE,
			Color.VIOLET, Color.CRIMSON };

	int currentColor;

	public MazeView(Maze m, int pixelsPerSquare) {
		currentColor = 0;

		pieces = new ArrayList<Node>();
		maze = m;
		squares = new Rectangle[maze.height][maze.width];

		this.pixelsPerSquare = pixelsPerSquare;

		// Color colors[] = { Color.LIGHTGRAY, Color.WHITE };
		// int color_index = 1; // alternating index to select tile color

		for (int c = 0; c < maze.width; c++) {

			for (int r = 0; r < maze.height; r++) {

				int x = c * pixelsPerSquare;
				int y = (maze.height - r - 1) * pixelsPerSquare;

				Rectangle square = new Rectangle(x, y, pixelsPerSquare,
						pixelsPerSquare);
				squares[r][c] = square;
				square.setStroke(Color.GRAY);
				square.setFill(Color.WHITE);
				this.getChildren().add(square);
			}
		}
	}

	private int squareCenterX(int c) {
		return c * pixelsPerSquare + pixelsPerSquare / 2;

	}

	private int squareCenterY(int r) {
		return (maze.height - r) * pixelsPerSquare - pixelsPerSquare / 2;
	}

	public Node addPieceInUniformColor(int c, int r, Color color) {
		int radius = (int) (pixelsPerSquare * .4);
		Circle piece = new Circle(squareCenterX(c), squareCenterY(r), radius);
		piece.setFill(color);
		// this.getChildren().add(piece);
		return piece;
	}

}
