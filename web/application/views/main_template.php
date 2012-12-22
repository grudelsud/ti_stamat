<!DOCTYPE html>
<html lang="en">
	<head>
		<meta charset="utf-8">
		<title>ReadReac.TV</title>
		<meta name="viewport" content="width=device-width, initial-scale=1.0">
		<meta name="description" content="">
		<meta name="author" content="">

		<?php $this->load->view('assets'); ?>
		<link rel="stylesheet" type="text/css" media="all" href="<?php echo ASSETS_URL; ?>css/main.style.css" />

		<script type="text/javascript" src="http://maps.googleapis.com/maps/api/js?key=AIzaSyD6c22UjErglZxSTfMBK9sDmXZWo_aTXVQ&sensor=true"></script>
		<script type="text/javascript" src="<?php echo ASSETS_URL; ?>js/libs/backbone-min.js"></script>
		<script type="text/javascript" src="<?php echo ASSETS_URL; ?>js/pages/main.<?php echo $template; ?>.js"></script>
		<script type="text/javascript" src="<?php echo ASSETS_URL; ?>js/pages/main.all.js"></script>

		<!-- Le HTML5 shim, for IE6-8 support of HTML5 elements -->
		<!--[if lt IE 9]>
			<script src="http://html5shim.googlecode.com/svn/trunk/html5.js"></script>
		<![endif]-->

		<!-- now the serious stuff, backbone ready to go -->
		<script type="text/javascript" src="<?php echo ASSETS_URL; ?>app/main.js"></script>
		<!-- and some modules -->
		<script type="text/javascript" src="<?php echo ASSETS_URL; ?>app/modules/tag.js"></script>
		<script type="text/javascript" src="<?php echo ASSETS_URL; ?>app/modules/media.js"></script>
		<script type="text/javascript" src="<?php echo ASSETS_URL; ?>app/modules/tweet.js"></script>
		<script type="text/javascript" src="<?php echo ASSETS_URL; ?>app/modules/feed.js"></script>
		<script type="text/javascript" src="<?php echo ASSETS_URL; ?>app/modules/feeditem.js"></script>
		<script type="text/javascript" src="<?php echo ASSETS_URL; ?>app/modules/reaction.js"></script>
		<script type="text/javascript" src="<?php echo ASSETS_URL; ?>app/modules/tweetfeed.js"></script>
		<script type="text/javascript" src="<?php echo ASSETS_URL; ?>app/modules/mediafinder.js"></script>
	</head>
	<body id="main" class="<?php echo $template; ?>">

		<!-- facebook js init -->
		<div id="fb-root"></div>
		<script type="text/javascript" src="<?php echo ASSETS_URL; ?>js/pages/fb.init.js"></script>

		<div class="container">
			<?php $this->load->view('main/header'); ?>
			<div class="head-margin">
			<?php $this->load->view('main/'.$template); ?>				
			</div>
			<?php $this->load->view('main/footer'); ?>
		</div><!-- end of .container -->
	</body>
</html>