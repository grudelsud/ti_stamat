<?php
//define("EW_DEBUG_ENABLED", TRUE, TRUE);


function add_log($type,$text=""){
global $conn;
$log_activity = "INSERT INTO logsLogo (type,text) VALUES ('$type','".addslashes($text)."');";
$conn->Execute($log_activity);

}

function setProcessStatus($idProcessNum,$idProcessStatus){
	global $conn;

	//segnalo la fine del processo
	$query_update_status = "UPDATE process set idProcessStatus = '$idProcessStatus', end = now() WHERE idProcessNum = '".$idProcessNum."'";
	$conn->Execute($query_update_status);
	
	echo $query_update_status;
}

function setProcessStatusByNumId($idProcessNum,$idProcessStatus){
	global $conn;

	//segnalo la fine del processo
	$query_update_status = "UPDATE process set idProcessStatus = '$idProcessStatus', end = now() WHERE idProcessNum = '".$idProcessNum."'";
	$conn->Execute($query_update_status);
	
}

function setProcessOutputByNumProcessId($idProcessNum,$output){
	
		$idProcessNum =  ew_ExecuteScalar("SELECT idProcessNum from process WHERE  idProcessNum = '$idProcessNum'");

	     setProcessOutput($idProcessNum,$output);
	
	}



function setProcessOutput($idProcessNum,$output){
	global $conn;
	
	//echo "entrato send output";
	$rs = $conn->execute("SELECT * from process LEFT JOIN processstatus ON (process.idProcessStatus = processstatus.idProcessStatus) WHERE idProcessNum = '$idProcessNum'");
	//echo "ricercato processo";
	
	if($rs->RecordCount()==0)  return;
	
	$output_string = $rs->fields('output');
	//var_dump($output_string);
	
	$output_array = unserialize($rs->fields('output'));
	
	if(is_array($output_array)) {
		
		//echo "l'output � gi� un array e lo integro";
		$output_array = array_merge($output,$output_array);
	}
	else {
		 $output_array =  $output;
		 //echo "risultato vuoto. lo metto uguale a quello passato";
	
	}

	//segnalo la fine del processo
	$query_update_status = "UPDATE process set output = '".serialize($output_array)."', end = now() WHERE idProcessNum = '$idProcessNum'";
	
	$conn->Execute($query_update_status);
	//echo "risultati salvati con query: $query_update_status ";
	
	
}


function setProcessEnd($idProcessNum,$result = null){
	global $conn;
	
	add_log('setProcessEnd',"Imposto processo $idProcessNum come completato");
	setProcessStatus($idProcessNum,EW_END_PROCESS_STATUS);
	add_log('setProcessEnd',"Operazione eseguita");
	
	//verifico se il processo terminato era l'ultimo di una pipeline e chiudo anche la pipeline
	
	//recupero l'id del processo principale
	
	
	
	
	
}


