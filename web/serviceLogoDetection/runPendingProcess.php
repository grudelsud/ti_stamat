<?php 

// nel caso del server mi posiziono sulla cartella per poterlo eseguire anche da crontab
//chdir("/var/www/eutv-tools/process/service/lib/");


?>
<?php include "ewcfg50.php" ?>
<?php include "ewmysql50.php" ?>
<?php include "phpfn50.php" ?>
<?php require "function.php" ?>
<?php 


//$mode_test = true

//define("EW_DEBUG_ENABLED", TRUE, TRUE);
if(isset($_REQUEST['repeat'])){
//define("EW_REPEAT", FALSE, TRUE);
define("EW_REPEAT", TRUE, TRUE);
} else {
define("EW_REPEAT", FALSE, TRUE);
}
?>

<html>
<head>
	<title>MICC Process</title>
<link href="../../style_process.css" rel="stylesheet" type="text/css" />
<!-- Change the left navigation menu setting by editing this CSS -->
<!-- If you want to remove this javascript, remove the onload event in the body tag also -->
<script language="JavaScript1.2"  type="text/javascript">

</script>

<?php if(EW_REPEAT) { ?>
<script>
<!--

/*
Auto Refresh Page with Time script
By JavaScript Kit (javascriptkit.com)
Over 200+ free scripts here!
*/

//enter refresh time in "minutes:seconds" Minutes should range from 0 to inifinity. Seconds should range from 0 to 59
var limit="0:10"

if (document.images){
var parselimit=limit.split(":")
parselimit=parselimit[0]*60+parselimit[1]*1
}
function beginrefresh(){
if (!document.images)
return
if (parselimit==1)
window.location.reload()
else{ 
parselimit-=1
curmin=Math.floor(parselimit/60)
cursec=parselimit%60
if (curmin!=0)
curtime=curmin+" minutes and "+cursec+" seconds left until page refresh!"
else
curtime=cursec+" seconds left until page refresh!"
window.status=curtime
setTimeout("beginrefresh()",5000)
}
}

window.onload=beginrefresh
//-->
</script>
<?php } ?>
<meta name="generator"  />
</head>
<body>
<p><strong>Last call: <?php echo date('r') ?></strong></p><br><br>
<?php

// Open connection to the database
$conn = ew_Connect();
runPendingProcess();
?>
</body>
