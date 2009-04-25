/*
 * Constants
 */
var LPG = 4.54609188; // litres per gallon
var MPM = 1609.344; // meters per mile
var URBAN_AVERAGE = 12; // average speed in urban cycle
var EXTRA_URBAN_AVERAGE = 39; // average speed in extra urban cycle

/*
 * Preference
 * Car description
 * MPG (mile per gallon): urban, extra urban, combined
 * Fuel price in pence
 * Default values are for a 2009 Volkswagen Polo Match 1.4 80hp.
 */
var preference = {
		owner:				"Piggy",
		car_desc:			"2009 Volkswagen Polo Match 1.4 80hp",
		urban_mpg:			34.0,
		extra_urban_mpg:	54.3,
		combined_mpg:		44.8,
		fuel_price:			92.9
}

/*
 * Global variables
 */
var directions;	// GDirections instance
var localSearch; // Google AJAX search

/*
 * Initialisation and termination
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
    localSearch.setCenterPoint("London, UK");
    
//    alert(document.cookie);
    loadPref();
    
    updateUIPref();
    
    initHelpDialog();
    
    initCarDialog();
}

function terminate() {
	savePref();
}

/*
 * Google Maps 
 */
function navigate() {
	var from = document.getElementById("dplus-input-from");
	var to = document.getElementById("dplus-input-to");
	
	if (from.value == "" || to.value == "") {
		alert("Neither \"from\" nor \"to\" can be empty!");
		return;
	}
	
	// first use localSearch to locate the (latitude, longitude) of from and to,
	// then find the route between them.
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
 * Reduce and increase fuel price
 */
function reducePrice() {
	updatePrice(-1.0);
}

function increasePrice() {
	updatePrice(1.0);
}

function updatePrice(inc) {
	preference.fuel_price = parseFloat(preference.fuel_price) + inc;
	document.getElementById("dplus-input-price").value = preference.fuel_price;	
}

/*
 * The number of times travelling along the route
 */
var savedTripNum;
var singleTripElement;
var tripNumElement;

function initTripNumChoice() {
	if (singleTripElement == undefined)
		singleTripElement = document.getElementById("dplus-info-single");
	
	if (tripNumElement == undefined)
		tripNumElement = document.getElementById("dplus-info-tripnum");

	savedTripNum = 1;
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
	var last = savedTripNum;
	var ratio = current / last;
	savedTripNum = current;
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
	var gallons = calcGallon1(speed, miles);
	return gallons * LPG;
}

// Fuel consumption A1
function calcGallon1(speed, miles) {
	var g;
	if (speed >= EXTRA_URBAN_AVERAGE) {
		g = miles / preference.extra_urban_mpg;
	} else if (EXTRA_URBAN_AVERAGE - speed > speed - URBAN_AVERAGE) {
		g = miles / preference.combined_mpg;
	} else {
		g = miles / preference.urban_mpg;
	}
	return g;
}

//Fuel consumption A2
function calcGallon2(speed, miles) {
	var g;
	if (speed >= EXTRA_URBAN_AVERAGE) {
		g = miles / preference.extra_urban_mpg;
	} else {
		g = miles / preference.urban_mpg;
	}
	return g;
}

function calcFuelCost(fuel) {
	var p = preference.fuel_price;
	return fuel * (p / 100);
}

/*
 * Preference management based on cookie
 */
function loadPref() {
	var pref = YAHOO.util.Cookie.getSubs("dplus");
	if (pref != null) {
		preference = pref;
	}
}

function savePref() {
	YAHOO.util.Cookie.setSubs("dplus", preference, {expires: new Date("January 1, 2025")}); 
}

function updateUIPref() {
	document.getElementById("dplus-pref-title").innerHTML = preference.owner + "'s Direction+";
	document.getElementById("dplus-pref-car").innerHTML = preference.owner + " is driving a " + preference.car_desc + ".";
    document.getElementById("dplus-input-price").value = preference.fuel_price;
}

/*
 * Help dialog
 */
var helpDialog;

function initHelpDialog() {
	helpDialog = new YAHOO.widget.Dialog("dplus-dialog-help", 
			{ width : "60em",
			  visible : false,
			  fixedcenter: true,
			  modal: true,
			  constraintoviewport : true
			});
	helpDialog.render();
}

function help() {
	helpDialog.show();	
}

/*
 * Change car dialog
 */
var carDialog;

function initCarDialog() {
	carDialog = new YAHOO.widget.Dialog("dplus-dialog-car", 
			{ width : "24em",
			  visible : false,
			  fixedcenter: true,
			  modal: true,
			  constraintoviewport : true
			});
	
	carDialog.render();
}

function changeCar() {
	document.getElementById("dplus-car-owner").value = preference.owner;
	document.getElementById("dplus-car-desc").value = preference.car_desc;
	document.getElementById("dplus-car-urban").value = preference.urban_mpg;
	document.getElementById("dplus-car-extra").value = preference.extra_urban_mpg;
	document.getElementById("dplus-car-combined").value = preference.combined_mpg;
	carDialog.show();
}

function saveCarDialog() {
	// Check if MPGs are numbers.
	var data = carDialog.getData();
	if (isNaN(data.urban_mpg) || isNaN(data.extra_urban_mpg) || isNaN(data.combined_mpg)) {
		alert("MPGs must be numbers.");
		return;
	}
	
	preference.owner = document.getElementById("dplus-car-owner").value;
	preference.car_desc = document.getElementById("dplus-car-desc").value;
	preference.urban_mpg = document.getElementById("dplus-car-urban").value;
	preference.extra_urban_mpg = document.getElementById("dplus-car-extra").value;
	preference.combined_mpg = document.getElementById("dplus-car-combined").value;
	closeCarDialog();
	updateUIPref();
}

function closeCarDialog() {
	carDialog.hide();
}
