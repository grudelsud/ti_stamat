<?php if ( ! defined('BASEPATH')) exit('No direct script access allowed');

/**
* API
*/
class Api extends CI_Controller
{
	function __construct()
	{
		parent::__construct();
		$this->load->model('user_model');
	}
	
	function index()
	{
		$this->_user_check('API - index');
		$this->_return_json_success('all good');
	}
	
	/**
	 * Feed Items CRUD (no update)
	 */
	function fetch_store_all_feeds()
	{
		$result = array();
		$this->load->model('feed_model');
		$feeds = $this->feed_model->get_feeds();
		foreach( $feeds as $feed ) {
			$this->load->library('rss_parser');
			$this->rss_parser->set_feed_url( $feed->url );
			$feed_content = $this->rss_parser->get_feed();
	
			foreach ($feed_content->get_items() as $item) {
				$item_md5id = md5( $item->get_id() );
				$this->db->where('item_md5id', $item_md5id);
				$query = $this->db->get('feeditems');
				if( $query->num_rows() == 0 ) {
					$data = array(
						'feed_id' => $feed->id,
						'item_md5id' => $item_md5id,
						'title' => $item->get_title(),
						'permalink' => $item->get_permalink(),
						'date' => $item->get_date('Y-m-d H:i:s'),
						'description' => $item->get_description(),
						'abstract' => substr(strip_tags($item->get_description()), 0, 499)
					);
					$this->db->insert('feeditems', $data);
					$feeditem_id = $this->db->insert_id();

					// serious stuff, we scrape content on the go...
					$this->load->model('scraper_model');
					$this->scraper_model->scrape_diffbot($feeditem_id);

					$result[$feed->title][$item_md5id] = $item->get_title();
				}
			}
		}
		$this->_return_json_success( $result );			
	}
	
	// fetch all content scraping pages referenced by permalinks
	function fetch_store_all_permalinks()
	{
		$this->_user_check('API - fetch_store_all_permalinks');
	
		if( $feed_id = $this->input->post('feed_id') ) {
			$result = array();
			$this->load->model('scraper_model');
	
			$this->db->where('feed_id', $feed_id);
			$query = $this->db->get('feeditems');
			foreach($query->result() as $row) {
				// $result[] = $this->scraper_model->scrape_readitlater($row->id);
				$result[] = $this->scraper_model->scrape_diffbot($row->id);
			}
			$this->_return_json_success( $result );
		} else {
			$this->_return_json_error('empty feed_id');
		}		
	}
	
