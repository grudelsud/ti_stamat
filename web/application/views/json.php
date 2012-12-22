<?php

	header('Access-Control-Allow-Origin: *');
	header('Content-type: application/json');

	header("HTTP/1.0 200 OK");
	header("HTTP/1.1 200 OK");
	header('Expires: Mon, 26 Jul 1997 05:00:00 GMT');
	header("Cache-Control: no-store, no-cache, must-revalidate");
	header("Cache-Control: post-check=0, pre-check=0");
	header("Pragma: no-cache");

	echo json_encode($json);
	exit;