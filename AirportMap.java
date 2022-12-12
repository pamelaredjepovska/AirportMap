package airportModule;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.fhpotsdam.unfolding.UnfoldingMap;
import de.fhpotsdam.unfolding.data.Feature;
import de.fhpotsdam.unfolding.data.GeoJSONReader;
import de.fhpotsdam.unfolding.data.PointFeature;
import de.fhpotsdam.unfolding.data.ShapeFeature;
import de.fhpotsdam.unfolding.marker.Marker;
import de.fhpotsdam.unfolding.marker.SimpleLinesMarker;
import de.fhpotsdam.unfolding.marker.SimplePointMarker;
import de.fhpotsdam.unfolding.utils.MapUtils;
import module6.EarthquakeMarker;
import airportModule.CityMarker;
import airportModule.CommonMarker;
import de.fhpotsdam.unfolding.geo.Location;
import parsing.ParseFeed;
import processing.core.PApplet;

/** An applet that shows airports (and routes)
 * on a world map.  
 * @author Adam Setters and the UC San Diego Intermediate Software Development
 * MOOC team
 *
 */
public class AirportMap extends PApplet {
	
	UnfoldingMap map;
	
	private String airportImg = "airplaneIcon4.png";
	
	// Show airports with the this altitude as the minimum
	public static final int MIN_ALTIDUDE = 4000;
	
	// Show airports that are within this distance of a city (km)
	public static final int MIN_CITY_DISTANCE = 500;
	
	// Show cities that are within this distance of an airport (km)
	public static final int MIN_AIRPORT_DISTANCE = 1000;
	
	// You can ignore this.  It's to get rid of eclipse warnings
	private static final long serialVersionUID = 1L;
	
	// A marker for each airport
	private List<Marker> airportMarkers;
	
	// List of features for routes
	private List<ShapeFeature> routes;
	
	// A marker for each route
	List<Marker> routeList;

	// List of features for each airport
	private List<PointFeature> features;
	
	// The file containing city names and info
	private String cityFile = "citymap.geojson";
	
	// The file containing all the routes between airports
	private String routesFile = "routes.dat";
	
	// Marker for each city
	private List<Marker> cityMarkers;
	
	private CommonMarker lastSelected;
	private CommonMarker lastClicked;
	
	
	public void setup() {
		// setting up PAppler
		size(1300, 900, OPENGL);
		
		// setting up map and default events
		map = new UnfoldingMap(this, 120, 50, 1250, 850);
		MapUtils.createDefaultEventDispatcher(this, map);
		
		// Read in city map
		List<Feature> cities = GeoJSONReader.loadData(this, cityFile);
		cityMarkers = new ArrayList<Marker>();
		for (Feature city : cities) 
		{
			cityMarkers.add(new CityMarker(city));
			//System.out.println(city.getProperty("pop_max") + " " + city.getProperty("pop_max").getClass());
		}
		
		/* get features from airport data
		 * Location: (x,y)
		 * Properties: country, altitude, code, city, name
		 */
		features = ParseFeed.parseAirports(this, "airports.dat");
		/*for (PointFeature f : features)
		{
			System.out.println(f.getLocation() + " ," + f.getProperties());
		}*/
		
		// List for markers, hashmap for quicker access when matching with routes
		airportMarkers = new ArrayList<Marker>();
		HashMap<Integer, Location> airports = new HashMap<Integer, Location>();
		
		// Create markers from features
		/* Key: unique ID for key
		 * Value: location (x,y)
		 */
				
		for(PointFeature feature : features) 
		{
			// To limit the amount of markers on the map
			if (Integer.parseInt((String)feature.getProperty("altitude")) > MIN_ALTIDUDE)
			{
				//ImageMarker m = new ImageMarker(feature, loadImage(airportImg));
				
				// No ImageMarker
				airportMarkers.add(new AirportMarker(feature));
				
				// Put airport in hashmap with OpenFlights unique id for key
				airports.put(Integer.parseInt(feature.getId()), feature.getLocation());
			}
			//System.out.println(Integer.parseInt(feature.getId()) + " " + feature.getLocation());
			//break;
		
		}
		
		/*for (Integer k:airports.keySet())
		{
			System.out.println(k + " | " + airports.get(k));
			break;
		}*/
		
		
		// Parse route data
		/* Locations: (x1,y1),(x2,y2)
		 * Properties: destination, source
		 */
		routes = ParseFeed.parseRoutes(this, routesFile);
		routeList = new ArrayList<Marker>();
		for(ShapeFeature route : routes) {
			
			// Get source and destination airportIds
			int source = Integer.parseInt((String)route.getProperty("source"));
			int dest = Integer.parseInt((String)route.getProperty("destination"));
			
			// Get locations for airports on route
			if(airports.containsKey(source) && airports.containsKey(dest)) 
			{
				route.addLocation(airports.get(source));
				route.addLocation(airports.get(dest));
			}
			
			SimpleLinesMarker sl = new SimpleLinesMarker(route.getLocations(), route.getProperties());
		
			//System.out.println(sl.getProperties());
			
			//UNCOMMENT IF YOU WANT TO SEE ALL ROUTES
			routeList.add(sl);
		}
		
		/*for (ShapeFeature sf:routes)
		{
			System.out.println(sf.getLocations() + " | " + sf.getProperties());
		}*/
		
		
		
		//UNCOMMENT IF YOU WANT TO SEE ALL ROUTES
		map.addMarkers(routeList);
		hideRouteMarkers();
		map.addMarkers(cityMarkers);
		map.addMarkers(airportMarkers);
		
	}
	