	function stupid_test()
	{
		$response = '{"tags":["Illinois","Copyright","Hagiography"],"icon":"http:\/\/www.repubblica.it\/images\/homepage\/apple-touch-icon.png","text":"Il corpo di un trentenne in vacanza in Spagna è stato recuperato in acqua, senza vestiti e con segni di lividi e percosse. Nei giorni scorsi il giovane aveva raccontato al padre, ex vicesindaco del capoluogo abruzzese, di essere stato rapinato\nUn\'immagine delle Canarie (ansa)L\'AQUILA - E\' stato trovato in acqua. Senza vestiti, con lividi e altri segni di percosse. Un giovane di 30 anni, Roberto Bonura, originario dell\'Aquila, è stato trovato senza vita nelle isole Canarie, in Spagna. Figlio dell\'avvocato Angelo - ex vicesindaco del capoluogo abruzzese - il ragazzo era in viaggio per festeggiare il compleanno ed è morto in circostanze ancora da chiarire completamente.\nIl corpo è stato rintracciato dopo le ricerche dovute a una denuncia al consolato italiano e alle forze dell\'ordine. Tra le ipotesi c\'è quella dell\'omicidio. Nei giorni scorsi Bonura aveva avvertito il padre di essere stato rapinato ed era stato invitato a tornare a casa.\nLa famiglia era già stata duramente colpita nel 2009: la madre del ragazzo, Nadia Ciuffini, era morta nel terremoto.\n(20 maggio 2012) © Riproduzione riservata","title":"Festeggiava compleanno alle Canarie giovane aquilano trovato morto","stats":{"fetchTime":5880,"confidence":"0.980"},"media":[{"link":"http:\/\/www.repubblica.it\/images\/2012\/05\/20\/164630670-1629140f-a47a-43a7-a421-952a3c5a2caf-th.jpg","type":"image"},{"link":"http:\/\/www.repubblica.it\/images\/2012\/05\/20\/144523023-3c30acf3-187d-40b3-8306-be2f8697931f-th.jpg","type":"image"},{"link":"http:\/\/www.repubblica.it\/images\/2012\/05\/20\/131854728-ee2753b2-4d94-417d-931a-87ab71f1b744-th.jpg","type":"image"},{"link":"http:\/\/www.repubblica.it\/images\/2012\/05\/20\/173746166-015e1c84-fa65-42ae-9a22-674b8e7d982a-th.jpg","type":"image"},{"link":"http:\/\/www.repubblica.it\/images\/2012\/05\/20\/025644714-5f3dde5b-5882-4de3-9ab9-d2debacc8aaf-th.jpg","type":"image"},{"link":"http:\/\/www.repubblica.it\/images\/2012\/05\/20\/005746344-3901a58d-b14e-4fc6-8143-64fa68d7921e-th.jpg","type":"image"},{"link":"http:\/\/www.repubblica.it\/images\/2012\/05\/20\/004140594-5f6cac5f-a023-47b1-9155-ff8b279892f4-th.jpg","type":"image"},{"link":"http:\/\/www.repubblica.it\/images\/2012\/05\/19\/175230741-fc3c3629-47d7-40db-a4bd-ed4a1fcab21f-th.jpg","type":"image"},{"link":"http:\/\/www.repubblica.it\/images\/2012\/05\/19\/151410251-968fd3d5-f04a-42d1-9540-eeba21510258-th.jpg","type":"image"},{"link":"http:\/\/www.repubblica.it\/images\/2012\/05\/19\/143445091-838b11e0-20cc-4d54-b0a7-441e23e2864a-th.jpg","type":"image"},{"link":"http:\/\/www.repubblica.it\/images\/2012\/05\/20\/204041096-1c3c443d-5181-4874-8a01-136216b5a30b.jpg","primary":"true","type":"image"}],"resolved_url":"http:\/\/www.repubblica.it\/cronaca\/2012\/05\/20\/news\/festeggiava_compleanno_alle_canarie_giovane_aquilano_trovato_morto-35568155\/?rss","url":"http:\/\/rss.feedsportal.com\/c\/32275\/f\/438637\/s\/1f879b35\/l\/0L0Srepubblica0Bit0Ccronaca0C20A120C0A50C20A0Cnews0Cfesteggiava0Icompleanno0Ialle0Icanarie0Igiovane0Iaquilano0Itrovato0Imorto0E355681550C0Drss\/story01.htm","xpath":"\/HTML[1]\/BODY[1]\/DIV[1]\/DIV[2]\/DIV[2]\/DIV[1]"}';
		$response_arr = json_decode($response, TRUE);
		print_r($response_arr);
	}

	function fetch_store_permalink()
	{
		$this->_user_check('API - fetch_store_permalink');
	
		if( $feeditem_id = $this->input->post('feeditem_id') ) {
			$result = array();
			$this->load->model('scraper_model');
			// $result[] = $this->scraper_model->scrape_readitlater($feeditem_id);
			$result[] = $this->scraper_model->scrape_diffbot($feeditem_id);
			$this->_return_json_success( $result );
		} else {
			$this->_return_json_error('empty feeditem_id');
		}		
	}
	
	function fetch_entities()
	{
		$this->_user_check('API - fetch_entities');
	
		if( $feeditem_id = $this->input->post('feeditem_id') ) {
			$result = array();
			$this->load->model('scraper_model');
			if( $this->input->post('annotate_micc') == 1 ) {
				$result['micc'] = $this->scraper_model->scrape_micc_lda($feeditem_id);
			} else if( $this->input->post('annotate_teamlife') == 1 ) {
				$result['sanr'] = $this->scraper_model->scrape_teamlife_sanr($feeditem_id);
			} else {
				$this->load->model('annotation_model');
				$result = $this->annotation_model->get_triples($feeditem_id);
			}
			$this->_return_json_success( $result );
		} else {
			$this->_return_json_error('empty feeditemcontents_id');
		}		
	}

