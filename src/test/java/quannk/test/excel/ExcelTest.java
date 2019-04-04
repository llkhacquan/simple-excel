package quannk.test.excel;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.util.Objects;

public class ExcelTest {

	private static final Logger LOG = LoggerFactory.getLogger(ExcelTest.class);

	private static void compareResult(String s1, String s2) {
		String[] ss1 = s1.split("\n");
		String[] ss2 = s2.split("\n");
		int n = Math.min(ss1.length, ss2.length);
		for (int i = 0; i < n; i++) {
			if (i % 2 == 0) {
				Assert.assertEquals(ss1[i], ss2[i]);
				Assert.assertTrue(Cell.isValidCellName(ss1[i]));
			} else {
				Assert.assertEquals(Double.parseDouble(ss1[i]), Double.parseDouble(ss2[i]), 0.00000000001);
			}
		}
	}

	/**
	 * read the data folder and do auto test
	 *
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