	public void draw() {
		background(0);
		map.draw();
		addKey();
		
	}
	
	/** Event handler that gets called automatically when the 
	 * mouse moves.
	 */
	@Override
	public void mouseMoved()
	{
		// clear the last selection
		if (lastSelected != null) {
			lastSelected.setSelected(false);
			lastSelected = null;
		
		}
		selectMarkerIfHover(airportMarkers);
		selectMarkerIfHover(cityMarkers);
		//loop();
	}
	
	// If there is a marker selected 
	private void selectMarkerIfHover(List<Marker> markers)
	{
		// Abort if there's already a marker selected
		if (lastSelected != null) {
			return;
		}
		
		for (Marker m : markers) 
		{
			CommonMarker marker = (CommonMarker)m;
			if (marker.isInside(map,  mouseX, mouseY)) {
				lastSelected = marker;
				marker.setSelected(true);
				return;
			}
		}
	}
	
	/** The event handler for mouse clicks
	 * It will display an airport and its nearby cities and its routes
	 * Or if a city is clicked, it will display all the nearby airport 
	 */
	@Override
	public void mouseClicked()
	{
		if (lastClicked != null) 
		{
			hideRouteMarkers();
			unhideMarkers();
			lastClicked = null;
		}
		else if (lastClicked == null) 
		{
			checkAirportsForClick();
			if (lastClicked == null) 
			{
				checkCitiesForClick();
			}
		}
	}
	
	// Helper method that will check if a city marker was clicked on
	// and respond appropriately
	private void checkCitiesForClick()
	{
		if (lastClicked != null) return;
		
		// Loop over the city markers to see if one of them is selected
		for (Marker marker : cityMarkers) 
		{
			if (!marker.isHidden() && marker.isInside(map, mouseX, mouseY)) 
			{
				lastClicked = (CommonMarker)marker;
				// Hide all the other city markers that aren't clicked
				for (Marker mhide : cityMarkers) 
				{
					if (mhide != lastClicked) 
					{
						mhide.setHidden(true);
					}
				}
				
				for (Marker ahide : airportMarkers) 
				{
					AirportMarker airportMarker = (AirportMarker)ahide;
					/* If the distance between the city and the airport is larger 
					 * than the min distance, hide that airport marker
					 */
					if (airportMarker.getDistanceTo(marker.getLocation()) > MIN_CITY_DISTANCE) 
					{
						airportMarker.setHidden(true);
					}
				}
				
				return;
			}
		}		
	}
	
