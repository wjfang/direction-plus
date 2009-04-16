/*
 * Constants
 */
var LPG = 4.54609188; // litres per gallon
var MPM = 1609.344; // meters per mile
var URBAN_AVERAGE = 12; // average speed in urban cycle
var EXTRA_URBAN_AVERAGE = 39; // average speed in extra urban cycle

/*
 * MPG (mile per gallon) for 2009 Volkswagen Polo Match 1.4 80hp
 */
var URBAN_MPG = 34.0; 
var EXTRA_URBAN_MPG = 54.3;
var COMBINED_MPG = 44.8;

/*
 * Global variables
 */
var directions;	// GDirections instance
var localSearch; // Google AJAX search

/*
 * Google Maps 
 */
function initialise() {
	var map = new GMap2(document.getElementById("dplus-map"));
    map.setCenter(new GLatLng(53.0, -1.4), 6); // UK
    map.enableScrollWheelZoom();
    var routePanel = document.getElementById("dplus-route");
    directions = new GDirections(map, routePanel);
    
    // In order to access route information!
    GEvent.addListener(directions, "load", analyseRoute);    
    
    GEvent.addListener(directions, "error", handleErrors);
    
    localSearch = new GlocalSearch();
}

function navigate() {
	var from = document.getElementById("dplus-input-from");
	var to = document.getElementById("dplus-input-to");
	
	if (from.value == "" || to.value == "") {
		alert("Neither \"from\" nor \"to\" can be empty!");
		return;
	}
	
	if (!document.getElementById("dplus-input-postcode").checked) {
		var query = "from: " + from.value + " to: " + to.value;
		directions.load(query);
		return;
	}
	
	// Now we assume both from and to are uk postcode. TODO: need to check.
	localSearch.setSearchCompleteCallback(null, function() {
		if (localSearch.results[0]) {
			var resultLat = localSearch.results[0].lat;
			var resultLng = localSearch.results[0].lng;
			var fromPoint = new GLatLng(resultLat, resultLng);
//			alert(fromPoint);
			
			// deal with "to"
			localSearch.setSearchCompleteCallback(null, function() {
				if (localSearch.results[0]) {
					var resultLat = localSearch.results[0].lat;
					var resultLng = localSearch.results[0].lng;
					var toPoint = new GLatLng(resultLat, resultLng);
//					alert(toPoint);
					
					var query = "from: " + fromPoint + " to: " + toPoint;
					directions.load(query);
					
				} else {
					alert("To postcode not found!");
				}
			});

			localSearch.execute(to.value);
			
		} else {
			alert("From postcode not found!");
		}
	});

	localSearch.execute(from.value);
}

function handleErrors() {
	if (directions.getStatus().code == G_GEO_UNKNOWN_ADDRESS)
		alert("No corresponding geographic location could be found for one of the specified addresses. " +
				"This may be due to the fact that the address is relatively new, or it may be incorrect.\nError code: "
				+ directions.getStatus().code);
	else if (directions.getStatus().code == G_GEO_SERVER_ERROR)
		alert("A geocoding or directions request could not be successfully processed, " +
				"yet the exact reason for the failure is not known.\n Error code: "
				+ directions.getStatus().code);
	else if (directions.getStatus().code == G_GEO_MISSING_QUERY)
		alert("The HTTP q parameter was either missing or had no value. For geocoder requests, " +
				"this means that an empty address was specified as input. For directions requests, " +
				"this means that no query was specified in the input.\n Error code: "
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
 * The number of times travelling along the route
 */
var lastTripElement;
var singleTripElement;
var tripNumElement;

function initTripNumChoice() {
	if (lastTripElement == undefined)
		lastTripElement = document.getElementById("dplus-info-lasttrip");
	
	if (singleTripElement == undefined)
		singleTripElement = document.getElementById("dplus-info-single");
	
	if (tripNumElement == undefined)
		tripNumElement = document.getElementById("dplus-info-tripnum");

	lastTripElement.value = 1;
	singleTripElement.checked = true;
	tripNumElement.value = 1;
	tripNumElement.disabled = true;
}

function onSingleTrip() {
	updateTripNum(1);
}

function onReturnTrip() {
	updateTripNum(2);
}

function onCustomTrip() {
	updateTripNum(tripNumElement.value);
	tripNumElement.disabled = false;
}

function onTripNum() {
	updateTripNum(tripNumElement.value);
}

function updateTripNum(current) {
	var last = lastTripElement.value;
	var ratio = current / last;
	lastTripElement.value = current;
	updateFuel(ratio);
	tripNumElement.disabled = true;
}

function updateFuel(ratio) {
	var fuelCell = document.getElementById("dplus-info-fuel");
	var costCell = document.getElementById("dplus-info-cost");
	
	fuelCell.innerHTML = (fuelCell.innerHTML * ratio).toFixed(2);
	costCell.innerHTML = (costCell.innerHTML * ratio).toFixed(2);
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
	initTripNumChoice();
}

function calcFuelConsumption(speed, meters) {
	var miles = meters / MPM; 
	var gallons = calcGallon2(speed, miles);
	return gallons * LPG;
}

// Fuel consumption A1
function calcGallon1(speed, miles) {
	var g;
	if (speed >= EXTRA_URBAN_AVERAGE) {
		g = miles / EXTRA_URBAN_MPG;
	} else if (EXTRA_URBAN_AVERAGE - speed > speed - URBAN_AVERAGE) {
		g = miles / COMBINED_MPG;
	} else {
		g = miles / URBAN_MPG;
	}
	return g;
}

//Fuel consumption A2
function calcGallon2(speed, miles) {
	var g;
	if (speed >= EXTRA_URBAN_AVERAGE) {
		g = miles / EXTRA_URBAN_MPG;
	} else {
		g = miles / URBAN_MPG;
	}
	return g;
}

function calcFuelCost(fuel) {
	var p = document.getElementById("dplus-input-price").value;
	return fuel * (p / 100);
}
