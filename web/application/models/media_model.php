<?php if ( ! defined('BASEPATH')) exit('No direct script access allowed');

/**
* Media_model
*/
class Media_model extends CI_Model
{
	function __construct()
	{
		parent::__construct();
	}

	// get media by id
	function get_media( $id )
	{
		$this->db->where('id', $id );
		$query = $this->db->get('feeditemmedia');
		return $query->result();
	}

	function get_media_array( $type, $primary, $flags, $tag, $min_width, $min_height, $page, $pagesize, &$meta = NULL )
	{
		$feeds = array();
		if(!empty($tag)) {
			$this->db->select('f.id');
			$this->db->from('feeds as f');
			$this->db->join('feeds_tags as ft', 'f.id = ft.feed_id');
			$this->db->join('tags as t', 'ft.tag_id = t.id');
			$this->db->where('t.slug', $tag);
			$query = $this->db->get();

			foreach ($query->result() as $row) {
				$feeds[] = $row->id;
			}
		}

		$meta = new stdClass();
		$meta->params = '';

		$this->db->select('fim.id, fim.url, fim.type, fim.primary, fim.flags, fim.hash, fim.abs_path, fim.width, fim.height, fi.title, fi.permalink, fi.date');
		$this->db->from('feeditemmedia as fim');
		$this->db->join('feeditems as fi', 'fim.feeditem_id = fi.id');

		if (!empty($feeds)) {
			$meta->params = 'tag/'.$tag.'/';
			$this->db->where_in('fi.feed_id', $feeds);
		}

		if (!empty($type)) {
			$this->db->where('fim.type', $type);
		}
		if (!empty($primary)) {
			$this->db->where('fim.primary', $primary);
		}
		if (!empty($flags)) {
			$this->db->where('fim.flags', $flags);
		}
		if (!empty($min_width)) {
			$this->db->where('fim.width >', $min_width);
		}
		if (!empty($min_height)) {
			$this->db->where('fim.height >', $min_height);
		}
		$limit = 100;
		$offset = 0;

		$meta->pagesize = $limit;
		$meta->page = 1;

		if( !empty($pagesize) && is_numeric($pagesize) && $pagesize > 0 ) {
			$limit = $pagesize;
			$meta->pagesize = $pagesize;
		}
		if( !empty($page) && is_numeric($page) && $page > 0 ) {
			$offset = $limit * ($page - 1);
			$meta->page = $page;
		}

		$this->db->limit($limit, $offset);
		$this->db->order_by('fim.id', 'desc');
		$query = $this->db->get();
		$result = $query->result();

		// TODO: check this, I can't believe we need to rerun the query just to count the number of results
		$this->db->select('fim.id');
		$this->db->from('feeditemmedia as fim');
		$this->db->join('feeditems as fi', 'fim.feeditem_id = fi.id');

		if (!empty($feeds)) {
			$this->db->where_in('fi.feed_id', $feeds);
		}

		if (!empty($type)) {
			$this->db->where('fim.type', $type);
		}
		if (!empty($primary)) {
			$this->db->where('fim.primary', $primary);
		}
		if (!empty($flags)) {
			$this->db->where('fim.flags', $flags);
		}
		if (!empty($min_width)) {
			$this->db->where('fim.width >', $min_width);
		}
		if (!empty($min_height)) {
			$this->db->where('fim.height >', $min_height);
		}

		$meta->count_all_results = $this->db->count_all_results();
		$meta->count_all_pages = ceil($meta->count_all_results / $meta->pagesize);

		$meta->prev = $meta->params.'page/'.($meta->page > 1 ? $meta->page - 1 : 1);
		$meta->next = $meta->params.'page/'.($meta->page < $meta->count_all_pages ? $meta->page + 1 : $meta->count_all_pages);

		return $result;
	}

	function update_flags( $id, $flags )
	{
		$data = array('flags' => $flags);
		$this->db->where('id', $id);
		$this->db->update('feeditemmedia', $data);
		return $this->db->affected_rows();
	}
}

/* end of media_model.php */