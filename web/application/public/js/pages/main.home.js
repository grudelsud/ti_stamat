$(function() {

	var mapOptions = {
		center: new google.maps.LatLng(50, -10),
		zoom: 5,
		mapTypeId: google.maps.MapTypeId.ROADMAP
	};

	var map;

	$('#tab_map_link').on('show', function(e) {
		if( map == undefined) {
			// commented to disable map
			// map = new google.maps.Map(document.getElementById("map_canvas"), mapOptions);
		}
	});

	// crappy patch, this should be defined in one of the mediafinder views, poorly designed at the moment
	$('#select_vs_descriptor').on('change', function(e) {
		// commented to disable map
		// $('#content_media .selected').click();
	});
})