	// Helper method that will check if an airport marker was clicked on
	// and respond appropriately
	private void checkAirportsForClick()
	{
		if (lastClicked != null) return;
		
		// Loop over the airport markers to see if one of them is selected
		for (Marker m : airportMarkers) 
		{
			AirportMarker marker = (AirportMarker)m;
			
			if (!marker.isHidden() && marker.isInside(map, mouseX, mouseY)) 
			{
				lastClicked = marker;
				
				/* Show all the routes from the clicked airport marker
				 * and its destination markers
				 */
				List<Location> airportRoute = new ArrayList<Location>(); // List of destination locations of the clicked marker
				for (Marker route : routeList)
				{
					SimpleLinesMarker r = (SimpleLinesMarker)route;
					if (r.getLocations().contains(lastClicked.getLocation()))
					{
						route.setHidden(false);
						airportRoute.add(r.getLocation(1)); // Add the destination location to the list
					}
				}
				
				// Hide all the other airport markers that aren't clicked and aren't en route
				for (Marker mhide : airportMarkers) 
				{
					if (mhide != lastClicked && !airportRoute.contains(mhide.getLocation())) 
					{
						mhide.setHidden(true);
					}
				}
				
				for (Marker mhide : cityMarkers) 
				{
					/* If the distance between the airport and the city is larger
					 * than the min distance, hide that city marker
					 */
					if (mhide.getDistanceTo(marker.getLocation()) > MIN_AIRPORT_DISTANCE) 
					{
						mhide.setHidden(true);
					}
				}
				
				return;
			}
		}
	}
	
	// loop over and unhide all markers
	private void unhideMarkers() 
	{
		for(Marker marker : airportMarkers) 
		{
			marker.setHidden(false);
		}
			
		for(Marker marker : cityMarkers) 
		{
			marker.setHidden(false);
		}
	}
	
	// loop over and hide all route markers
	private void hideRouteMarkers() 
	{
		for(Marker marker : routeList) 
		{
			marker.setHidden(true);
		}
	}
	
	// helper method to draw key in GUI
	private void addKey() 
	{	
		// Remember you can use Processing's graphics methods here
		fill(255, 250, 240);
		
		int xbase = 25;
		int ybase = 50;
		
		rect(xbase, ybase, 150, 250);
		
		fill(0);
		textAlign(LEFT, CENTER);
		textSize(12);
		text("Airport Key", xbase+25, ybase+25);
		
		fill(150, 30, 30);
		int tri_xbase = xbase + 35;
		int tri_ybase = ybase + 50;
		triangle(tri_xbase, tri_ybase-CityMarker.TRI_SIZE, tri_xbase-CityMarker.TRI_SIZE, 
				tri_ybase+CityMarker.TRI_SIZE, tri_xbase+CityMarker.TRI_SIZE, 
				tri_ybase+CityMarker.TRI_SIZE);

		fill(0, 0, 0);
		textAlign(LEFT, CENTER);
		text("City Marker", tri_xbase + 15, tri_ybase);
		
		text("Airport", xbase+50, ybase+70);
		//text("Ocean Quake", xbase+50, ybase+90);
		text("Altitude: ", xbase+25, ybase+115);
		
		fill(255, 255, 255);
		ellipse(xbase+35, 
				ybase+70, 
				10, 
				10);
		//rect(xbase+35-5, ybase+90-5, 10, 10);
		
		fill(color(255, 0, 0));
		ellipse(xbase+35, ybase+140, 12, 12);
		fill(color(0, 0, 255));
		ellipse(xbase+35, ybase+160, 12, 12);
		fill(color(255, 255, 0));
		ellipse(xbase+35, ybase+180, 12, 12);
		
		textAlign(LEFT, CENTER);
		fill(0, 0, 0);
		text("< 5000", xbase+50, ybase+140);
		text("< 10 000", xbase+50, ybase+160);
		text("> 10 000", xbase+50, ybase+180);

		/*text("Past hour", xbase+50, ybase+200);
		
		fill(255, 255, 255);
		int centerx = xbase+35;
		int centery = ybase+200;
		ellipse(centerx, centery, 12, 12);

		strokeWeight(2);
		line(centerx-8, centery-8, centerx+8, centery+8);
		line(centerx-8, centery+8, centerx+8, centery-8);*/
		
		
	}

}
