package quannk.test.excel;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("WeakerAccess")
public class Cell {
	@NotNull
	private final String cellName;
	@NotNull
	private final String value;
	private boolean isComputed;
	private double doubleValue = 0;

	public Cell(@NotNull String cellName, @NotNull String value) {
		Preconditions.checkArgument(isValidCellName(cellName), "Invalid cell position " + cellName);
		this.cellName = cellName;
		this.value = value;
		boolean isNumber;
		try {
			doubleValue = Double.parseDouble(value);
			isNumber = true;
		} catch (NumberFormatException e) {
			isNumber = false;
		}
		this.isComputed = isNumber;
	}

	public Cell(@NotNull String cellName) {
		this(cellName, "0");
	}

	public static boolean isValidCellName(String cellName) {
		return cellName.matches("[A-Z]+[1-9]+[0-9]*");
	}

	public boolean isComputed() {
		return isComputed;
	}

	@NotNull
	public String getCellName() {
		return cellName;
	}

	@NotNull
	public String getValue() {
		return value;
	}

	public double getDoubleValue() {
		if (isComputed) {
			return doubleValue;
		} else {
			throw new RuntimeException("Not computed");
		}
	}

	public void setDoubleValue(double value) {
		Preconditions.checkArgument(!isComputed);
		isComputed = true;
		doubleValue = value;
	}
}
