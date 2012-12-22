$(function() {
	api('get_vocabularies', function(data) {
		var $select = $('#vocabulary_select select');
		$.each(data.success, function(key,val) {
			$select.append('<option value="'+val.id+'">'+val.name+'</option>')
		});
		load_tags( $select.val(), '#tags', true );
		$('input[name|="vocabulary_id"]').val( $select.val() );		
	});

	$('#vocabulary_select select').change(function() {
		load_tags( $(this).val(), '#tags', true );
		$('input[name|="vocabulary_id"]').val( $(this).val() );		
	});

	$('#form_add_tag').submit(function(e) {
		e.preventDefault();
		var data = $(this).serialize();
		$('#form_add_tag #tag').val('');
		api( 'add_tag', append_tags('#tags'), data );
	});
	
	$('#clear').click(function() {
		$('#parent').val( '' );
		$('input[name|="parent_id"]').val( 0 );		
	});

	// on button delete tags (delete feeds_tags association)
	$('#tag_controls button.delete').click(function() {
		var message = 'are you sure you want to delete the selected tags? this will also affect feed/tags associations and all children will inherit the same parent';
		$('#dialog').empty().append( message ).dialog('option', {
			'title': 'confirm',
			'buttons': {
				'cancel': function() { $(this).dialog('close'); },
				'ok': function() {
					delete_tags();
				}
			}
		}).dialog('open');
	});
});

function delete_tags()
{
	$('#dialog').dialog('close');

	var $tags = $('#vocabulary_detail #tags span.selected');
	var id_array = new Array();
	$.each($tags, function(key,val) {
		id_array[key] = $(val).attr('id').replace('tag_', '');
	});
	// prepare post object
	var data = {};
	data.tag_id = id_array.join(',');

	api('delete_tags', function() {
		load_tags( $('#vocabulary_select select').val(), '#tags', true );
	}, data);
}