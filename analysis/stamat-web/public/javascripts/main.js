$(function() {

	// pretty code in <pre></pre> wrappers
	prettyPrint();

	// selects stanford or gate for entity extraction changing the action to the form
	$('#form-extract-ent-classifier').change(function(e) {
		var entity_classifier = $('input:radio[name=entity-classifier]:checked').val();
		var $form = $('#form-extract-ent-content');
		$form.attr('action', entity_classifier);
	});

	// selects json or db as source for image indexing
	$('#form-indeximages-selectfrom').change(function(e) {
		var index_from = $('input:radio[name=index-selectfrom]:checked').val();
		var $form = $('#form-indeximages-content');
		$form.attr('action', index_from);
	});

	// sends data upon form submission. 
	// if radio button "data-format" is set to text it simply serielizes data and send
	// if radio button "data-format" is set to json, content type is set accordingly, first input in the form is read and sent
	$('form').submit(function(e) {
		e.preventDefault();
		var $form = $(e.target);
		var form_action = window.location.origin + $form.attr('action');
		var data_format = $('input:radio[name=data-format]:checked').val();
		if( data_format == 'text' ) {
			var form_data = $form.serialize();
			var content_type = 'application/x-www-form-urlencoded';
		} else {
			var form_data = $form.find(':input').first().val();
			var content_type = 'application/json';			
		}

		$.ajax({
			type: 'post',
			contentType: content_type,
			url: form_action,
			data: form_data,
			success: function(result) {
				$('#response-inner').empty().append(JSON.stringify(result));
			},
			error: function(jqXHR, textStatus, errorThrown) {
				$('#response').before('<div class="alert alert-error"><button class="close" data-dismiss="alert">×</button>'+ textStatus +' '+ jqXHR.status +' - '+ errorThrown +'</div>');
			}
		});
	});
});