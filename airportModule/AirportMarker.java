package airportModule;

import java.util.List;

import de.fhpotsdam.unfolding.data.Feature;
import de.fhpotsdam.unfolding.data.PointFeature;
import de.fhpotsdam.unfolding.marker.SimpleLinesMarker;
import processing.core.PConstants;
import processing.core.PGraphics;

/** 
 * A class to represent AirportMarkers on a world map.
 *   
 *
 */
public class AirportMarker extends CommonMarker {
	public static List<SimpleLinesMarker> routes;
	
	public static int TRI_SIZE = 5;  
	public static final int MIN_ALTIDUDE = 5000;
	public static final int MAX_ALTIDUDE = 10000;
	protected float radius; // The size of the marker
	
	/* Location: (x,y)
	 * Properties: country, altitude, code, city, name
	 */
	public AirportMarker(Feature airport) {
		super(((PointFeature)airport).getLocation(), airport.getProperties());
	
	}
	
	@Override
	public void drawMarker(PGraphics pg, float x, float y) 
	{
		// Determine the color of the marker based on the altitude
		colorDetermine(pg);
		pg.ellipse(x, y, radius, radius);
	}

	@Override
	public void showTitle(PGraphics pg, float x, float y) 
	{
		// Show rectangle with title
		String name = "Name: " + getName();
		String loc = "Loc: " + getCountry() + ", " + getCity();
		
		pg.pushStyle();
		
		pg.fill(255, 255, 255);
		pg.textSize(12);
		pg.rectMode(PConstants.CORNER);
		pg.rect(x, y-TRI_SIZE-39, Math.max(pg.textWidth(name), pg.textWidth(loc)) + 6, 39);
		pg.fill(0, 0, 0);
		pg.textAlign(PConstants.LEFT, PConstants.TOP);
		pg.text(name, x+3, y-TRI_SIZE-33);
		pg.text(loc, x+3, y - TRI_SIZE -18);
		
		pg.popStyle();
		
		// Show routes
	}
	
	// Determine color and raidus of marker from altitude
	// We use: < 5000 = red, < 10 000 = blue, > 10 000 = yellow
	private void colorDetermine(PGraphics pg) 
	{
		float altitude = getAltitude();
		
		if (altitude < MIN_ALTIDUDE) 
		{
			pg.fill(255, 0, 0);
			this.radius = 8;
		}
		else if (altitude < MAX_ALTIDUDE) 
		{
			pg.fill(0, 0, 255);
			this.radius = 10;
		}
		else 
		{
			pg.fill(255, 255, 0);
			this.radius = 12;
		}
	}
	
	/* Local getters for some airport properties.  
	 */
	public String getCountry()
	{
		return getStringProperty("country").replaceAll("\"", "");
	}
	
	public String getCity()
	{
		return getStringProperty("city").replaceAll("\"", "");
	}
	
	public String getName()
	{
		return getStringProperty("name").replaceAll("\"", "");
	}
	
	public int getAltitude()
	{
		return Integer.parseInt(getStringProperty("altitude"));
	}
	
	public int getCode()
	{
		return Integer.parseInt(getStringProperty("code"));
	}
	
}
