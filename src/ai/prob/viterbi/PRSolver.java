package ai.prob.viterbi;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

public class PRSolver {

	// transition matrix
	private double[][] transitionMatrix;
	// the initial distribution
	private double[] initialState;

	// size of the board
	private int height;
	private int width;

	// the board
	private Maze maze;

	// the observation matrices
	private Map<Character, double[]> sensorModel;

	public PRSolver(Maze m) {
		this.height = m.height;
		this.width = m.width;
		this.sensorModel = new HashMap<Character, double[]>();
		this.initialState = new double[this.height * this.width];
		this.maze = m;

		// initialize the matrices
		this.setSensorMatrix();
		this.setInitialDistribution();
		this.setTransitionMatrix();
	}

	/**
	 * initialize transition matrix
	 */
	private void setTransitionMatrix() {
		int size = this.height * this.width;
		this.transitionMatrix = new double[size][size];
		// the possibility of moving towards whatever direction
		double unit = 0.25;
		for (int from = 0; from < height; from++) {
			for (int to = 0; to < width; to++) {
				if (maze.isLegal(to, from)) {
					double total = 0.0;
					if (maze.isLegal(to - 1, from)) {
						total += unit;
						this.transitionMatrix[this.getFlattedCoordinate(to,
								from)][this.getFlattedCoordinate(to - 1, from)] = unit;
					}

					if (maze.isLegal(to + 1, from)) {
						total += unit;
						this.transitionMatrix[this.getFlattedCoordinate(to,
								from)][this.getFlattedCoordinate(to + 1, from)] = unit;
					}

					if (maze.isLegal(to, from - 1)) {
						total += unit;
						this.transitionMatrix[this.getFlattedCoordinate(to,
								from)][this.getFlattedCoordinate(to, from - 1)] = unit;
					}

					if (maze.isLegal(to, from + 1)) {
						total += unit;
						this.transitionMatrix[this.getFlattedCoordinate(to,
								from)][this.getFlattedCoordinate(to, from + 1)] = unit;
					}

					this.transitionMatrix[this.getFlattedCoordinate(to, from)][this
							.getFlattedCoordinate(to, from)] = 1.0 - total;
				}
			}
		}
	}

	/**
	 * set the initial distribution as uniform distribution
	 */
	private void setInitialDistribution() {
		int numberOfTiles = 0;
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				if (maze.isLegal(x, y)) {
					numberOfTiles++;
				}
			}
		}
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				if (maze.isLegal(x, y)) {
					this.initialState[this.getFlattedCoordinate(x, y)] = (double) 1.0
							/ numberOfTiles;
				}
			}
		}
	}

	/**
	 * initialize the observation matrix
	 */
	private void setSensorMatrix() {
		int size = this.height * this.width;
		char[] colors = new char[] { 'b', 'r', 'g', 'y' };
		for (char c : colors) {
			double[] sensorDistribution = new double[size];
			for (int y = 0; y < height; y++) {
				for (int x = 0; x < width; x++) {
					double value = maze.isLegal(x, y) ? (maze.getChar(x, y) == c ? 0.88
							: 0.04)
							: 0.0;
					sensorDistribution[this.getFlattedCoordinate(x, y)] = value;
				}
			}
			this.sensorModel.put(c, sensorDistribution);
		}
	}

	/**
	 * get the distribution of each step
	 * 
	 * @param sensorInput
	 * @return
	 */
	public List<PathNode> getPath(char[] sensorInput) {
		List<PathNode> result = new LinkedList<PathNode>();
		// put the input sequence to a queue
		Queue<Character> input = new LinkedList<Character>();
		for (char c : sensorInput) {
			input.add(c);
		}
		this.filtering(input, this.initialState.clone(), result);
		return result;
	}

	/**
	 * compute the distribution of each step according to the current
	 * observation and previous status
	 * 
	 * @param input
	 * @param currentState
	 * @param path
	 */
	private void filtering(Queue<Character> input, double[] currentState,
			List<PathNode> path) {
		path.add(new PathNode(currentState));
		if (input.isEmpty()) {
			return;
		}

		char currentInput = input.poll().charValue();

		// get the observation matrix according to the current observation
		double[] sensorMatrix = this.sensorModel.get(currentInput);
		// apply to the transition matrix
		Utils.multiply(currentState, this.transitionMatrix);
		// apply the observation
		Utils.multiply(currentState, sensorMatrix);
		// normalize
		Utils.normalize(currentState);
		// next recursion
		this.filtering(input, currentState, path);
	}

	/**
	 * get the Viterbi path according to observation sequence
	 * 
	 * @return int[][] result where: 
	 *         result[0][k] = coordinate x in step k
	 *         result[1][k] = coordinate y in step k
	 */
	public int[][] getViterbiPath(char[] sensorInput) {
		int num_of_states = this.initialState.length;
		int num_of_steps = sensorInput.length;

		// store the distributions
		double[][] distribution = new double[num_of_states][num_of_steps];
		// store the path
		int[][] vpath = new int[num_of_states][num_of_steps];

		// compute the distribution in the first observation
		double[] observation = this.sensorModel.get(sensorInput[0]);
		for (int i = 0; i < num_of_states; i++) {
			if (initialState[i] == 0.0 || observation[i] == 0.0) {
				distribution[i][0] = Double.NEGATIVE_INFINITY;
				continue;
			}
			distribution[i][0] = Math.log(initialState[i])
					+ Math.log(observation[i]);
		}

		// iteratively compute the distribution for the later observations
		for (int o = 1; o < sensorInput.length; o++) {
			// get the observation vector for the current observation
			observation = this.sensorModel.get(sensorInput[o]);
			for (int to = 0; to < num_of_states; to++) {
				double maxPossibility = Double.NEGATIVE_INFINITY;
				int maxIndex = -1;
				for (int from = 0; from < num_of_states; from++) {
					double transition = this.transitionMatrix[from][to];
					double p = transition == 0.0
							|| distribution[from][o - 1] == Double.NEGATIVE_INFINITY ? Double.NEGATIVE_INFINITY
							: distribution[from][o - 1] + Math.log(transition);
					if (p > maxPossibility) {
						maxPossibility = p;
						maxIndex = from;
					}
				}
				distribution[to][o] = maxPossibility == Double.NEGATIVE_INFINITY ? Double.NEGATIVE_INFINITY
						: maxPossibility + Math.log(observation[to]);
				vpath[to][o] = maxIndex;
			}
		}
		
		double max = Double.NEGATIVE_INFINITY;
		int row = 0;
		for (int i = 0; i < num_of_states; i++) {
			double p = distribution[i][num_of_steps - 1];
			if (p > max) {
				max = p;
				row = i;
			}
		}
		int[][] result = new int[2][num_of_steps + 1];
		result[0][num_of_steps] = row % this.width;
		result[1][num_of_steps] = row / this.width;

		// parse the result to a 2D array
		for (int i = num_of_steps - 1; i >= 0; i--) {
			result[0][i] = vpath[row][i] % this.width;
			result[1][i] = vpath[row][i] / this.width;
			row = vpath[row][i];
		}

		return result;
	}

	/**
	 * translate 2D coordinate to 1D coordinate
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	private int getFlattedCoordinate(int x, int y) {
		return y * width + x;
	}

	/**
	 * Distribution at a certain step
	 */
	class PathNode {
		private double[][] state;

		public PathNode(double[] arg) {
			this.state = Utils.to2DMatrix(arg, height, width);
		}

		public double[][] getState() {
			return this.state;
		}
	}
}
