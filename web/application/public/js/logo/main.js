/*
 * jQuery File Upload Plugin JS Example 6.7
 * https://github.com/blueimp/jQuery-File-Upload
 *
 * Copyright 2010, Sebastian Tschan
 * https://blueimp.net
 *
 * Licensed under the MIT license:
 * http://www.opensource.org/licenses/MIT
 */

/*jslint nomen: true, unparam: true, regexp: true */
/*global $, window, document */

$(function () {
    'use strict';

	
	// Load dialog on click
	 $('.detailedResuls').live("click",function(e) {	
                var idProcess = [];
                idProcess=$(this).data('idprocess'); 
                $.getJSON('../logo/getDetectedImages', {'idProcess': idProcess}, function(data) {
                    console.log("getImages: " + data);
                    $('#galleria').empty();
                    $.each(data, function(entryIndex, entry) {
                        var html=""; 
                        html += '<a href="' +  entry['imageFrameUrl'] + '"> ';
                        html += '<img data-title="Frame"';
                        html += ' src="' + entry['imageFramethumbUrl'] + '"> </a> '
                         $('#galleria').append(html);
                    });    
                    Galleria.run('#galleria');
            $('#galleria').modal();
                });
		return false;
	});


   setInterval(checkStatus, 5000);


   function checkStatus(){
        $.getJSON('../logo/checkStatusJSON', function(data) {
            if (data.length>0){
                $('#tableProcess').empty();
                var table = document.getElementById("tableProcess");
                var rowCount = table.rows.length;
                $('#processListString').html("Processing List");
                var row = table.insertRow(rowCount);
                var cell1=row.insertCell(0);
                var cell2=row.insertCell(1);
                var cell3=row.insertCell(2);
                var cell4=row.insertCell(3);
                var cell5=row.insertCell(4);
                var cell6=row.insertCell(5);
                cell1.innerHTML="<h5>Logo</h5>";
                cell2.innerHTML="<h5>Video</h5>";
                cell3.innerHTML="<h5>Status</h5>";
                cell4.innerHTML="<h5>Detected frames</h5>";
                cell5.innerHTML="<h5>Detailed results</h5>";
                cell6.innerHTML="";
            }
            if (data.length==0){
                $('#tableProcess').empty();
                $('#processListString').html("");
            }
            
            $.each(data, function(entryIndex, entry) {
                console.log("the entry: " + entry);
                 
                rowCount = table.rows.length;
                row = table.insertRow(rowCount);
                row.setAttribute('id',entry['idProcess']);
                cell1=row.insertCell(0);
                cell2=row.insertCell(1);
                cell3=row.insertCell(2);
                cell4=row.insertCell(3);
                cell5=row.insertCell(4);
                cell5.setAttribute('class','detailedResuls');
                cell6=row.insertCell(5);
                cell6.setAttribute('class','delete');
                
                var logoUrl = entry['logoUrl'].replace(/^.*[\\\/]/, '');
                var videoUrl = entry['videoUrl'].replace(/^.*[\\\/]/, '');
                cell1.innerHTML=logoUrl;
                cell2.innerHTML=videoUrl;
                cell3.innerHTML=entry['status'];
                cell4.innerHTML=entry['detection'];
                cell5.innerHTML='<input type="button" name="basic" value="more Details" class="btn btn-primary detailedResuls" data-idProcess="' + entry['idProcess'] +'"/>';
                cell6.innerHTML='<button class="btn btn-danger deleteProcess" data-idProcess='+ entry['idProcess'] +'> <i class="icon-trash icon-white"></i> <span>Delete</span></button>';
                
             });
         }); 
   }
   
    

    
    $('#process_form').submit(function(e) {
        console.log("click");    
        var urls = [];
        $('input', $('.fileupload')).each(function(el) {
            if($(this).attr('type') == 'checkbox') {
               
                var c = $(this).get(0).checked;
                if(c == true) {
                    console.log($(this).parent().parent().find('.name a').attr('href'));
                    var url = $(this).parent().parent().find('.name a').attr('href');
                    urls.push(url);
                }
             } 
        });
        
        var urlsString = urls.join(",");
       
       
        $.post('../logo/process', {'urls': urlsString}, function(data) {
            console.log("You sent: " + data);
            checkStatus();
            $('input:checkbox').removeAttr('checked');
        });
        
        
        return false;
    });
    

  
    $('.deleteProcess').live("click",function(e) {
        console.log("clickDelete");    
        var idProcess = [];
        idProcess=$(this).data('idprocess');        
        $.post('../logo/deleteProcess', {'idProcess': idProcess}, function(data) {
            console.log("Delete: " + data);
            checkStatus();
        });
        
        return false;
    });

 

     $('.upLoadURL').live("click",function(e) {
        console.log("upLoadURL");    
        //var idProcess = [];
        var url=jQuery("#urlFile").val(); 
        $.post('../logo/upload', {'urlFile': url, "form_type": 3}, function(data) {
            console.log("Upload urlFile: " + data);
            $.each(data, function(entryIndex, entry) {
                var html = '';
                html += '<tr class="template-download fade in"><td><input type="checkbox" value="1"></td><td class="preview"></td>';
                html += '<td class="name"><a href="' + entry['url'] + '" title="' + entry['name']+'" rel="" download="'+ entry['name'] +'">'+entry['name']+'</a></td>'
                html += '<td class="size"><span>'+ entry['size'] + ' MB</span></td><td colspan="2"></td>';
                html += '<td class="delete"> <button class="btn btn-danger" data-type="DELETE" data-url="http://stamat.net/index.php/logo/upload">';
                html += '<i class="icon-trash icon-white"></i> <span>Delete</span> </button> </td></tr>';
            
                $('.files3').prepend(html);
            });

            checkStatus();
        });
        
        return false;
    });
    
   
    // Initialize the jQuery File Upload widget:
    //$('#fileupload').fileupload();


    $('.fileupload').each(function (index, obj) {
        var indice = parseInt(index+1);
        $(this).fileupload({
            dropZone: $(this),
            uploadTemplateId: "template-upload" + indice ,
            downloadTemplateId: "template-download" + indice,
            filesContainer: ".files" + indice,
            method : 'post',
            submit: function (e, data) {
                
                data.formData = {"form_type": indice};
             }
        });
    });
    
    // Enable iframe cross-domain access via redirect option:
    $('.fileupload').fileupload(
        'option',
        'redirect',
        window.location.href.replace(
            /\/[^\/]*$/,
            '/cors/result.html?%s'
        )
    );

    if (window.location.hostname === 'blueimp.github.com') {
        // Demo settings:
        $('.fileupload').fileupload('option', {
            
            url: '//jquery-file-upload.appspot.com/',
            maxFileSize: 5000000,
            acceptFileTypes: /(\.|\/)(gif|jpe?g|png)$/i,
            process: [
                {
                    action: 'load',
                    fileTypes: /^image\/(gif|jpeg|png)$/,
                    maxFileSize: 20000000 // 20MB
                },
                {
                    action: 'resize',
                    maxWidth: 1440,
                    maxHeight: 900
                },
                {
                    action: 'save'
                }
            ]
        });
        // Upload server status check for browsers with CORS support:
        if ($.support.cors) {
            $.ajax({
                url: '//jquery-file-upload.appspot.com/',
                type: 'HEAD'
            }).fail(function () {
                $('<span class="alert alert-error"/>')
                    .text('Upload server currently unavailable - ' +
                            new Date())
                    .appendTo('.fileupload');
            });
        }
    } else {
        // Load existing files:
        $('.fileupload').each(function (index, object) {
            var that = this;
            $.getJSON(this.action,{'form_type': index+1}, function (result) {
                if (result && result.length) {
                    $(that).fileupload('option', 'done')
                        .call(that, null, {result: result});
                }
                
            });
        });
    }

});