function runPendingProcess(){

global $conn;


$pid_query = "select *  from process  WHERE (idProcessStatus = 2 OR idProcessStatus = 3) ";
		$rswrk = $conn->Execute($pid_query);
		
		
$num_process_running =  $rswrk->RecordCount();


$log="";



if($num_process_running) {  ///PARTE DI CONTROLLO SE I PROCESSI ATTIVI SONO ANCORA IN ESECUZIONE O IN TIMEOUT - INIZIO

		echo("$num_process_running Processes  running. <br>");
		
		while(!$rswrk->EOF) {
					//PsExec("ls /var/www/octo/");
					echo("Stampa controllo esecuzione processo <br>"); 
					$pid = $rswrk->fields("OSProcessId");
					
				
									echo "Controllo esecuzione eseguibile<br>";
					
					
									//eseguo la chiamata per verificare se il processo � ancora in esecuzione
									$exeCall = "ps --pid ".$pid;
									//$exeCall = "ps -p ".$pid;
									$result = exec($exeCall);
									echo "primo <pre>".$result."</pre>";
									
									/*
									if(
									//su linux
									$result =="  PID TTY          TIME CMD" || 
									// su mac osx
									$result =="")
									*/
									if(strpos($result, $pid)===false)  
										{
													//il processo non � pi� attivo. lo blocco dal gestionale
													$log_terminate = "Il processo non � pi� ".$pid." attivo ";
													add_log('runPendingProcess:finishing',$log_terminate);
													setProcessEnd($rswrk->fields("idProcessNum"));
										}
					
									else echo "il processo ".$pid." attivo<br>";
							
						    					/*	$to_time=time();
												$from_time=strtotime($rswrk->fields("start"));
												$second_in_execution= round(abs($to_time - $from_time));
												
												echo("Controllo timeout. Processo in esecuzione da ".$rswrk->fields("start")." -> $second_in_execution secondi.<br>");
												
												if($second_in_execution>3600) {
													echo "Processo andato in timeout (pi� di 3600 secondi). Fermato.";
													//setProcessEnd($rswrk->fields("idProcessNum"));
													setProcessStatus($rswrk->fields("idProcessNum"),6);
												}
						    */
												
							
					$rswrk->MoveNext();
		}
		
		
		
		
		
}   ///PARTE DI CONTROLLO SE I PROCESSI ATTIVI SONO ANCORA IN ESECUZIONE O IN TIMEOUT - FINE
echo "terminato controllo processi in esecuzione";
echo "limite processi in parallelo: ".EW_MAX_PROCESS_IN_EXECUTION;




if($num_process_running < EW_MAX_PROCESS_IN_EXECUTION)  { // caso di nessun processo in esecuzione->quindi cerco se ce n'� qualcuno da eseguire

						$select_query = "SELECT * FROM process WHERE idProcessStatus =1 OR  idProcessStatus =8  ORDER BY process.priority,idProcessNum";
						
						$rswrk = $conn->Execute($select_query);
						
						while(!$rswrk->EOF){// esiste un processo da eseguire
						
						
						
						//metto il servizio su starting
						$query_update_status = "UPDATE process set idProcessStatus = 3, start = now() WHERE idProcessNum = ".$rswrk->fields("idProcessNum");
						$conn->Execute($query_update_status);
						add_log('runPendingProcess:starting process',"INIZIALIZZATO PROCESSO ".$rswrk->fields("idProcessNum"));
						
						//aumento il numero di processi in esecuzione
						$num_process_running++;
						
						
					
									$command_complete = $rswrk->fields("command");

									/*$command_to_append = "lynx -dump http://".EW_ABS_HOST."/eutv-tools/process/service/lib/setProcessEnd.php?idProcessNum=".$rswrk->fields("idProcessNum");
									$command_complete = "( ".$rswrk->fields("command")." ; ".$command_to_append." )";
									*/
									$log .="<br>Report IdProcess: ".$rswrk->fields("idProcessNum")." <br><br>The command executed is: "
									. $command_complete . "<br><br>.";
									
									$result = PsExec($command_complete);
									
									//aggiorno l'OSProcessId eseguito
									$query_update_status = "UPDATE process set OSProcessId ='".$result."' WHERE idProcessNum = ".$rswrk->fields("idProcessNum");
									$conn->Execute($query_update_status);
								

						
						echo $log;
						
						add_log('runPendingProcess:starting REPORT',$log);
						
						
						//se sono al numero massimo di processi in esecuzione contemporanea non ne eseguo pi�
						if($num_process_running >= EW_MAX_PROCESS_IN_EXECUTION) {
							echo "Numero di processi massimi in esecuzione";
							break;
						}
						
						//ho eseguito il primo programma in esecuzione
						
						$rswrk->MoveNext();
						
			
			} 
			
			if($rswrk->RecordCount()==0) 
					{
						echo ("<br><br>No process pending.");	
						//sendTweet(date(DATE_RFC822)." - No process pending");
					}		

		/*$log_activity = "INSERT INTO logsLogo (type,text) VALUES ('runPendingProcess:test_cron','test call');";
		$conn->Execute($log_activity);
*/

}	//fine caso di esecuzione processo






}

function downloadPendingImages(){}



function checkFinishedTraining(){}



