var stamat_api_key = 'rxcvbnoibuvyctxrtcyvbn6oiu';

var feeds_table = {};
var selected_feed = {};
var feed_pagesize = 10;

$(function() {
	$('#dialog').dialog({autoOpen: false, resizable: false, height:140, modal: true});
	
	$('#vocabulary_detail #tags li span').live({
		click: function() {
			$(this).toggleClass('selected');
			// stuff for the form handling inserts
			if( $(this).hasClass('selected') ) {
				$('#parent').val( $(this).html() );
				$('input[name|="parent_id"]').val( $(this).attr('id').replace('tag_', '') );
			} else {
				$('#parent').val( '' );
				$('input[name|="parent_id"]').val( 0 );
			}
		},
		mouseover: function() {
			var children = 'child_'+$(this).attr('id').replace('tag_', '');
			$('#vocabulary_detail li span.'+children).addClass('child_over');

			if( $(this).hasClass('child') ) {
				var match = $(this).attr('class').match(/child_([0-9]+)/i)[1];
				var parent = 'tag_'+match;
				$('#vocabulary_detail li span#'+parent).addClass('parent_over');
			}
		},
		mouseout: function() {
			$('#vocabulary_detail #tags li span').removeClass('parent_over child_over');
		}
	});

	$('#feed_content .item button.show_content').live('click', function() {
		var feeditem_id = $(this).closest('.item').attr('id').replace('item_', '');
		$('#feed_content .item').removeClass('selected');
		$('#feed_content #item_'+feeditem_id).addClass('selected');
		var url = base_url + 'index.php/admin/permalink/' + feeditem_id;
		$('#permalink_content').empty().append('<iframe src="'+url+'"></iframe>');

		// $('#permalink_container').fadeIn();
		$('#permalink_container').modal('show');

		var data = {};
		data.feeditem_id = feeditem_id;
		api('fetch_entities', function(data) {
			var entities = 'tagged as: ';
			var $entities = $('#feed_content #item_'+feeditem_id+' div.entities').empty();
			$.each(data.success, function(key, val) {
				entities += '<span>'+val.name+'</span>' + ' ';
			});
			$entities.append( entities );
		}, data);

	});

	$('#permalink_container').on('hidden', function() {
		$('#feed_content .item').removeClass('selected');		
	});

});

// load content in feed table
function load_feeds(data)
{
	feeds_table = $('#feeds_table').dataTable({
		'bProcessing' : true,
		'bJQueryUI' : true,
		'aaData' : data.success,
		'aoColumns' : [
		{ 'mDataProp': 'id' },
		{ 'mDataProp': 'title' },
		{ 'mDataProp': 'tags' },
		{ 'mDataProp': 'url' },
		],
	});
}

function load_tags(vocabulary_id, root, clean) {
	if( clean == true ) {
		$(root).empty().append('<ul class="parent_tag_0"></ul>');
	}
	var data = {};
	data.vocabulary_id = vocabulary_id;
	data.limit = 50;
	api( 'get_vocabulary_tags', append_tags(root), data );
}

// used by admin.feed, admin.vocabulary, admin.topics
function append_tags(root)
{
	return function(data) {
		var first = true;
		var max = 0;
		$.each(data.success, function(key, val) {
			if( typeof(val.name) != 'undefined' ) {
				$append_list = $(root+' .parent_tag_'+val.parent_id);
				// size_* can be anything between 1 and 15, so we need to normalize val.count
				// assuming tags are sorted by count descending
				if( first ) {
					first = false;
					max = val.count;
				}
				var size = Math.round((val.count / max) * 14) + 1;
				$append_item = $('<li><span id="tag_'+val.id+'" class="child child_'+val.parent_id+' size_'+size+'">'+val.name+'</span></li>').append('<ul class="parent_tag_'+val.id+'"></ul>');
				if( val.parent_id == 0 ) {
					$append_item.addClass('clear');
				}
				$append_list.append( $append_item );
			}
		});
	}
}

function show_error_message( message ) {
	$('#dialog').empty().append( message ).dialog('option', {
		'title': 'error',
		'buttons': {
			'ok': function() { $(this).dialog('close'); }
		}
	}).dialog('open');
}

// make an api call
function api(method, callback, data) {
	if( typeof data == 'object' ) {
		data.access_key = stamat_api_key;		
	} else if( typeof data == 'string' ) {
		data += '&access_key=' + stamat_api_key;
	} else {
		data = {};
		data.access_key = stamat_api_key;		
	}
	$.ajax({
		cache    : false,
		data     : data,
		dataType : 'JSON',
		success  : callback,
		type     : 'POST',
		url      : base_url+'index.php/api/'+method
	});
}
