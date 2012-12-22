<!DOCTYPE html>
<html>
	<head>
		<?php if($template == 'users') : ?>
		<?php foreach($grocery->css_files as $file) : ?>
		<link type="text/css" rel="stylesheet" href="<?php echo $file; ?>" />
		<?php endforeach; ?>
		<?php foreach($grocery->js_files as $file) : ?>
		<script src="<?php echo $file; ?>"></script>
		<?php endforeach; ?>
		<?php endif; ?>

		<?php $this->load->view('assets'); ?>
		<link rel="stylesheet" type="text/css" media="all" href="<?php echo ASSETS_URL; ?>css/admin.style.css" />
		<script type="text/javascript" src="<?php echo ASSETS_URL; ?>js/pages/admin.<?php echo $template; ?>.js"></script>
		<script type="text/javascript" src="<?php echo ASSETS_URL; ?>js/pages/admin.all.js"></script>
	</head>
	<body id="admin" class="<?php echo $template; ?>">

		<?php $this->load->view('admin/header'); ?>
		<div class="container head-margin">
			<?php $this->load->view('admin/'.$template); ?>		
			<?php $this->load->view('admin/footer'); ?>
		</div>
		<div id="dialog"></div>

		<?php
			$debug = false;
			if( $debug ) {
				include 'phpBugLost.php';
				echo bl_debug();
			}
		?>
	</body>
</html>