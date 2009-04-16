/*
 * Constants
 */
var LPG = 4.54609188; // litres per gallon
var MPM = 1609.344; // meters per mile
var URBAN_AVERAGE = 12; // average speed in urban cycle
var EXTRA_URBAN_AVERAGE = 39; // average speed in extra urban cycle

/*
 * Global variables
 */
var directions;	// GDirections instance

/*
 * Initialisation
 */
function initialise() {
	var map = new GMap2(document.getElementById("dplus-map"));
    map.setCenter(new GLatLng(50.899994, -1.3999939), 6); // UK
    var routePanel = document.getElementById("dplus-route");
    directions = new GDirections(map, routePanel);
    
    // In order to access route information!
    GEvent.addListener(directions, "load", analyseRoute);    
    
    GEvent.addListener(directions, "error", handleErrors);
}

/*
 * Google Maps relevant 
 */
function handleErrors() {
	if (directions.getStatus().code == G_GEO_UNKNOWN_ADDRESS)
		alert("No corresponding geographic location could be found for one of the specified addresses. This may be due to the fact that the address is relatively new, or it may be incorrect.\nError code: "
				+ directions.getStatus().code);
	else if (directions.getStatus().code == G_GEO_SERVER_ERROR)
		alert("A geocoding or directions request could not be successfully processed, yet the exact reason for the failure is not known.\n Error code: "
				+ directions.getStatus().code);
	else if (directions.getStatus().code == G_GEO_MISSING_QUERY)
		alert("The HTTP q parameter was either missing or had no value. For geocoder requests, this means that an empty address was specified as input. For directions requests, this means that no query was specified in the input.\n Error code: "
				+ directions.getStatus().code);

	// else if (gdir.getStatus().code == G_UNAVAILABLE_ADDRESS) <--- Doc bug...
	// this is either not defined, or Doc is wrong
	// alert("The geocode for the given address or the route for the given
	// directions query cannot be returned due to legal or contractual
	// reasons.\n Error code: " + gdir.getStatus().code);

	else if (directions.getStatus().code == G_GEO_BAD_KEY)
		alert("The given key is either invalid or does not match the domain for which it was given. \n Error code: "
				+ directions.getStatus().code);
	else if (directions.getStatus().code == G_GEO_BAD_REQUEST)
		alert("A directions request could not be successfully parsed.\n Error code: "
				+ directions.getStatus().code);
	else
		alert("An unknown error occurred.");
}

/*
 * UI relevant
 */
function navigate() {
	var from = document.getElementById("dplus-input-from");
	var to = document.getElementById("dplus-input-to");
	var query = "from: " + from.value + " to: " + to.value;
//	alert(query);
	directions.load(query);
}

/*
 * Analyse route
 */
function analyseRoute() {
	var route = directions.getRoute(0);
	var fuel = 0; // fuel in litres
	
	for (var i = 0; i < route.getNumSteps(); i++) {
		var step = route.getStep(i);
		var meters = step.getDistance().meters;
		var seconds = step.getDuration().seconds;
		var speed = (meters / MPM) / (seconds / 3600);
		fuel += calcFuelConsumption(speed, meters);
	}
	
	var fuelCell = document.getElementById("dplus-info-fuel");
	fuelCell.innerHTML = fuel.toFixed(2);
	
	var cost = calcFuelCost(fuel);
	var costCell = document.getElementById("dplus-info-cost");
	costCell.innerHTML = cost.toFixed(2);
	
	document.getElementById("dplus-info").style.display = "block";
}
 
function calcFuelConsumption(speed, meters) {
	// mile per gallon
	var urban = 34.0; 
	var extra = 54.3;
	var average = 44.8;
	
	var d = meters / MPM;
	var f = 0;
	
	if (speed >= EXTRA_URBAN_AVERAGE) {
		f = d / extra;
	} else if (EXTRA_URBAN_AVERAGE - speed > speed - URBAN_AVERAGE) {
		f = d / average;
	} else {
		f = d / urban;
	}
	f *= LPG;
	return f;
}

function calcFuelCost(fuel) {
	return fuel * 0.919;
}

