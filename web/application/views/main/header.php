<div class="navbar navbar-fixed-top">
	<div class="navbar-inner">
		<div class="container">
			<a class="brand" href="<?php echo BASE_URL; ?>">STAMAT</a>

			<?php if($template != 'login') : ?>
			<ul class="nav">
				<?php if( !empty($logged_admin) && $logged_admin ) : ?>
				<li><a href="<?php echo site_url('/admin'); ?>">admin</a></li>
				<?php endif; ?>
			</ul>

			<ul class="nav pull-right">
				<li class="dropdown">
					<a href="#" data-toggle="dropdown" class="dropdown-toggle">howdy, <?php echo $logged_user['username']; ?>! <b class="caret"></b></a>
					<ul class="dropdown-menu">
						<li><a href="<?php echo site_url('/auth/logout'); ?>">logout</a></li>
					</ul>
				</li>
			</ul>
			<?php endif; ?>
		</div>
	</div>
</div>