	function fetch_check_stamat_ner()
	{
		$this->load->model('scraper_model');

		$this->db->where('flags', 0);
		$this->db->order_by('id', 'desc');
		$this->db->limit(1);
		$query = $this->db->get('feeditemcontents');

		if($query->num_rows() > 0) {
			$row = $query->row();
			$result = new stdClass;
			$result->response = $this->scraper_model->scrape_stamat_ner($row->content);
			$result->request = $row;
			$this->_return_json_success($result);
		} else {
			$this->_return_json_error('empty table');
		}
	}
	
	function fetch_stamat_entities()
	{
		$this->load->model('scraper_model');
		$this->load->model('annotation_model');

		$params = $this->uri->uri_to_assoc();
		$limit = preg_match('/[0-9]{1,3}/', $params['limit']) ? $params['limit'] : 99;

		$this->db->where('flags', 0);
		$this->db->order_by('id', 'desc');
		$this->db->limit($limit);
		$query = $this->db->get('feeditemcontents');

		$output = array();
		foreach ($query->result() as $row) {
			$result = $this->scraper_model->scrape_stamat_ner($row->content);

			// scrape_stamat_ner returns null either if ner is down or returned an error for some reason. this is why we use 3 equals here
			if($result === NULL) {
				$this->_return_json_error('stamat ner returned null, it might be a little bit tired. pls check that stamat-web framework is running.');
			}

			$entities[STRUCT_OBJ_PERSON] = array();
			$entities[STRUCT_OBJ_ORGANIZATION] = array();
			$entities[STRUCT_OBJ_LOCATION] = array();

			foreach ($result as $entity) {
				$type = strtolower($entity->type);
				$entities[$type][] = $entity->keyword;
			}

			$annotated = FALSE;

			if (count($entities[STRUCT_OBJ_PERSON])) {
				$this->annotation_model->annotate_stamat_people($row->feeditem_id, $entities[STRUCT_OBJ_PERSON]);
				$annotated = TRUE;
			}
			if (count($entities[STRUCT_OBJ_ORGANIZATION])) {
				$this->annotation_model->annotate_stamat_organizations($row->feeditem_id, $entities[STRUCT_OBJ_ORGANIZATION]);
				$annotated = TRUE;
			}
			if (count($entities[STRUCT_OBJ_LOCATION])) {
				$this->annotation_model->annotate_stamat_locations($row->feeditem_id, $entities[STRUCT_OBJ_LOCATION]);
				$annotated = TRUE;
			}

			// stamat_ner might return empty annotations, it's a tricky one, so we save it as called and empty
			if ($annotated) {
				$this->db->where('id', $row->id);
				$data = array('flags' => ANNOTATED_STAMAT);
				$this->db->update('feeditemcontents', $data);

				// just for output sake
				$item = new stdClass;
				$item->id = $row->id;
				$item->feeditem_id = $row->feeditem_id;
				$item->entities = $entities;
				$output[] = $item;
			} else {
				$this->db->where('id', $row->id);
				$data = array('flags' => ANNOTATED_STAMAT_EMPTY);
				$this->db->update('feeditemcontents', $data);
			}

		}
		$this->_return_json_success($output);
	}

	function count_feed_items()
	{
		$this->_user_check();
		// pagination used for admin.items.js
		if( $feed_id = $this->input->post('feed_id') ) {
			$this->db->where('feed_id', $feed_id);
			$this->db->from('feeditems');
			$this->_return_json_success( $this->db->count_all_results() );
		// pagination used for admin.topics.js
		} else if( $tag_array = $this->input->post('tag_array') ) {
			$this->load->model('vocabulary_model');
			$subject_type_id = $this->vocabulary_model->get_tag_id( STRUCT_OBJ_FEEDITEM );

			$this->db->where('subject_tag_id', $subject_type_id);
			$first = TRUE;
			foreach( $tag_array as $tag_id ) {
				if( $first ) {
					$this->db->where('object_entity_id', $tag_id);
					$first = FALSE;
				} else {
					$this->db->or_where('object_entity_id', $tag_id);					
				}
			}
			$this->db->from('tagtriples');
			$this->_return_json_success( $this->db->count_all_results() );
		} else {
			$this->_return_json_error('empty feed_id');
		}
	}
	