function syncroNewConcept(){}




    function PsExec($commandJob) {

        $command = $commandJob.' > /dev/null 2>&1 & echo $!';
        exec($command ,$op);
        $pid = (int)$op[0];

        if($pid!="") return $pid;

        return false;
    }
    
    
    function PsExists($pid) {

        exec("ps ax | grep $pid 2>&1", $output);
	   //var_dump($output);

        while( list(,$row) = each($output) ) {

                $row_array = explode(" ", $row);
			 //echo "<pre>"; var_dump($row_array);echo "</pre>"; 
                $check_pid = $row_array[0];
                $check_pid_1 = $row_array[1];

                if($pid == $check_pid || $pid == $check_pid_1) {
                        return true;
                }

        }

        return false;
    } 


function append_txt($string,$file_txt){
$file=fopen($file_txt,"a");
		fseek($file,0);
		fputs($file,$string."\n");
		fclose($file);
}





function start_crono($counter = "default"){
	
	global $time_start;
	
	$time_start[$counter] = microtime(true);
	
	}			
						

function end_crono($counter = "default"){
	
	global $time_start, $crono;
	
	
	$time_end = microtime(true);
	$time = number_format(($time_end - $time_start[$counter]),5);
	
	$msg = "<br />Execution time counter <em>$counter</em>: <strong>$time</strong> seconds";
	$crono[$counter] = $time;
	debug($msg);
	
	return $msg;
	
	}			


function executeProcess($input){
	
	global $conn;
	
	//inizializzo il parametro che viene utilizzato per passare ai processi il idProcessNum del processo precedente
	$previousIdProcessNum = "";

$idSetProcess = $input['idSetProcess'];


$command = "";

$x_idProcessNum = isset($input['x_idProcessNum'])? $input['x_idProcessNum'] : rand_int(12);

$sSqlWrk = "INSERT INTO `process` ( `idProcessNum`, `idSetProcess`, `idProcessStatus`, `start`, `end`, `OSProcessId`, `command`) VALUES".
 		"('".$x_idProcessNum."', ".$idSetProcess.", 1, NULL, NULL, NULL, '');";
$rswrk = $conn->Execute($sSqlWrk);


$idProcessNum = $conn->Insert_ID();




$sSqlWrk = "SELECT * from setprocess WHERE idSetProcess = ".$idSetProcess."";
$rswrk_setprocess = $conn->Execute($sSqlWrk);

$priority =  $rswrk_setprocess->fields('priority');

	$separator="";
				$equal_symbol="";
				$log_suff = "";

				if($rswrk_setprocess->fields('idProcessType')=="1"){
					//caso di un eseguibile
					$command .= $rswrk_setprocess->fields('exe')." ";
					//$send_pid = ($rswrk_setprocess->fields('sendPid')=="1"?" -p=".$x_idProcessNum:"");
					$send_pid = ($rswrk_setprocess->fields('sendPid')=="1"?" --pid=".$x_idProcessNum:"");
					$separator=" ";
					$equal_symbol="";
					$log_suff = " >> ".EW_EXE_FILE_LOG;
				
				}
				
				elseif($rswrk_setprocess->fields('idProcessType')=="2"){
					//caso di un web service http
					$command .= $rswrk_setprocess->fields('service')."?";
					$send_pid = ($rswrk_setprocess->fields('sendPid')=="1"?"&pid=".$x_idProcessNum:"");
					
					
					$separator="&";
					$equal_symbol="=";
					$log_suff = "";


				}


				//caricamento parametri processo
				$sSqlWrk = "SELECT * from processparams WHERE idSetProcess = ".$idSetProcess." AND type = 0 ORDER BY `order`,idProcessParams";
				$rswrk = $conn->Execute($sSqlWrk);
				$arwrk = ($rswrk) ? $rswrk->GetRows() : array();
				$rowswrk = count($arwrk);
				
				for ($rowcntwrk = 0; $rowcntwrk < $rowswrk; $rowcntwrk++) {
				//controllo se il valore � passato con l'id del paramentro
				if($arwrk[$rowcntwrk]['mode']=="0" && @$input["params_".$arwrk[$rowcntwrk]['idProcessParams']]=="1") $command .= $arwrk[$rowcntwrk]['code'].$separator;
				//controllo se il valore � passato con il code del paramentro
				if($arwrk[$rowcntwrk]['mode']=="0" && @$input[$arwrk[$rowcntwrk]['code']]=="1") $command .= $arwrk[$rowcntwrk]['code'].$separator;
				
				if($arwrk[$rowcntwrk]['mode']=="1" && @$input["params_".$arwrk[$rowcntwrk]['idProcessParams']]!="") $command .= $arwrk[$rowcntwrk]['code'].$equal_symbol.@$input["params_".$arwrk[$rowcntwrk]['idProcessParams']].$separator;
				if($arwrk[$rowcntwrk]['mode']=="1" && @$input[$arwrk[$rowcntwrk]['code']]!="") $command .= $arwrk[$rowcntwrk]['code'].$equal_symbol.@$input["params_".$arwrk[$rowcntwrk]['idProcessParams']].$separator;
				
				$sSqlWrk = "INSERT INTO `processparamsvalue` ( `idProcessParams`, idProcessNum,value) VALUES".
						"(".$arwrk[$rowcntwrk]['idProcessParams'].", ".$idProcessNum.", '".$_GET["params_".$arwrk[$rowcntwrk]['idProcessParams']].$input[$arwrk[$rowcntwrk]['code']]."');";
				$conn->Execute($sSqlWrk);
				
				}
				$command .=$send_pid;
				$command .=$log_suff;
if($rswrk_setprocess->fields('isMultiProcess')=="0") {
	$sSqlWrk = "UPDATE `process` set `command` = '".$command."', priority='".$priority."' WHERE idProcessNum= $idProcessNum";
	$conn->Execute($sSqlWrk);
}

//caso di un multiprocesso
if($rswrk_setprocess->fields('isMultiProcess')=="1") {}//caso di un multiprocesso - fine
	
	
	
	
	
	}
	
	function mysql_insert_query($table, $inserts) {
    $values = array_values($inserts);
    $keys = array_keys($inserts);
       
    return 'INSERT INTO '.$table.' (`'.implode('`,`', $keys).'`) VALUES (\''.implode('\',\'', $values).'\');'."\n";
}



