package by.gravity.doublexplayer.model;

public enum Rate {

	X1("1X", 1),
	X2("2X", 0.5),
	X4("4X", 0.25),
	X8("8X", 0.12);

	private final String name;

	private final double value;

	Rate(String name, double value) {

		this.name = name;
		this.value = value;
	}

	public String getName() {

		return name;
	}

	public double getValue() {

		return value;
	}

	public Rate getNext(Rate rate) {

		switch (rate) {
		case X1:
			return X2;
		case X2:
			return X4;
		case X4:
			return X8;
		case X8:
			return X1;
		default:
			break;
		}

		return X1;
	}
}
