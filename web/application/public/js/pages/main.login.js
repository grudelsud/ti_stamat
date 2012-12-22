$(function() {
	$('#fb_login').click(function() {
		FB.login(function(response) {
			if (response.authResponse) {
				console.log('Welcome!  Fetching your information.... ');
				FB.api('/me', function(response) {
					window.location = base_url + 'index.php/main/login_facebook';
				});
			} else {
				$('#auth_fb_message').empty().append('<p>Sorry, you cancelled login or did not fully authorize.</p>');
			}
		}, {scope: 'email'});
	});   
	$('#twitter_login').click(function() {
		window.location = base_url + 'index.php/main/login_twitter';
	});
});