	function load_feed_items()
	{
		$this->_user_check();
	
		if( $feed_id = $this->input->post('feed_id') ) {			
	
			$this->db->where('feed_id', $feed_id);
			$this->db->order_by('date', 'desc');
			
			$offset = $this->input->post('offset') ? $this->input->post('offset') : 0;
			$limit = $this->input->post('limit') ? $this->input->post('limit') : 100;
			$query = $this->db->get('feeditems', $limit, $offset);
	
			$result = array();
			foreach ($query->result() as $row) {
				$result_item = new stdClass();
	
				$result_item->id = $row->id;
				$result_item->title = $row->title;
				$result_item->permalink = $row->permalink;
				$result_item->description = $row->description;
				$result_item->date = $row->date;
	
				$content_id = array();
				$this->db->select('id');
				$this->db->where('feeditem_id', $row->id);
				$query_content = $this->db->get('feeditemcontents');
				foreach( $query_content->result() as $row_content ) {
					$content_id[] = $row_content->id;
				}
				$result_item->content_id = $content_id;
	
				$result[] = $result_item;
			}
			
			$this->_return_json_success( $result );
		} else {
			$this->_return_json_error('empty feed_id');
		}
	}
	
	function load_tagged_feed_items()
	{
		$this->_user_check();

		if( $tag_array = $this->input->post('tag_array') ) {

			$this->load->model('vocabulary_model');
			$subject_type_id = $this->vocabulary_model->get_tag_id( STRUCT_OBJ_FEEDITEM );

			$this->db->select('feeditems.id, feeditems.title, feeditems.permalink, feeditems.description, feeditems.date');
			$this->db->distinct();
			$this->db->from('feeditems');
			$this->db->join('tagtriples', 'feeditems.id = tagtriples.subject_entity_id');
			$this->db->group_by('feeditems.id');
			$this->db->where('tagtriples.subject_tag_id', $subject_type_id);
			$first = TRUE;
			foreach( $tag_array as $tag_id ) {
				if( $first ) {
					$this->db->where('tagtriples.object_entity_id', $tag_id);
					$first = FALSE;
				} else {
					$this->db->or_where('tagtriples.object_entity_id', $tag_id);					
				}
			}

			$this->db->order_by('date', 'desc');
			$offset = $this->input->post('offset') ? $this->input->post('offset') : 0;
			$limit = $this->input->post('limit') ? $this->input->post('limit') : 100;
			$this->db->limit($limit, $offset);
			$query = $this->db->get();
			
			$this->_return_json_success( $query->result() );
		} else {
			$this->_return_json_error('empty triples');
		}
	}

	/**
	 * Feed related CRUD functions (no update)
	 * 
	 * add_feed - add feed to database
	 * get_feed - read feed + tag details by id
	 * get_feeds - get full feed list
	 * delete_feed - delete feed by id
	 */
	function add_feed()
	{
		$this->_user_check('API - add_feed');
		$title = $this->input->post('title');
		$url = $this->input->post('url');
		
		if( $title && $url ) {
			$this->load->model('feed_model');
			
			// TODO: should use dynamic user.id here, but ion_auth->user() doesn't seem to work
			$id = $this->feed_model->add_feed( $title, $url, 1 );
			$this->_return_json_success( $this->feed_model->get_feed( $id ) );
		} else {
			$this->_return_json_error('empty fields');
		}
	}
	
	function get_feed()
	{
		$this->_user_check();
		if( $feed_id = $this->input->post('feed_id') ) {
			$this->load->model('feed_model');
	
			$result = new stdClass();
			$result->feed = $this->feed_model->get_feed($feed_id);
			$result->tags = $this->feed_model->get_tags($feed_id);
			$this->_return_json_success( $result );
		}		
	}
	
	function get_feeds()
	{
		$this->_user_check();
		$this->load->model('feed_model');
		
		// TODO: should fetch logged user.id here, but bloody ion_auth->user() doesn't seem to work
		$this->_return_json_success( $this->feed_model->get_feeds( TRUE ) );
	}
	