function mysql_update_query($table, $rsnew,$id_key) {
    $upd_query = "UPDATE $table SET ";
    $flag = false;
    $updates_value = array();
    foreach($rsnew as $key=>$value){
	   /* echo "<br><br>analisi campo $key";
	    echo "<br>vecchio valore ".$rsold->fields($key);
	    echo "<br>nuovo valore ".$updates[$key];*/
	    
		    //se � il campo id vado al campo successivo
		    if($key==$id_key) continue;
		    
		    if($value == 'NOW()') $newvalue = $value;
		    elseif($value == 'null') $newvalue = 'null';
		    else $newvalue =  "'".check_string($value)."'";
		    
		    $updates_value[] = " $key = $newvalue ";
		   
	    
	    }
	    
	    
	 return $upd_query." ".implode(',', $updates_value)." WHERE $id_key = '".$rsnew[$id_key]."'; ";
}

function check_string($string){
	
	
	//return addslashes($string);
	return $string;
	}
	
	function getSoapValue($var){
		if(!isset($var)) return false;
		if(!isset($var)) return false;
		
		}


function requestVar($str,$default_value = "") {
	return ($_REQUEST[$str]!=""?mysql_real_escape_string($_REQUEST[$str]):$default_value);
}
function varRequest($str,$default_value = "") {
	return requestVar($str,$default_value);
}
function requestVarEscape($str,$default_value = "") {
	return ($_REQUEST[$str]!=""?mysql_real_escape_string($_REQUEST[$str]):$default_value);
}

function requestVarIntEscape($str,$default_value = null) {
	return (($_REQUEST[$str]!=""&&is_numeric($_REQUEST[$str]))?$_REQUEST[$str]:$default_value);
}

?>
