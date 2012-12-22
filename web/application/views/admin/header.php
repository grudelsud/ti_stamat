<div class="navbar navbar-fixed-top">
	<div class="navbar-inner">
		<div class="container">
			<a class="brand" href="<?php echo site_url('/'); ?>">STAMAT <br/> Social Topics and Media Analysis Tool</a>

			<ul class="nav">
				<?php if( !empty($logged_admin) && $logged_admin ) : ?>
				<li><a href="<?php echo site_url('/admin/feed'); ?>">feeds</a></li>
				<li><a href="<?php echo site_url('/admin/items'); ?>">items</a></li>
				<li><a href="<?php echo site_url('/admin/vocabulary'); ?>">vocabularies</a></li>
				<li><a href="<?php echo site_url('/admin/topics'); ?>">topics &amp; entities</a></li>
				<li><a href="<?php echo site_url('/admin/logodetection'); ?>">logodetection</a></li>
				<!-- <li><a href="<?php echo site_url('/admin/users'); ?>">users</a></li> -->

				<li class="divider-vertical"></li>

				<!-- <li><a href="<?php echo site_url('/tools/create_slugs'); ?>">re-create slugs</a></li> -->
				<li><a href="<?php echo site_url('/tools/create_scrapers'); ?>">reset scrapers</a></li>
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

		</div>
	</div>
</div>