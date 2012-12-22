<?php if ( ! defined('BASEPATH')) exit('No direct script access allowed');

/**
* Annotation_model
*/
class Annotation_model extends CI_Model
{
	
	function __construct()
	{
		parent::__construct();
		$this->load->model('vocabulary_model');
	}

	function get_triples( $subject_entity_id, $fetch_object_entity_id = TRUE )
	{
		// simil sparql query! fetching all triples where subject == this feed item
		if( $fetch_object_entity_id ) {
			$this->db->select('t.id, t.name, t.slug');
			$this->db->from('tagtriples as tt');
			$this->db->join('tags as t', 'tt.object_entity_id = t.id');
			$this->db->where('tt.subject_entity_id', $subject_entity_id);
			$this->db->where('t.stop_word', 0);
			$query = $this->db->get();
		} else {
			$this->db->select('id');
			$this->db->where('subject_entity_id', $subject_entity_id);
			$query = $this->db->get('tagtriples');
		}
		$response = array();
		foreach($query->result_array() as $row) {
			$response[] = $row;
		}
		return $response;
	}

	function annotate_subject_engine_objects( $vocabulary, $subject_type, $subject_id, $engine, $object_type, $object_array )
	{
		$vocabulary_id = $this->vocabulary_model->get_vocabulary_id( $vocabulary, TRUE );
		$objects = $this->vocabulary_model->add_tags($vocabulary_id, $object_array);

		$subject_type_id = $this->vocabulary_model->get_tag_id( $subject_type );
		$struct_act_annotate_id = $this->vocabulary_model->get_tag_id( STRUCT_ACT_ANNOTATE );
		$struct_engine = $this->vocabulary_model->get_tag_id( $engine );
		$object_type_id = $this->vocabulary_model->get_tag_id( $object_type );

		$tag_triples = array();
		foreach($objects as $object) {
			$data = array(
				'subject_tag_id' => $subject_type_id,
				'subject_entity_id' => $subject_id,
				'predicate_tag_id' => $struct_act_annotate_id,
				'predicate_entity_id' => $struct_engine,
				'object_tag_id' => $object_type_id,
				'object_entity_id' => $object->id
			);
			$this->db->insert('tagtriples', $data);
			$tag_triples[] = $this->db->insert_id();
		}
		return $tag_triples;
	}

	function annotate_stamat_people($feeditem_id, $person_array)
	{
		return $this->annotate_subject_engine_objects( VOCABULARY_EXTRACTED_PEOPLE, STRUCT_OBJ_FEEDITEM, $feeditem_id, STRUCT_ENG_STAMAT, STRUCT_OBJ_PERSON, $person_array);
	}

	function annotate_stamat_locations($feeditem_id, $location_array)
	{
		return $this->annotate_subject_engine_objects( VOCABULARY_EXTRACTED_LOCATIONS, STRUCT_OBJ_FEEDITEM, $feeditem_id, STRUCT_ENG_STAMAT, STRUCT_OBJ_LOCATION, $location_array);
	}

	function annotate_stamat_organizations($feeditem_id, $organization_array)
	{
		return $this->annotate_subject_engine_objects( VOCABULARY_EXTRACTED_ORGANIZATIONS, STRUCT_OBJ_FEEDITEM, $feeditem_id, STRUCT_ENG_STAMAT, STRUCT_OBJ_ORGANIZATION, $organization_array);
	}

	function annotate_feeditem_engine_entities($feeditem_id, $engine, $entities, $vocabulary = VOCABULARY_EXTRACTED_ENTITIES)
	{
		return $this->annotate_subject_engine_objects( $vocabulary, STRUCT_OBJ_FEEDITEM, $feeditem_id, $engine, STRUCT_OBJ_ENTITY, $entities);
	}

	function annotate_feeditem_engine_topics($feeditem_id, $engine, $topics, $vocabulary = VOCABULARY_EXTRACTED_TOPICS)
	{
		return $this->annotate_subject_engine_objects( $vocabulary, STRUCT_OBJ_FEEDITEM, $feeditem_id, $engine, STRUCT_OBJ_TOPIC, $topics);
	}

	function annotate_feeditem_engine_keywords($feeditem_id, $engine, $keywords, $vocabulary = VOCABULARY_EXTRACTED_TOPICS)
	{
		return $this->annotate_subject_engine_objects( $vocabulary, STRUCT_OBJ_FEEDITEM, $feeditem_id, $engine, STRUCT_OBJ_KEYWORD, $keywords);
	}

	function annotate_tags($feeditem_id, $tags)
	{
		$object_type_id = $this->vocabulary_model->get_tag_id( STRUCT_OBJ_TAG );
		$this->db->where('subject_entity_id', $feeditem_id);
		$this->db->where('object_tag_id', $object_type_id);
		$this->db->delete('tagtriples');
		return $this->annotate_subject_engine_objects( VOCABULARY_EXTRACTED_TAGS, STRUCT_OBJ_FEEDITEM, $feeditem_id, STRUCT_ENG_STAMAT, STRUCT_OBJ_TAG, $tags);
	}

	function annotate_micc_lda($feeditem_id, $topics, $entities, $prev_annotations)
	{
		$tagtriples_t = $this->annotate_feeditem_engine_topics( $feeditem_id, STRUCT_ENG_MICCLDA, $topics );
		$tagtriples_e = $this->annotate_feeditem_engine_entities( $feeditem_id, STRUCT_ENG_MICCLDA, $entities );

		$this->db->where('id', $feeditem_id);
		$data = array(
			'sem_annotated' => (ANNOTATED_MICC | (int)$prev_annotations)
		);
		$this->db->update('feeditems', $data);
		return array_merge($tagtriples_t, $tagtriples_e);
	}

	function annotate_teamlife_sanr($feeditem_id, $lang, $keywords, $prev_annotations)
	{
		$tagtriples = $this->annotate_feeditem_engine_keywords( $feeditem_id, STRUCT_ENG_TEAMLIFE, $keywords, VOCABULARY_TEAMLIFE );
		$language_id = $this->vocabulary_model->get_language_id( $lang, TRUE );

		$this->db->where('id', $feeditem_id);
		$data = array(
			'language_id' => $language_id,
			'sem_annotated' => (ANNOTATED_SANR | (int)$prev_annotations)
			);
		$this->db->update('feeditems', $data);
		return $tagtriples;
	}
}
