<div class="row">
	<div id="feed_container" class="span8">
		<h3>manage feeds</h3>
		<div id="add_feed">
			<?php echo form_open( 'add_feed', array('id' => 'form_add_feed') ); ?>
			<?php echo form_label( 'Title', 'title' ); ?>
			<?php echo form_input( array('name'=>'title', 'id'=>'title') ); ?>

			<?php echo form_label( 'URL', 'url' ); ?>
			<?php echo form_input( array('name'=>'url', 'id'=>'url') ); ?>

			<?php echo form_submit( 'submit', 'add feed' ); ?>
			<?php echo form_close(); ?>
		</div>
		<div id="fetch_feeds">
			<button type="button" class="fetch">cache all feeds to database</button>
			<p>this operation is performed periodically by a cron job</p>
		</div>

		<div id="feeds">
			<table width="100%" id="feeds_table">
				<thead>
					<tr>
						<th width="5%">id</th>
						<th width="25%">title</th>
						<th width="40%">tags</th>
						<th width="30%">url</th>
					</tr>
				</thead>
				<tbody></tbody>
			</table>
		</div>

		<div id="feed_detail">
			<div id="feed_controls">
				<!-- <button type="button" class="fetch_content">fetch permalinks content</button> -->
				<button type="button" class="delete">delete feed</button>
			</div>
			<div id="feed_content">
				<div id="feed_meta"></div>
				<h3>Tags</h3>
				<p>Tags assigned to this feed (select to delete)</p>
				<ul id="feed_tags"></ul>
			</div>
			<div id="tag_controls">
				<button type="button" class="delete">delete selected tags</button>
			</div>
		</div>
	</div>

	<div id="vocabulary_container" class="span4">
		<div id="vocabulary_select">
			<h3>select vocabulary</h3>
			<select></select>
		</div>
		<div id="vocabulary_detail">
		<div id="tags"></div>
		<div id="tag_controls">
			<button type="button" id="add_tag">add selected tags</button>
		</div>
		<hr/>
		<h3>colour codes</h3>
		<ul>
			<li><span>node</span></li>
			<li><span class="selected">selected</li>
			<li><span class="parent_over">parent</li>
			<li><span class="child_over">child</li>
		</ul>
		</div>		
	</div>
</div>