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
function Preference(config) {
	this.data = {
		owner:					"Piggy",
		car_desc:				"2009 Volkswagen Polo Match 1.4 80hp",
		urban_mpg:				new Number(34.0),
		extra_urban_mpg: 		new Number(54.3),
		combined_mpg: 			new Number(44.8),
		fuel_price:				new Number(92.9)
	};
	
	this.ownerElement = document.getElementById(config.prefOwnerId);
	this.descElement = document.getElementById(config.prefDescId);
	this.urbanElement = document.getElementById(config.prefUrbanId);
	this.extraElement = document.getElementById(config.prefExtraId);
	this.combinedElement = document.getElementById(config.prefCombinedId);
	this.titleElement = document.getElementById(config.titleId);
	this.carElement = document.getElementById(config.carId);
	
	this.load();
	
	this.updateUI();
	
	this.dialog = new YAHOO.widget.Dialog(config.prefDialogId, 
			{ width : "24em",
			  visible : false,
			  fixedcenter: true,
			  modal: true,
			  constraintoviewport : true
			});
	
	this.dialog.render();
}

Preference.prototype.load = function() {
	var pref = YAHOO.util.Cookie.getSubs("dplus");
	if (pref != null) {
		this.data = pref;
	}
}

Preference.prototype.save = function() {
	YAHOO.util.Cookie.setSubs("dplus", this.data, {expires: new Date("January 1, 2025")}); 
}

Preference.prototype.updateUI = function() {
	this.titleElement.innerHTML = this.data.owner + "'s Direction+";
	this.carElement.innerHTML = this.data.owner + " is driving a " + this.data.car_desc + ".";
}

Preference.prototype.openDialog = function() {
	this.ownerElement.value = this.data.owner;
	this.descElement.value = this.data.car_desc;
	this.urbanElement.value = this.data.urban_mpg;
	this.extraElement.value = this.data.extra_urban_mpg;
	this.combinedElement.value = this.data.combined_mpg;
	this.dialog.show();
}

Preference.prototype.saveDialog = function() {
	// Check if MPGs are numbers.
	var data = this.dialog.getData();
	if (isNaN(data.urban_mpg) || isNaN(data.extra_urban_mpg) || isNaN(data.combined_mpg)) {
		alert("MPGs must be numbers.");
		return;
	}
	
	this.data.owner = data.owner;
	this.data.car_desc = data.car_desc;
	this.data.urban_mpg = data.urban_mpg;
	this.data.extra_urban_mpg = data.extra_urban_mpg;
	this.data.combined_mpg = data.combined_mpg;
	this.closeDialog();
	this.updateUI();
}

Preference.prototype.closeDialog = function() {
	this.dialog.hide();
}

/*
* Help dialog
*/
function Help(helpDialogId) {
	this.dialog = new YAHOO.widget.Dialog(helpDialogId, 
			{ width : "50em",
			  visible : false,
			  fixedcenter: true,
			  modal: true,
			  constraintoviewport : true
			});
	this.dialog.render();
}

Help.prototype.open = function() {
	this.dialog.show();	
}

/*
 * Price
 */
function Price(priceId, getPriceFunc, setPriceFunc) {
	this.priceElement = document.getElementById(priceId);
	this.getPrice = getPriceFunc;
	this.setPrice = setPriceFunc;
	this.updateUI(this.getPrice());
}

Price.prototype.minus1p = function() {
	this.update(-1.0);
}

Price.prototype.add1p = function() {
	this.update(1.0);
}

Price.prototype.update = function(inc) {
	var p = parseFloat(this.getPrice()) + inc;
	this.setPrice(p);
	this.updateUI(p);	
}

Price.prototype.updateUI = function(p) {
	this.priceElement.value = p;
}

/*
 * Info
 */
function Info(config) {
	this.savedTripNum = 1;
	this.singleTripElement = document.getElementById(config.singleTripId);
	this.tripNumElement = document.getElementById(config.tripNumId);
	this.tripSetElement = document.getElementById(config.tripSetId);
	this.fuelElement = document.getElementById(config.fuelId);
	this.costElement = document.getElementById(config.costId);
	this.infoElement = document.getElementById(config.infoId);
	this.cost = new Number;
	this.fuel = new Number;
}

Info.prototype.reset = function() {
	this.infoElement.style.display = "block";
	this.savedTripNum = 1;
	this.singleTripElement.checked = true;
	this.tripNumElement.value = 1;
	this.tripNumElement.disabled = true;
	this.tripSetElement.disabled = true;
}

Info.prototype.onSingleTrip = function() {
	this.updateTripNum(1);
	this.tripNumElement.disabled = true;
	this.tripSetElement.disabled = true;
}

Info.prototype.onReturnTrip = function() {
	this.updateTripNum(2);
	this.tripNumElement.disabled = true;
	this.tripSetElement.disabled = true;
}

