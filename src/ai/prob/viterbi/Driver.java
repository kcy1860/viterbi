package ai.prob.viterbi;


import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import ai.prob.viterbi.PRSolver.PathNode;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;

public class Driver extends Application {

	Maze maze;
	int goalX, goalY;
	private static final int PIXELS_PER_SQUARE = 32;
	MazeView mazeView;
	MazeView initView;
	List<AnimationPath> animationPathList;

	TextArea logArea;
	TextArea inputArea;

	private void initMazeView() {
		maze = Maze.readFromFile("pr5*5.maz");
		animationPathList = new ArrayList<AnimationPath>();
		mazeView = new MazeView(maze, PIXELS_PER_SQUARE);
		initView = new MazeView(maze, PIXELS_PER_SQUARE);
		logArea = new TextArea();
		logArea.setPrefColumnCount(maze.width);
		logArea.setPrefRowCount(maze.height + 2);
		logArea.setPrefWidth(maze.width * 2 * PIXELS_PER_SQUARE);
		logArea.setEditable(false);
		logArea.setWrapText(true);

		inputArea = new TextArea();
		inputArea.setPrefColumnCount(maze.width);
		inputArea.setPrefRowCount(2);
		// inputArea.setPrefWidth(maze.width * 2 * PIXELS_PER_SQUARE);
		inputArea.setEditable(false);
		inputArea.setWrapText(true);
	}

	char[] input;

	// assumes maze and mazeView instance variables are already available
	private void runSearches() {
		goalX = 1;
		goalY = 1;
		PRSolver solver = new PRSolver(maze);
		input = new char[] { 'b', 'g', 'r', 'b', 'g', 'r', 'y' };
		// input = new char[] { 'b', 'y', 'g', 'b', 'r', 'r', 'g', 'g', 'y', 'g'
		// };
		this.inputArea.appendText(Utils.buildInputString(input, -1));
		List<PathNode> path = solver.getPath(input);
		int[][] vpath = solver.getViterbiPath(input);
		animationPathList.add(new AnimationPath(mazeView, path, vpath));
	}

	public static void main(String[] args) {
		launch(args);
	}

	// javafx setup of main view window for mazeworld
	@Override
	public void start(Stage primaryStage) {

		initMazeView();

		primaryStage.setTitle("Viterbi");

		// add everything to a root stackpane, and then to the main window
		StackPane root = new StackPane();

		HBox hb = new HBox();
		hb.getChildren().addAll(this.initView, this.mazeView);
		hb.setSpacing(10);
		hb.setPadding(new Insets(10, 10, 10, 10));

		VBox vb = new VBox();
		vb.getChildren().addAll(hb, inputArea, logArea);
		vb.setSpacing(10);
		vb.setPadding(new Insets(10, 10, 10, 10));

		root.getChildren().add(vb);
		primaryStage.setScene(new Scene(root));

		primaryStage.show();

		runSearches();

		Timeline timeline = new Timeline(1.0);
		timeline.setCycleCount(Timeline.INDEFINITE);
		timeline.getKeyFrames().add(
				new KeyFrame(Duration.seconds(.05), new GameHandler()));
		timeline.playFromStart();

	}

	// every frame, this method gets called and tries to do the next move
	// for each animationPath.
	private class GameHandler implements EventHandler<ActionEvent> {

		@Override
		public void handle(ActionEvent e) {
			for (AnimationPath animationPath : animationPathList) {
				animationPath.doNextMove();
			}
		}
	}

	// each animation path needs to keep track of some information:
	// the underlying search path, the "piece" object used for animation,
	// etc.
	private class AnimationPath {
		private List<PathNode> searchPath;
		private int currentMove = 0;
		boolean animationDone = true;
		Queue<Node> currentNodes = new LinkedList<Node>();
		MazeView view;
		Node lk;
		public int[][] vpath;
		int lastX = 0, lastY = 0;

		public AnimationPath(MazeView mazeView, List<PathNode> path, int[][] vp) {
			this.vpath = vp;
			searchPath = path;
			this.view = mazeView;

			PathNode node = (PathNode) searchPath.get(currentMove);
			for (int i = 0; i < node.getState().length; i++) {
				for (int j = 0; j < node.getState()[0].length; j++) {
					double p = node.getState()[i][j];
					view.squares[i][j].setFill(Utils.getColor(
							view.maze.getChar(j, i), p));
					initView.squares[i][j].setFill(Utils.getColor(
							view.maze.getChar(j, i), p));
				}
			}
			this.currentMove++;
		}

		// try to do the next step of the animation. Do nothing if
		// the mazeView is not ready for another step.
		public void doNextMove() {
			if (currentMove < searchPath.size() && animationDone) {
				animateMove();
				currentMove++;
			}
		}

		// move the piece n by dx, dy cells
		public void animateMove() {
			// delete points from last move
			while (!currentNodes.isEmpty()) {
				mazeView.getChildren().remove(currentNodes.poll());
			}
			PathNode node = (PathNode) searchPath.get(currentMove);
			for (int i = 0; i < node.getState().length; i++) {
				for (int j = 0; j < node.getState()[0].length; j++) {
					double p = node.getState()[i][j];
					view.squares[i][j].setFill(Utils.getColor(
							view.maze.getChar(j, i), p));
				}
			}

			double time = 500;
			if (this.lk == null) {
				int x = this.vpath[0][currentMove];
				int y = this.vpath[1][currentMove];
				this.lastX = x;
				this.lastY = y;
				lk = mazeView.addPieceInUniformColor(x, y, Color.GOLD);
				mazeView.getChildren().add(lk);
				TranslateTransition tt = new TranslateTransition(
						Duration.millis(time), lk);
				tt.setByX(0);
				tt.setByY(0);
				tt.setOnFinished(new AnimationFinished());
				tt.play();

				logArea.setText(Utils.matrix2String(node.getState()));
				inputArea.setText(Utils.buildInputString(input, currentMove));
			} else {
				int x = this.vpath[0][currentMove];
				int y = this.vpath[1][currentMove];
				TranslateTransition tt = new TranslateTransition(
						Duration.millis(time), lk);
				tt.setByX(PIXELS_PER_SQUARE * (x - lastX));
				tt.setByY(-PIXELS_PER_SQUARE * (y - lastY));
				lastX = x;
				lastY = y;
				tt.setOnFinished(new AnimationFinished());
				tt.play();

				logArea.setText(Utils.matrix2String(node.getState()));
				inputArea.setText(Utils.buildInputString(input, currentMove));
				animationDone = false;
			}
		}

		private class AnimationFinished implements EventHandler<ActionEvent> {
			@Override
			public void handle(ActionEvent event) {
				animationDone = true;
			}
		}
	}
}