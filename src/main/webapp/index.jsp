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
    <script type="text/javascript" src="javascript/jquery-2.0.3.js"></script>
    <script type="text/javascript" src="javascript/atmosphere.js"></script>
    <script type="text/javascript" src="javascript/application.js"></script>
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
<% response.addCookie(new Cookie(OurAtmosphereHandler.COOKIE_NAME, UUID.randomUUID().toString())); %>
<body>
    <div id="header"><h3>Atmosphere Push Test. Default transport is SSE (Server Side Events), fallback is long-polling.  Server will push a new status every few seconds, getting longer with each message</h3></div>
    <div id="content"></div>
    <div>
        <span id="status">Connecting...</span>
    </div>
</body>
</html>