Info.prototype.onCustomTrip = function() {
	this.updateTripNum(this.tripNumElement.value);
	this.tripNumElement.disabled = false;
	this.tripSetElement.disabled = false;
}

Info.prototype.onTripNum = function() {
	this.updateTripNum(this.tripNumElement.value);
}

Info.prototype.updateTripNum = function(current) {
	var last = this.savedTripNum;
	var ratio = current / last;
	this.savedTripNum = current;
	this.updateFuel(ratio);
}

Info.prototype.updateFuel = function(ratio) {
	this.cost *= ratio;
	this.fuel *= ratio;
	this.updateUI();
}

Info.prototype.updateUI = function() {
	this.fuelElement.innerHTML = this.fuel.toFixed(2);
	this.costElement.innerHTML = this.cost.toFixed(2);	
}

/*
 * BBC Travel News Database Query Facility
 */
function TravelNewsDatabase(map) {
	this.endpoint = "./travelNewsDBQuery";
	this.map = map; // Google map
	this.baseIcon = this.createBaseIcon();
}

TravelNewsDatabase.prototype.createBaseIcon = function() {
	var icon = new GIcon();
	icon.shadow = "http://www.google.com/mapfiles/shadow50.png";
	icon.iconSize = new GSize(20, 34);
	icon.shadowSize = new GSize(37, 34);
	icon.iconAnchor = new GPoint(9, 34);
	icon.infoWindowAnchor = new GPoint(9, 2);
	icon.infoShadowAnchor = new GPoint(18, 25);
	return icon;
}

TravelNewsDatabase.prototype.success = function(response) {
	var newsarray = YAHOO.lang.JSON.parse(response.responseText);
	for (var i = 0; i < newsarray.length; i++) {
		var news = newsarray[i];
		this.createMarker(news);
	}
}

TravelNewsDatabase.prototype.createMarker = function(news) {
	var point = new GLatLng(news.latitude, news.longitude);

	// Set up our GMarkerOptions object
	var markerOptions = {};

	var marker = new GMarker(point, markerOptions);

	GEvent.addListener(marker, "click", function() {
		marker.openInfoWindowHtml("<p><b>" + news.title + "</b></p>" +
				"<p>" + news.description + "</p>" +
				"<p><a href=\"" + news.link + "\" target=\"_blank\">More Information</a></p>",
				{maxWidth: 360});
	});
	this.map.addOverlay(marker, news.title);
}

TravelNewsDatabase.prototype.failure = function(response) {
	alert(response.responseText);
}

TravelNewsDatabase.prototype.query = function(route) {
	var points = new Array();
	for (var i = 0; i < route.getNumSteps(); i++) {
		var s = route.getStep(i);
		var p = s.getLatLng();
		points[points.length] = [p.lat(), p.lng()];
	}
	var e = route.getEndLatLng();
	points[points.length] = [e.lat(), e.lng()];
	var request = YAHOO.lang.JSON.stringify(points); 
	YAHOO.util.Connect.asyncRequest('POST', this.endpoint, this, request); 
}

/*
 * DPlus
 */
function DPlus(config) {
	// create a GDirections instance
	var map = new GMap2(document.getElementById(config.mapId));
    map.setCenter(new GLatLng(53.0, -1.4), 6); // UK
    map.enableScrollWheelZoom();
    map.addControl(new GLargeMapControl());
	map.addControl(new GMapTypeControl());
	
    var routeElement = document.getElementById(config.routeId);
    this.directions = new GDirections(map, routeElement);
    
    // In order to access route information, need to add listener for event.
    // Use closure to create callback triggered by event
    var createCallBack = function(obj, method) {
    	return function() {
    		method.call(obj);
    	};
    };
    
    // Another way to do this is to use GEvent.callback(...) 
    // var cb = GEvent.callback(this, this.analyseRoute);
    
    // Registers an event handler for a custom event on the source object. 
    // Returns a handle that can be used to eventually deregister the handler. 
    // The event handler will be called with this set to the source object.
    GEvent.addListener(this.directions, "load", createCallBack(this, this.analyseRoute));
    // cb = GEvent.callback(this, this.handleErrors);
    GEvent.addListener(this.directions, "error", createCallBack(this, this.handleErrors));
    
    // create a GlocalSearch instance
    this.localSearch = new GlocalSearch();
    this.localSearch.setCenterPoint("London, UK");
    
    // preference
    this.preference = new Preference(config.pref);
    
    // help
    this.help = new Help(config.helpDialogId);
    
    // price
    var prefdata = this.preference.data;
    this.price = new Price(config.priceId,
    		function() {
    			return prefdata.fuel_price;
    		},
    		function(p) {
    			prefdata.fuel_price = p;
    		});
    
    // info
    this.info = new Info(config.info);
    
    // travel news database
    this.travelNewsDatabase = new TravelNewsDatabase(map);
    
    // main
    this.from = document.getElementById(config.fromId);
	this.to = document.getElementById(config.toId);	
}