	function delete_feed()
	{
		$this->_user_check('API - delete_feed');
		$feed_id = $this->input->post('feed_id');
		if( $feed_id ) {
			$this->load->model('feed_model');
			$this->_return_json_success($this->feed_model->delete_feed( $feed_id ));
		} else {
			$this->_return_json_error('empty field id');
		}
	}

	/**
	 * Feed/tag related CRUD functions (no update)
	 * 
	 * add_feed_tag - add feed / tag association to database
	 * get_feed_tags - read  tags associated to feed by feed_id
	 * delete_feed_tags - delete feed association by comma separated tag_id
	 */
	function add_feed_tag()
	{
		$this->_user_check('API - add_feed_tag');
		$feed_id = $this->input->post('feed_id');
		$tag_ids = $this->input->post('tag_id');
		
		if( $feed_id && $tag_ids ) {
			$this->load->model('feed_model');
			$result = $this->feed_model->add_tags( $feed_id, explode(',', $tag_ids) );
			$this->_return_json_success( $result );
		} else {
			$this->_return_json_error('both feed and tags must be selected');
		}
	}

	function get_feed_tags()
	{
		$this->_user_check();
	
		if( $feed_id = $this->input->post('feed_id') ) {
			$this->load->model('feed_model');
			$this->_return_json_success( $this->feed_model->get_tags($feed_id) );
		}
	}

	function delete_feed_tags()
	{
		$this->_user_check('API - delete_feed_tags');
		$tag_ids = $this->input->post('tag_id');
		
		if( $tag_ids ) {
			$this->load->model('feed_model');
			$result = $this->feed_model->delete_tags( explode(',', $tag_ids) );
			$this->_return_json_success( $result );
		} else {
			$this->_return_json_error('select tags 1st');
		}		
	}

	/**
	 * Vocabulary/tag related CRUD functions (no update)
	 * 
	 * add_tag - add tag to database
	 * get_vocabulary_tags - read tags associated to vocabulary by vocabulary_id
	 * delete_tags - delete tags by comma separated tag_id
	 */
	function add_tag()
	{
		$this->_user_check('API - add_tag');
		
		if( $tag = $this->input->post('tag') ) {
			$parent = $this->input->post('parent_id');
			$this->load->model('vocabulary_model');
			$vocabulary_id = $this->input->post('vocabulary_id') ? $this->input->post('vocabulary_id') : 1;
			$result[] = $this->vocabulary_model->add_tag( $vocabulary_id, $tag, empty( $parent ) ? NULL : $parent );
			$this->_return_json_success( $result );
		}
		
	}
	
	function get_vocabularies()
	{
		$this->_user_check();
		$this->load->model('vocabulary_model');
		$this->_return_json_success( $this->vocabulary_model->get_vocabularies() );
	}
	
	function get_vocabulary_tags()
	{
		$this->_user_check();
		$this->load->model('vocabulary_model');
		$vocabulary_id = $this->input->post('vocabulary_id') ? $this->input->post('vocabulary_id') : 1;
		$limit = $this->input->post('limit') ? $this->input->post('limit') : 100;
		$this->_return_json_success( $this->vocabulary_model->get_tags( $vocabulary_id, $limit ) );
	}
	
	function delete_tags()
	{
		$this->_user_check('API - delete_tags');
		$tag_ids = $this->input->post('tag_id');
		
		if( $tag_ids ) {
			$this->load->model('vocabulary_model');
			$result = $this->vocabulary_model->delete_tags( explode(',', $tag_ids) );
			$this->_return_json_success( $result );
		} else {
			$this->_return_json_error('select tags 1st');
		}
	}

	/**
	 * user & output related stuff
	 */
	private function _user_check($message = '')
	{
		if( $this->user_model->api_check() ) {
			if( !empty($message) ) {
				$this->user_model->log( $message );
			}
		}
	}
	
	/**
	 * returns success message in json
	 */
	private function _return_json_success($success) {
		$this->_return_json('success', $success);
	}
	
	/**
	 * returns error message in json
	 */
	private function _return_json_error($error) {
		$this->_return_json('error', $error);
	}
	
	/**
	 * returns a json array
	 */
	private function _return_json($response, $message) {
		$data = array(
			'json' => array(
				$response => $message
			)
		);
		$this->load->view('json', $data);
	}
}

/* end of api.php */