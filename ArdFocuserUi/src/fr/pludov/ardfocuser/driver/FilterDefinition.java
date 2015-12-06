package fr.pludov.ardfocuser.driver;

import java.awt.Color;

public class FilterDefinition {
	String name;
	Color color;
	int position;
	
	public FilterDefinition() {
		position = 100;
		color = Color.RED;
		name = "rouge";
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Color getColor() {
		return color;
	}

	public void setColor(Color color) {
		this.color = color;
	}

	public int getPosition() {
		return position;
	}

	public void setPosition(int position) {
		this.position = position;
	}

}
