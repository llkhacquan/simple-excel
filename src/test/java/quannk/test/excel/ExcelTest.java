package quannk.test.excel;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.util.*;

public class ExcelTest {
	private static final Logger LOG = LoggerFactory.getLogger(ExcelTest.class);
	private static final double EPSILON = 0.00000000001;
	private final Random RANDOM = new Random();
	private int randomCellName = 1;

	private static void compareResult(String s1, String s2) {
		String[] ss1 = s1.split("\n");
		String[] ss2 = s2.split("\n");
		int n = Math.min(ss1.length, ss2.length);
		for (int i = 0; i < n; i++) {
			if (i % 2 == 0) {
				Assert.assertEquals(ss1[i], ss2[i]);
				Assert.assertTrue(Cell.isValidCellName(ss1[i]));
			} else {
				Assert.assertEquals(Double.parseDouble(ss1[i]), Double.parseDouble(ss2[i]), EPSILON);
			}
		}
	}

	/**
	 * random create test and test it
	 */
	@Test
	public void testRandom() throws IOException, Excel.CircularDependenciesException {
		for (int i = 0; i < 1000; i++) {
			List<Cell> autoTest = createAutoTest();
			StringBuilder sb = new StringBuilder();
			sb.append(autoTest.size()).append('\n');
			for (Cell cell : autoTest) {
				sb.append(cell.getCellName()).append('\n').append(cell.getValue()).append('\n');
			}

			Excel excel = new Excel(Excel.readInput(new ByteArrayInputStream(sb.toString().getBytes())));
			excel.compute();
			HashMap<String, Cell> data = excel.getData();
			for (Cell cell : autoTest) {
				Assert.assertEquals(cell.getDoubleValue(), data.get(cell.getCellName()).getDoubleValue(), EPSILON);
			}
		}
	}

	/**
	 * random create test with circular-dependency and test it
	 */
	@Test
	public void testRandomCircularDependency() throws IOException {
		for (int i = 0; i < 1000; i++) {
			List<Cell> autoTest = createAutoTest();
			autoTest.add(new Cell("B1", "B2 " + autoTest.get(0).getCellName() + " +"));
			autoTest.add(new Cell("B2", autoTest.get(0).getCellName() + " B1 +"));
			StringBuilder sb = new StringBuilder();
			sb.append(autoTest.size()).append('\n');
			for (Cell cell : autoTest) {
				sb.append(cell.getCellName()).append('\n').append(cell.getValue()).append('\n');
			}

			Excel excel = new Excel(Excel.readInput(new ByteArrayInputStream(sb.toString().getBytes())));

			try {
				excel.compute();
				Assert.fail("Should fail here");
			} catch (Excel.CircularDependenciesException e) {
				// ok, good, now check the cause of circular dependency
				if (e.getCellName1().equals("B1")) {
					Assert.assertEquals("B2", e.getCellName2());
				} else {
					Assert.assertEquals("B1", e.getCellName2());
					Assert.assertEquals("B2", e.getCellName1());
				}
			}
		}
	}

	private List<Cell> createAutoTest() {
		List<Cell> result = new ArrayList<>();
		final int n = RANDOM.nextInt(100) + 1;
		// create cell with value as number
		for (int i = 0; i < n; i++) {
			String name = randomCellName();
			Cell cell = new Cell(name, String.valueOf(RANDOM.nextDouble()));
			result.add(cell);
		}

		// create cells with value as formula
		for (int i = 0; i < n; i++) {
			String name = randomCellName();
			Cell cell1 = result.get(RANDOM.nextInt(result.size()));
			Cell cell2 = result.get(RANDOM.nextInt(result.size()));
			String value = cell1.getCellName() + " " + cell2.getCellName() + " +";
			Cell cell = new Cell(name, value);
			cell.setDoubleValue(cell1.getDoubleValue() + cell2.getDoubleValue());
			result.add(cell);
		}
		Collections.shuffle(result); // more fun
		return result;
	}

	private String randomCellName() {
		return "A" + randomCellName++;
	}

	/**
	 * read the data folder and do auto test
	 * <p>
	 * in.x contains input
	 * out.x (if exists) contains expected output; if out.x does not exist, it means there should be a CircularDependenciesException
	 */
	@Test
	public void testAuto() throws IOException {
		for (String inFileName : Objects.requireNonNull(new File("data").list((dir, name) -> name.matches("in.\\d+")))) {
			File inFile = new File("data", inFileName);
			File outFile = new File("data", inFileName.replace("in.", "out."));
			boolean circularDependency = !outFile.exists();
			Excel excel = new Excel(Excel.readInput(new FileInputStream(inFile)));
			try {
				excel.compute();
				if (circularDependency) {
					Assert.fail("Should not reach here");
				}
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				PrintStream ps = new PrintStream(baos);
				excel.print(false, ps);
				compareResult(new String(Files.readAllBytes(outFile.toPath())), baos.toString());
			} catch (Excel.CircularDependenciesException e) {
				if (circularDependency) {
					// ok, good
					System.out.println(e.getMessage());
				} else {
					// not good
					throw new RuntimeException(e);
				}
			}
			LOG.info("Done test with {}", inFile);
		}
	}
}