function navigate() {
	var from = document.getElementById("dplus-input-from");
	var to = document.getElementById("dplus-input-to");
	var result = document.getElementById("dplus-result");
	
	result.innerHTML = "<p>From: " + from.value + "</p><p>To: " + to.value + "</p>";
}