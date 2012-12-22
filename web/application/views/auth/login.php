<div class="mainInfo well">

	<h3>Login</h3>
	<p>Please login with your email/username and password below.</p>
	
	<div id="infoMessage"><?php echo $message;?></div>
	
	<?php echo form_open("auth/login");?>
	<label for="identity">Email/Username</label>
	<?php echo form_input($identity);?>

	<label for="password">Password</label>
	<?php echo form_input($password);?>

	<label for="remember" class="checkbox">Remember Me
		<?php echo form_checkbox('remember', '1', FALSE);?>
	</label>
	
	<?php echo form_submit('submit', 'Login');?>
	<?php echo form_close();?>

</div>
