<?php defined('BASEPATH') OR exit('No direct script access allowed');

class logo extends CI_Controller {

	function __construct()
	{
		parent::__construct();
                $this->load->library('ion_auth');
		$this->load->library('session');
		$this->load->database();
		$this->load->helper('url');
	}

	//redirect if needed, otherwise display the user list
	function index()
	{
		if (!$this->ion_auth->logged_in())
		{
			//redirect them to the login page
			redirect('auth/login', 'refresh');
		}
		else
		{
                	$this->load->view('logo/index');
		}
	}
        
       
        
        public function upload()
        {
            error_reporting(E_ALL | E_STRICT);

            
            
            $data = $this->input->post('newsletter');
        
            $this->load->helper("upload.class");
        
            if (isset($_REQUEST['form_type'])) {
                $form_type = $_REQUEST['form_type'];
            } else {
                $form_type = "";
            }
        
            $upload_handler = new UploadHandler();
            $upload_handler->setPath($form_type);
        
        
            header('Pragma: no-cache');
            header('Cache-Control: no-store, no-cache, must-revalidate');
            header('Content-Disposition: inline; filename="files.json"');
            header('X-Content-Type-Options: nosniff');
            header('Access-Control-Allow-Origin: *');
            header('Access-Control-Allow-Methods: OPTIONS, HEAD, GET, POST, PUT, DELETE');
            header('Access-Control-Allow-Headers: X-File-Name, X-File-Type, X-File-Size');
            switch ($_SERVER['REQUEST_METHOD']) {
            case 'OPTIONS':
                break;
            case 'HEAD':
            case 'GET':
                $upload_handler->get();
                break;
            case 'POST':
                if (isset($_REQUEST['_method']) && $_REQUEST['_method'] === 'DELETE') {
                    $upload_handler->delete();
                    $file_path = $upload_handler->getFullFilenameUrlDelete();
                    
                    if ($form_type ==1){
                        $queryString ='DELETE FROM logo WHERE url="'. $file_path . '"';
                        $query=$this->db->query($queryString);
                    }
                    elseif ($form_type ==2)   {
                        $queryString ='DELETE FROM logoModel WHERE url="'. $file_path . '"';
                        $query=$this->db->query($queryString);
                    }
                    elseif ($form_type ==3)   {
                        $queryString ='DELETE FROM video WHERE url="'. $file_path . '"';
                        $query=$this->db->query($queryString);
                    }
                    
                } else {
                   
                    
                    
                    if (!isset($_REQUEST['urlFile'])){
                        $upload_handler->post();
                    }
                    else{
                      
                        $urlFile = $_REQUEST['urlFile'];
                        $ch = curl_init();
                        $source = $urlFile;
                        curl_setopt($ch, CURLOPT_URL, $source);
                        curl_setopt($ch, CURLOPT_RETURNTRANSFER, 1);
                        $data = curl_exec ($ch);
                        curl_close ($ch);
                        
                        $destination = "/var/www/stamat/application/public/tmp/". basename($urlFile);
                        $file = fopen($destination, "w+");
                        fputs($file, $data);
                        fclose($file);
                        $urlFilename=basename($urlFile);
                        $sizeUrlFilename= filesize($destination);
                        $files= array(
                            "error" => array(0),
                            "name" => array($urlFilename),
                            "size" => array($sizeUrlFilename),
                            "tmp_name" => array($destination),
                            "type" => array("getURLFile")
                        );
                        
                        $_FILES=array("files" => $files);
                        print_r($files);
                        $upload_handler->postGetUrl();
                       
                        //unlink($destination);
                        
                   }
                    $urlString=$upload_handler->getFullFilenameUrl();
                    $pathString = $upload_handler->getFullFilename();
                    if ($form_type ==1){
                        $queryString = 'INSERT INTO logo (url,path) VALUES("'. $urlString . '" , "' . $pathString . '")';
                        $query=$this->db->query($queryString);
                    }
                    elseif ($form_type ==2)   {
                        $queryString = 'INSERT INTO logoModel (url,path) VALUES("'. $urlString . '" , "' . $pathString . '")';
                        $query=$this->db->query($queryString);
                    }
                    elseif ($form_type ==3)   {
                        $queryString = 'INSERT INTO video (url,path) VALUES("'. $urlString . '" , "' . $pathString . '")';
                        $query=$this->db->query($queryString);
                    }
                   
                }
                break;
            case 'DELETE':
                $upload_handler->delete();
                break;
            default:
                header('HTTP/1.1 405 Method Not Allowed');
        }

    }
    
