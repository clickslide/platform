$(function () {
    $("#server-dd").change(function(){
	    var selectedServer = $("#server-dd option:selected").text();
        populateServicesCombo(selectedServer);
	});
    $("#service-dd").change(function(){
	    var selectedServer = $("#server-dd option:selected").text();
        var selectedService = $("#service-dd option:selected").text();
        populateOperationsCombo(selectedServer,selectedService);
	});
    //$("#service-dd").ufd({log:true});
    //$("#operation-dd").ufd({log:true});
    $("#filterOptions").click(function(){
        /*var selectedServer = $("#server-dd").find('option:selected').text();
        var selectedService = $("#service-dd").find('option:selected').text();
        var selectedOperation = $("#operation-dd").find('option:selected').text();
        reloadIFrame({server:selectedServer,
            service:selectedService,operation:selectedOperation});*/
        triggerCollect();
    });
    $("#timely-dd button").click(function(){
        $("#timely-dd button").removeClass('btn-primary');
        $(this).addClass('btn-primary');
        triggerCollect();
    });
});
function triggerCollect(){
        var selectedServer = $("#server-dd").find('option:selected').text();
        var selectedService = $("#service-dd").find('option:selected').text();
        var selectedOperation = $("#operation-dd").find('option:selected').text();
        var timeGrouping = $("#timely-dd button.btn-primary").text();
        reloadIFrame({server:selectedServer,
            service:selectedService,operation:selectedOperation,timeGroup:timeGrouping});
};
function reloadIFrame(param){
    var params = param || {};
    var server = param.server || "";
    var service = param.service || "";
    var operation = param.operation || "";
    var t = param.timeGroup || "";
    $("iframe").each(function(){
        //var id = $(this).attr('id');
        var currentUrl = $(this).attr('src');
        if(currentUrl.indexOf('?')){
            var absUrl = currentUrl.split('?');
            currentUrl = absUrl[0];
        }
        var newUrl = currentUrl+"?server="+encodeURI(server)+"&service="+
            encodeURI(service)+"&opr="+encodeURI(operation)+"&t="+t;
        $(this).attr('src',newUrl);
    });
};
function populateCombo(id,data){
	
}
$(document).ready(function(){
	$.ajax({
       		url:'populate_combos_ajaxprocessor.jag',
		dataType:'json', 
		success:function(result){
			
			var options = "<option value='__default__'></option>";
			for(var i=0;i<result.length;i++){
				var data = result[i];
				for(var key in data){
					options = options + "<option>"+data[key]+"</option>"
				}
			}
            $("#server-dd").find('option').remove();
            $("#server-dd").append(options);
		    //$("#server-dd").ufd({log:true,addEmphasis: true});
  	    }
		
	});
    /*$.getJSON("populate_combos_ajaxprocessor.jag?server=10.150.3.174:9443",
        function(data){
          alert(data);
        });*/
});
function populateServicesCombo(server){
     $.ajax({
       		url:'populate_combos_ajaxprocessor.jag?server='+server+'',
		dataType:'json',
		success:function(result){

			var options = "<option value='__default__'></option>";
			for(var i=0;i<result.length;i++){
				var data = result[i];
				for(var key in data){
					options = options + "<option>"+data[key]+"</option>"
				}
			}
            $("#service-dd").find('option').remove();
            $("#service-dd").append(options);
		    //$("#service-dd").ufd({log:true,addEmphasis: true});
  	    }

	});
};
function populateOperationsCombo(server,service){

     $.ajax({
       		url:'populate_combos_ajaxprocessor.jag?server='+server+'&service='+service+'',
		    dataType:'json',
		success:function(result){

			var options = "<option value='__default__'></option>";
			for(var i=0;i<result.length;i++){
				var data = result[i];
				for(var key in data){
					options = options + "<option>"+data[key]+"</option>"
				}
			}
            $("#operation-dd").find('option').remove();
            $("#operation-dd").append(options);
		    //$("#operation-dd").ufd({log:true,addEmphasis: true});
  	    }

	 });
};