DPlus.prototype.destroy = function() {
	this.preference.save();
}

DPlus.prototype.handleErrors = function() {
	var code = this.directions.getStatus().code;
	if (code == G_GEO_UNKNOWN_ADDRESS)
		alert("No corresponding geographic location could be found for one of the specified addresses. " +
				"This may be due to the fact that the address is relatively new, or it may be incorrect.\nError code: "
				+ code);
	else if (code == G_GEO_SERVER_ERROR)
		alert("A geocoding or directions request could not be successfully processed, " +
				"yet the exact reason for the failure is not known.\n Error code: "
				+ code);
	else if (code == G_GEO_MISSING_QUERY)
		alert("The HTTP q parameter was either missing or had no value. For geocoder requests, " +
				"this means that an empty address was specified as input. For directions requests, " +
				"this means that no query was specified in the input.\n Error code: "
				+ code);

	// else if (gdir.getStatus().code == G_UNAVAILABLE_ADDRESS) <--- Doc bug...
	// this is either not defined, or Doc is wrong
	// alert("The geocode for the given address or the route for the given
	// directions query cannot be returned due to legal or contractual
	// reasons.\n Error code: " + gdir.getStatus().code);

	else if (code == G_GEO_BAD_KEY)
		alert("The given key is either invalid or does not match the domain for which it was given. \n Error code: "
				+ code);
	else if (code == G_GEO_BAD_REQUEST)
		alert("A directions request could not be successfully parsed.\n Error code: "
				+ code);
	else
		alert("Sorry, no route can be found!");
}

DPlus.prototype.analyseRoute = function() {
	var route = this.directions.getRoute(0);
	var fuel = 0; // fuel in litres
	
	for (var i = 0; i < route.getNumSteps(); i++) {
		var step = route.getStep(i);
		var meters = step.getDistance().meters;
		var seconds = step.getDuration().seconds;
		var speed = (meters / MPM) / (seconds / 3600);
		fuel += this.calcFuelConsumption(speed, meters);
	}
	
	this.info.fuel = fuel;	
	this.info.cost = this.calcFuelCost(fuel);
	
	this.info.updateUI();
	this.info.reset();
	
	this.travelNewsDatabase.query(route);
}

// Google Maps 
DPlus.prototype.navigate = function() {
	if (this.from.value == "" || this.to.value == "") {
		alert("Neither the source address nor the destination address can be empty!");
		return;
	}
	
	// first use localSearch to locate the (latitude, longitude) of from and to,
	// then find the route between them.
	// when search is completed, will call "this.function"
	this.localSearch.setSearchCompleteCallback(this, function() {
		if (this.localSearch.results[0]) {
			var resultLat = this.localSearch.results[0].lat;
			var resultLng = this.localSearch.results[0].lng;
			var fromPoint = new GLatLng(resultLat, resultLng);
			
			// deal with "to"
			this.localSearch.setSearchCompleteCallback(this, function() {
				if (this.localSearch.results[0]) {
					var resultLat = this.localSearch.results[0].lat;
					var resultLng = this.localSearch.results[0].lng;
					var toPoint = new GLatLng(resultLat, resultLng);
					var query = "from: " + fromPoint + " to: " + toPoint;
					this.directions.load(query);
				} else {
					alert("The destination address can not be found!");
				}
			});

			this.localSearch.execute(this.to.value);
			
		} else {
			alert("The source address can not be found!");
		}
	});

	this.localSearch.execute(this.from.value);
}

DPlus.prototype.calcFuelConsumption = function(speed, meters) {
	var miles = meters / MPM; 
	var gallons = this.calcGallon1(speed, miles);
	return gallons * LPG;
}

// Fuel consumption A1
DPlus.prototype.calcGallon1 = function(speed, miles) {
	var g;
	if (speed >= EXTRA_URBAN_AVERAGE) {
		g = miles / this.preference.data.extra_urban_mpg;
	} else if (EXTRA_URBAN_AVERAGE - speed > speed - URBAN_AVERAGE) {
		g = miles / this.preference.data.combined_mpg;
	} else {
		g = miles / this.preference.data.urban_mpg;
	}
	return g;
}

// Fuel consumption A2
DPlus.prototype.calcGallon2 = function(speed, miles) {
	var g;
	if (speed >= EXTRA_URBAN_AVERAGE) {
		g = miles / this.preference.data.extra_urban_mpg;
	} else {
		g = miles / this.preference.data.urban_mpg;
	}
	return g;
}

DPlus.prototype.calcFuelCost = function(fuel) {
	var p = this.preference.data.fuel_price;
	return fuel * (p / 100);
}
