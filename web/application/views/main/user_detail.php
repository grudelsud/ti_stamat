<div class="row">
	<div class="span12" id="auth_traditional">
		<p>Additional Info</p>
                <p>Please enter the users information below.</p>
                <div id="infoMessage"><?php echo $message;?></div>
                
                <?php echo form_open("main/update_user");?>
                
                <p>Username:<br />
                <?php echo form_input($username);?>
                </p>
                
                <p>Email:<br />
                <?php echo form_input($email);?>
                </p>
        
                <p>Password:<br />
                <?php echo form_input($password);?>
                </p>
      
                <p>Confirm Password:<br />
                <?php echo form_input($password_confirm);?>
                </p>
      
      
                <p><?php echo form_submit('submit', 'Create User');?></p>

      
                <?php echo form_close();?>
        
        </div>
     
    
</div>