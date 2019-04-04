package quannk.test.excel;

import com.google.common.base.Preconditions;

import java.io.*;
import java.util.*;

public final class Excel {

	private final Set<String> computingCell = new HashSet<>();
	private final Set<String> cellNeedComputation = new HashSet<>();
	private final Map<String, Cell> name2cellMap;

	public Excel(Map<String, Cell> data) {
		name2cellMap = Collections.unmodifiableMap(new HashMap<>(data));
		// add not-computed cell to cellNeedComputation
		for (Map.Entry<String, Cell> e : data.entrySet()) {
			String key = e.getKey();
			if (!e.getValue().isComputed()) {
				cellNeedComputation.add(key);
			}
		}
	}

	/**
	 * Run the application as said in the problem; if a file is provided in the argument, read data from file
	 */
	public static void main(String[] args) throws IOException {
		InputStream in = System.in;
		if (args.length > 0) {
			in = new FileInputStream(new File(args[0]));
		}
		Excel excel = new Excel(readInput(in));
		try {
			excel.compute();
			excel.print(false, System.out);
		} catch (CircularDependencies e) {
			System.out.println(e.getMessage());
		}
	}

	/**
	 * read input
	 */
	public static Map<String, Cell> readInput(InputStream data) throws IOException {
		Map<String, Cell> result = new HashMap<>();
		int line = 0;
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(data))) {
			line++;
			int n = Integer.parseInt(reader.readLine());
			for (int i = 0; i < n; i++) {
				line++;
				String cellPos = reader.readLine().toUpperCase(); // uppercase is for "just in case"
				line++;
				String cellValue = reader.readLine();
				result.put(cellPos.toUpperCase(), new Cell(cellPos, cellValue));
			}
			return result;
		} catch (NumberFormatException e) {
			throw new RuntimeException("Error at line " + line, e);
		}
	}

	private static double computeTrivialOperator(double d1, double d2, String operator) {
		switch (operator) {
			case "+":
				return d1 + d2;
			case "-":
				return d1 - d2;
			case "*":
				return d1 * d2;
			case "/":
				return d1 / d2;
			default:
				throw new RuntimeException("Not a valid operator:" + operator);
		}
	}

	public void print(boolean niceView, PrintStream out) {
		name2cellMap.values().stream().sorted(Comparator.comparing(Cell::getCellName)).forEach(cell -> {
			if (niceView) {
				out.println(cell.getCellName() + " => " + cell.getDoubleValue());
			} else {
				out.println(cell.getCellName() + "\n" + cell.getDoubleValue());
			}
		});
	}

	/**
	 * compute all cell in the data
	 *
	 * @throws CircularDependencies if something wrong
	 */
	public void compute() throws CircularDependencies {
		while (!cellNeedComputation.isEmpty()) {
			String nextCell = cellNeedComputation.stream().findAny().get();
			Preconditions.checkArgument(!name2cellMap.get(nextCell).isComputed());
			name2cellMap.get(nextCell).setDoubleValue(compute(nextCell));
		}
	}

	private double compute(String cellName) throws CircularDependencies {
		Cell cell = name2cellMap.get(cellName);
		if (cell == null) {
			// TODO we can just return 0 here, but the problem does not say it clearly, so better to notify user
			throw new RuntimeException("Data does not have this cell");
		}
		if (name2cellMap.get(cellName).isComputed()) {
			return cell.getDoubleValue();
		}

		// not computed
		if (computingCell.contains(cellName)) {
			throw new CircularDependencies(cellName, null);
		}

		Preconditions.checkArgument(cellNeedComputation.remove(cellName));
		Preconditions.checkArgument(computingCell.add(cellName));

		String expression = name2cellMap.get(cellName).getValue();
		Stack<Double> stack = new Stack<>();
		for (String s : expression.split(" ")) {
			Double number;
			if (isOperator(s)) { // an operator
				Double d1 = stack.pop();
				Double d2 = stack.pop();
				Preconditions.checkNotNull(d1, "Something wrong with expression: " + expression);
				Preconditions.checkNotNull(d2, "Something wrong with expression: " + expression);
				stack.push(computeTrivialOperator(d1, d2, s));
			} else if ((number = getDoubleIfAvailable(s)) != null) { // a pure number
				stack.push(number);
			} else { // a cell
				Preconditions.checkState(Cell.isValidCellName(s), "Not a valid cell name:" + s);
				try {
					stack.push(compute(s));
				} catch (CircularDependencies e) {
					throw new CircularDependencies(e.cellName1, s);
				}
			}
		}
		Preconditions.checkState(stack.size() == 1, "Something wrong with expression: " + expression);
		return stack.pop();
	}

	private boolean isOperator(String s) {
		return s.equals("+") ||
				s.equals("-") ||
				s.equals("*") ||
				s.equals("/");
	}

	private Double getDoubleIfAvailable(String s) {
		try {
			return Double.parseDouble(s);
		} catch (NumberFormatException e) {
			return null;
		}
	}

	public static class CircularDependencies extends Exception {
		String cellName1;
		String cellName2;

		CircularDependencies(String cellName1, String cellName2) {
			this.cellName1 = cellName1;
			this.cellName2 = cellName2;
		}

		@Override
		public String getMessage() {
			return "Circular dependency between " + cellName1 + " and " + cellName2 + " detected";
		}
	}
}
