<%@page import="com.github.dbadia.atomosphere.test.OurAtmosphereHandler"%>
<%@page import="java.util.UUID"%>
<%@page import="java.util.UUID"%>
<%@ page session="false" %>

<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1" />
    <title>Atmosphere Test</title>
    <!--[if lt IE 9]><script src="//ajax.googleapis.com/ajax/libs/jquery/1.12.4/jquery.min.js"></script><![endif]-->
	<!--[if IE 9]><!--><script src="//ajax.googleapis.com/ajax/libs/jquery/3.1.1/jquery.min.js"></script><!--<![endif]-->
    <script type="text/javascript" src="//cdnjs.cloudflare.com/ajax/libs/atmosphere/2.2.12/atmosphere.js"></script>
   	<script>
	// http://stackoverflow.com/a/11663507/2863942
	// Avoid `console` errors in browsers that lack a console.
	(function() {
	    var method;
	    var noop = function () {};
	    var methods = [
	        'assert', 'clear', 'count', 'debug', 'dir', 'dirxml', 'error',
	        'exception', 'group', 'groupCollapsed', 'groupEnd', 'info', 'log',
	        'markTimeline', 'profile', 'profileEnd', 'table', 'time', 'timeEnd',
	        'timeStamp', 'trace', 'warn'
	    ];
	    var length = methods.length;
	    var console = (window.console = window.console || {});
	
	    while (length--) {
	        method = methods[length];
	
	        // Only stub undefined methods.
	        if (!console[method]) {
	            console[method] = noop;
	        }
	    }
	}());
   	
   	
   	$(function () {
   	    "use strict";

   	    var content = $('#content');
   	    var input = $('#input');
   	    var status = $('#status');
   	    var myName = false;
   	    var author = null;
   	    var logged = false;
   	    var socket = atmosphere;
   	    var urlpath = window.location.pathname.substring(0, window.location.pathname.lastIndexOf("/")) +'/update';
   	    var request = { url: urlpath,
   	        contentType: "application/json",
   	        logLevel: 'debug',
   	        transport: 'sse',
   	        reconnectInterval: 5000,

   	        fallbackTransport: 'long-polling'};


   	    request.onOpen = function (response) {
   	    	console.log('atmosphere onOpen');
   	        content.html($('<p>', { text: 'Atmosphere connected using ' + response.transport }));
   	        input.removeAttr('disabled').focus();
   	    };

   	    request.onReconnect = function (request, response) {
   	    	console.log('atmosphere onReconnect');
   	        content.html($('<p>', { text: 'Connection lost, trying to reconnect. Trying to reconnect ' + request.reconnectInterval}));
   	        input.attr('disabled', 'disabled');
   	    };

   	    request.onReopen = function (response) {
   	    	console.log('atmosphere onReopen');
   	        input.removeAttr('disabled').focus();
   	        content.html($('<p>', { text: 'Atmosphere re-connected using ' + response.transport }));
   	    };

   	    request.onMessage = function (response) {
   	        var message = response.responseBody;
   	        console.log(message);
   	        try {
   	            var json = atmosphere.util.parseJSON(message);
   	        } catch (e) {
   	            console.log('This doesn\'t look like a valid JSON: ', message);
   	            return;
   	        }

   	        input.removeAttr('disabled').focus();
   	        if (!logged) {
   	            logged = true;
   	            status.text(myName + ': ').css('color', 'blue');
   	        } else {
   	            var color = json.author === author ? 'blue' : 'black';
   	          	var datetime = new Date();
   	            content.append('<p><span style="color:' + color + '">' + json.author + '</span> @ ' + +(datetime.getHours() < 10 ? '0' + datetime.getHours() : datetime.getHours()) + ':'
   	                + (datetime.getMinutes() < 10 ? '0' + datetime.getMinutes() : datetime.getMinutes())
   	                + ': ' + json.message + '</p>');
   	            if(json.message == 'AUTH_COMPLETE') {
   	            	subSocket.push(atmosphere.util.stringifyJSON({ author: author, message: 'redirect' }));
   	            	window.location.replace('done');
   	            }
   	            
   	        }
   	    };

   	    request.onClose = function (response) {
   	    	console.log('atmosphere onClose');
   	        content.html($('<p>', { text: 'Server closed the connection after a timeout' }));
   	        input.attr('disabled', 'disabled');
   	    };

   	    request.onError = function (response) {
   	    	console.log('atmosphere onError');
   	        content.html($('<p>', { text: 'Sorry, but there\'s some problem with your '
   	            + 'socket or the server is down' }));
   	    };
   	    var subSocket = socket.subscribe(request);

   	    
   	    author = new Date().getTime() / 1000;
   	    var msg = 'start';
   	    subSocket.push(atmosphere.util.stringifyJSON({ author: author, message: msg }));
   	    $(this).val('');
   	    myName = author;


   	    function addMessage(author, message, color) {
   	    	var datetime = new Date();
   	        content.append('<p><span style="color:' + color + '">' + author + '</span> @ ' + +(datetime.getHours() < 10 ? '0' + datetime.getHours() : datetime.getHours()) + ':'
   	            + (datetime.getMinutes() < 10 ? '0' + datetime.getMinutes() : datetime.getMinutes())
   	            + ': ' + message + '</p>');
   	    }
   	});
   	</script>
    <!--  <script type="text/javascript" src="javascript/application.js"></script> -->
    <style>
    * {font-family: tahoma; font-size: 12px; padding: 0px; margin: 0px;}
    p {line-height: 18px;}
    div {width: 500px; margin-left: auto; margin-right: auto;}
    #content {padding: 5px; background: #ddd; border-radius: 5px; border: 1px solid #CCC; margin-top: 10px;}
    #header {padding: 5px; background: #f5deb3; border-radius: 5px; border: 1px solid #CCC; margin-top: 10px;}
    #input {border-radius: 2px; border: 1px solid #ccc; margin-top: 10px; padding: 5px; width: 400px;}
    #status {width: 88px; display: block; float: left; margin-top: 15px;}
    </style>
</head>
<body>
    <div id="header"><h3>Atmosphere Push Test. Default transport is SSE (Server Side Events), fallback is long-polling.  Server will push a new status every few seconds, getting longer with each message</h3></div>
    <div id="content"></div>
    <div>
        <span id="status">Connecting...</span>
    </div>
</body>
</html>
