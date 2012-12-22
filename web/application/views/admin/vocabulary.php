<div class="row">

	<div class="span3">
		<div id="vocabulary_select">
			<h3>select vocabulary</h3>
			<select></select>
		</div>
		<h3>manage tags</h3>
		<div id="add_tag">
			<?php echo form_open( 'add_tag', array('id' => 'form_add_tag') ); ?>
			<?php echo form_label( 'Type the tag you want to add', 'tag' ); ?>
			<?php echo form_input( array('name'=>'tag', 'id'=>'tag') ); ?>

			<?php echo form_label( 'Parent tag: select from the cloud or leave empty for top level or <a id="clear" href="#">[click to clear]</a>', 'parent' ); ?>
			<?php echo form_input( array('name'=>'parent', 'id'=>'parent', 'disabled'=>'disabled') ); ?>
			<?php echo form_hidden( 'parent_id', '0' ); ?>
			<?php echo form_hidden( 'vocabulary_id', '0' ); ?>

			<?php echo form_submit( 'submit', 'add!' ); ?>
			<?php echo form_close(); ?>
		</div>
	</div>

	<div id="vocabulary_detail" class="span9">
		<h3>tags</h3>
		<div id="tags"></div>
		<div id="tag_controls"><button type="button" class="delete">delete selected tags</button></div>
		<hr/>
		<h3>colour codes</h3>
		<ul>
			<li><span>node</span></li>
			<li><span class="selected">selected</span></li>
			<li><span class="parent_over">parent</span></li>
			<li><span class="child_over">child</span></li>
		</ul>
	</div>

</div>