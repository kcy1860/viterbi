package ai.prob.viterbi;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import javafx.scene.paint.Color;

public class Utils {
	/**
	 * apply vector = vector * matrix
	 * 
	 * @param vector
	 *            : 1*n vector
	 * @param matrix
	 *            : n*n matrix
	 */
	public static void multiply(double[] vector, double[][] matrix) {
		int length = vector.length;
		double[] result = new double[length];
		for (int k = 0; k < length; k++) {
			double current = 0.0;
			for (int j = 0; j < length; j++) {
				current += vector[j] * matrix[j][k];
			}
			result[k] = current;
		}
		for (int k = 0; k < length; k++) {
			vector[k] = result[k];
		}
	}

	/**
	 * @param vector1
	 *            = (a1, a2, a3 ... an)
	 * @param vector2
	 *            = (b1, b2, b3 ... bn)
	 * 
	 *            return vector = (a1*b1, a2*b2, ... an*bn)
	 */

	public static void multiply(double[] vector1, double[] vector2) {
		int length = vector1.length;
		for (int k = 0; k < length; k++) {
			vector1[k] = vector1[k] * vector2[k];
		}

	}

	/**
	 * normalize a vector
	 * 
	 * @param vector
	 */
	public static void normalize(double[] vector) {
		double total = 0.0;
		int h1 = vector.length;
		for (int i = 0; i < h1; i++) {
			total += vector[i];
		}
		for (int i = 0; i < h1; i++) {
			vector[i] = vector[i] / total;

		}
	}

	/**
	 * transform a 1D vector to a 2D matrix
	 * 
	 * @param vector
	 * @param height
	 * @param width
	 * @return
	 */
	public static double[][] to2DMatrix(double[] vector, int height, int width) {
		double[][] result = new double[height][width];
		int index = 0;
		for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {
				result[i][j] = vector[index++];
			}
		}
		return result;
	}

	/**
	 * transform a 2D double matrix to a string
	 * 
	 * @param matrix
	 * @return
	 */
	public static String matrix2String(double[][] matrix) {
		StringBuffer result = new StringBuffer();
		int height = matrix.length;
		int width = matrix[0].length;
		NumberFormat formatter = new DecimalFormat("#0.000  ");
		for (int i = height - 1; i >= 0; i--) {
			for (int j = 0; j < width; j++) {
				result.append(formatter.format(matrix[i][j]));
			}
			result.append("\n");
		}
		return result.toString();
	}

	/**
	 * input string shown on the board
	 * 
	 * @param input
	 * @return
	 */
	public static String buildInputString(char[] input, int i) {
		StringBuffer result = new StringBuffer();
		result.append("Input:\n");
		i = i-1;
		int k = 0;
		for (char c : input) {
			if(i==k){
				result.append(Character.toUpperCase(c));
			}else{
				result.append(c);
			}
			k++;		
			result.append(" ");
		}
		return result.toString();
	}

	/**
	 * used to animate the path
	 * 
	 * @param c
	 * @param possibility
	 * @return
	 */
	public static Color getColor(char c, double possibility) {
		possibility = possibility < 0.5 ? possibility * 2 : 1;
		switch (c) {
		case 'r':
			return Color.color(1.0, 0, 0, possibility);
		case 'g':
			return Color.color(0, 1.0, 0, possibility);
		case 'b':
			return Color.color(0, 0, 1.0, possibility);
		case 'y':
			return Color.color(1.0, 1.0, 0, possibility);
		default:
			return Color.LIGHTGREY;
		}
	}
}
