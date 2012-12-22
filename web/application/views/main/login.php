<div class="row">
	<div class="span6" id="auth_traditional">
		<?php $this->load->view('auth/login'); ?>
	</div>

	<div class="span6">
		<h3>Alternatively</h3>
		<p>
			<button id="fb_login" type="button">Login with Facebook</button>
			<div id="auth_fb_message"></div>
		</p>
		<p>
			<button id="twitter_login" type="button">Login with Twitter</button>
		</p>
	</div>

	<!--
 	<div class="span4" id="mobile_apps">
		<p>Mobile App</p>
		<div id="app_android"><p>Android</p></div>
		<div id="app_ios"><p>iPhone / iPad</p></div>
	</div>
	 -->
</div>