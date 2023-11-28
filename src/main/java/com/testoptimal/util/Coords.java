package com.testoptimal.util;

public class Coords {
	public int x;
	public int y;
	public Coords(int x_p, int y_p) {
		this.x = x_p;
		this.y = y_p;
	}

	public Coords(String coords_p) {
		if (StringUtil.isEmpty(coords_p)) return;

		coords_p = coords_p.trim();
		if (coords_p.startsWith("Bounds=")) {
			coords_p = coords_p.substring(7);
		}
		if (coords_p.startsWith("(")) {
			coords_p = coords_p.substring(1, coords_p.length()-1);
		}
		String [] coordsList = coords_p.split(",");
		if (coordsList.length>0) {
			this.x = Integer.parseInt(coordsList[0].trim());
		}
		if (coordsList.length>1) {
			this.y = Integer.parseInt(coordsList[1].trim());
		}
	}
	public String toString() {
		return this.x + "," + this.y;
	}
}