    public function process(){
        
        $urls = $this->input->post('urls');
        $urlsArray= explode (",",$urls);
        $imageUrl = $urlsArray[0];
        $videoUrl = $urlsArray[1];
        $queryString = "SELECT id, path FROM logo WHERE  url='" .$imageUrl. "'";
        $query=$this->db->query($queryString);
        $row = $query->result();
        $id_logo = $row[0]->id;
        $path_logo = $row[0]->path;
        
        $queryString = "SELECT id, path FROM video WHERE  url='" .$videoUrl. "'";
        $query=$this->db->query($queryString);
        $row = $query->result();
        $id_video = $row[0]->id;
        $path_video = $row[0]->path;
       
        
        
        // telecom server
        $queryString = 'INSERT INTO process (name, idProcessStatus, id_logo, id_video) VALUES("Logo Detection",1,'. $id_logo . ',' . $id_video . ')';
        $query=$this->db->query($queryString);
        $id_process = $this->db->insert_id();
        
        $match_params = "-S -e SIFT -d PyramidSIFT -m FlannBased -v -f 2  -R";
        $resultImage_path = "/var/www/stamat/application/public/logoResults/" . $id_process . "/";
        $resultCVS_path = "/var/www/stamat/application/public/logoResults/". $id_process . "/";
        $LOGORECOG_BIN = "/var/www/stamat/application/public/scripts/logorecog";

        mkdir($resultImage_path, 0777);
        
        $execString = $LOGORECOG_BIN . " -q  ". $path_logo . " -V " . $path_video . " " . $match_params . " -s ". $resultImage_path ." -u " . $resultCVS_path ."; ";
        $fileNameCSV = "q_" . basename($path_logo) . "--_t_" . basename($path_video) . ".csv";
        $cvsFile = "/var/www/stamat/application/public/logoResults/". $id_process . "/" . $fileNameCSV;
        //$cvsFile = "/var/www/stamat/application/public/logoResults/test.csv";
        $javaCVSreader ="java -jar /var/www/stamat/application/public/scripts/importlogoDetectionCSV.jar " . $id_process . " " . $cvsFile;
        
        
        $execString ="(". $execString . $javaCVSreader . ")";
        $queryStringID = 'UPDATE process SET command="'.$execString.'"  WHERE  idProcessNum=' . $id_process;
        //echo $queryStringID;
        $query=$this->db->query($queryStringID);
        
        
    }
    
    public function getDetectedImages(){
        $idProcessNum = $_REQUEST['idProcess'];
        $queryString = "SELECT id, idProcessNum, imageFrame, score  FROM processResults WHERE idProcessNum=".$idProcessNum ;
        $query=$this->db->query($queryString);
        $results= array();
        $logoResultUrl = base_url() . "application/public/logoResults/" . $idProcessNum . "/";
        foreach ($query->result() as $row){
          
            if ($row->score>=0.2){ 
              $imageFrameUrlFull=$logoResultUrl . $row->imageFrame . ".jpg";
              $imageFrameThumbUrlFull=$logoResultUrl . $row->imageFrame . "_thumb.jpg";
              $resultRow = array(
                    'id' => $row->id,
                    'idProcess' => $row->idProcessNum,
                    'imageFrameUrl' => $imageFrameUrlFull,
                    'imageFramethumbUrl' =>   $imageFrameThumbUrlFull 
                );  
                array_push($results,$resultRow);
          }
        }
        $this->output->set_content_type('application/json')
                 ->set_output(json_encode($results));
        
    }
    
    public function deleteProcess(){
        $idProcessNum = $_REQUEST['idProcess'];
        $queryString = "DELETE FROM process WHERE idProcessNum=".$idProcessNum ;
        $query=$this->db->query($queryString);
        
        $queryString = "DELETE FROM processResults WHERE idProcessNum=".$idProcessNum ;
        $query=$this->db->query($queryString);
        
        $resultImage_path = "/var/www/stamat/application/public/logoResults/" . $idProcessNum . "/";
        
        
        $this->rrmdir($resultImage_path);
    }


    # recursively remove a directory
    public function rrmdir($dir) {
        foreach(glob($dir . '/*') as $file) {
            if(is_dir($file))
                rrmdir($file);
            else
                unlink($file);
        }
        rmdir($dir);
    }

    
    public function checkStatus(){
        $this->load->database();
        $queryString = "SELECT process.idProcessNum AS idProcess, logo.url AS logoUrl, video.url AS videoUrl, processstatus.name As status, process.detection 
                        FROM process JOIN logo ON process.id_logo = logo.id JOIN video ON process.id_video = video.id JOIN processstatus on processstatus.idProcessStatus = process.idProcessStatus";
        $query=$this->db->query($queryString);
        $num=$query->num_rows();
        $results= array();
        foreach ($query->result() as $row)
        {
            $scoreFrame=0.2;
            $scoreVideo=5;
            $queryString="SELECT numFrame FROM processResults WHERE idProcessNum =" .$row->idProcess ." AND score >= ". $scoreFrame;
            $query=$this->db->query($queryString);
            $num=$query->num_rows();
            if ($num>=$scoreVideo){
                $detection =$num;
            }
            else {
                $detection=$num;
            }
            if ($row->status!="Finished") 
                {$detection="";}
            $resultRow = array(
               'idProcess' => $row->idProcess,
               'logoUrl'  => $row->logoUrl,
               'videoUrl' => $row->videoUrl,
               'status' => $row->status,
               //'detection' => $row->detection
               'detection' => $detection
               );
            array_push($results,$resultRow);
            
            
        }
        return  $results;
   }
   
       public function checkStatusJSON(){
        $results=$this->checkStatus();
        $this->output->set_content_type('application/json')
                 ->set_output(json_encode($results));
        
   }

	
}
