package quannk.test.excel;

import org.junit.Assert;
import org.junit.Test;

public class CellTest {

	private static void checkOK(String cellPosition, String col, int row) {
		Cell cell = new Cell(cellPosition);
	}


	private static void checkFail(String cellPosition) {
		try {
			Cell cell = new Cell(cellPosition);
			Assert.fail("Should fail:" + cellPosition);
		} catch (IllegalArgumentException ignored) {

		}
	}

	@Test
	public void testParse() {
		checkOK("A123", "A", 123);
		checkOK("AN123", "AN", 123);
		checkOK("AZA10", "AZA", 10);

		checkFail("aA10");
		checkFail("123");
		checkFail("AB");
		checkFail("A0A");
		checkFail("AB0");
		checkFail(" AB0");
		checkFail("A0");
	}

}