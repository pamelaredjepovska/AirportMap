package airportModule;

import de.fhpotsdam.unfolding.data.Feature;
import de.fhpotsdam.unfolding.data.PointFeature;
import de.fhpotsdam.unfolding.geo.Location;
import processing.core.PConstants;
import processing.core.PGraphics;

/**
 * Implements a visual marker for cities on an earthquake map
 * 
 * 
 */
public class CityMarker extends CommonMarker {

	public static int TRI_SIZE = 5; // The size of the triangle marker

	public CityMarker(Location location) {
		super(location);
	}

	public CityMarker(Feature city) {
		super(((PointFeature) city).getLocation(), city.getProperties());
		// Cities have properties: "name", "country" and "population"
	}

	// pg is the graphics object on which you call the graphics
	// methods. e.g. pg.fill(255, 0, 0) will set the color to red
	// x and y are the center of the object to draw.
	// They will be used to calculate the coordinates to pass
	// into any shape drawing methods.
	// e.g. pg.rect(x, y, 10, 10) will draw a 10x10 square
	// whose upper left corner is at position x, y
	/**
	 * Implementation of method to draw marker on the map.
	 */
	public void drawMarker(PGraphics pg, float x, float y) {
		// Save previous drawing style
		pg.pushStyle();

		// IMPLEMENT: drawing triangle for each city
		pg.fill(150, 30, 30);
		pg.triangle(x, y - TRI_SIZE, x - TRI_SIZE, y + TRI_SIZE, x + TRI_SIZE, y + TRI_SIZE);

		// Restore previous drawing style
		pg.popStyle();
	}

	/** Show the title of the city if this marker is selected */
	public void showTitle(PGraphics pg, float x, float y) {
		String name = "Loc: " + getCity() + ", " + getCountry() + " ";
		String pop = "Pop: " + getPopulation();

		pg.pushStyle();

		pg.fill(255, 255, 255);
		pg.textSize(12);
		pg.rectMode(PConstants.CORNER);
		pg.rect(x, y - TRI_SIZE - 39, Math.max(pg.textWidth(name), pg.textWidth(pop)) + 6, 39);
		pg.fill(0, 0, 0);
		pg.textAlign(PConstants.LEFT, PConstants.TOP);
		pg.text(name, x + 3, y - TRI_SIZE - 33);
		pg.text(pop, x + 3, y - TRI_SIZE - 18);

		pg.popStyle();
	}

	private String getCity() {
		try {
			return getStringProperty("name");
		} catch (Exception ex) {
			return "";
		}
	}

	private String getCountry() {
		try {
			return getStringProperty("sov0name");
		} catch (Exception ex) {
			return "";
		}

	}

	private Integer getPopulation() {
		try {
			return getIntegerProperty("pop_max");
		} catch (NumberFormatException ex) {
			return 0;
		}

	}
}
