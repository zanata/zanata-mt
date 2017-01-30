<html>
<head>
  <title>Zanata Machine Translations</title>
  <link rel="shortcut icon"
      href="http://assets-zanata.rhcloud.com/master/favicon.ico"/>
  <link rel="stylesheet"
      href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css"
      integrity="sha384-BVYiiSIFeK1dGmJRAkycuHAHRg32OmUcww7on3RYdg4Va+PmSTsz/K68vbdEjh4u"
      crossorigin="anonymous">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <script
      src="https://code.jquery.com/jquery-3.1.1.min.js"
      integrity="sha256-hVVnYaiADRTO2PzUGmuLJr8BLUSjGIZsDYGmIJLv2b8="
      crossorigin="anonymous"></script>
  <script src="/script/script.js"></script>
</head>
<body>
<h1>Zanata Machine Translations</h1>
<div class="container-fluid">
  <h2>To use this service</h2>
  <ul>
    <li>
      1) Drag <a id="bookmarklet-link" href="This will be filled by bookmarklet js"><strong>this bookmarklet</strong></a> to your browser bookmark toolbar OR create a bookmark in your browser and copy/paste the bookmarklet
      below as the link.
    </li>
    <li>
      2) Visit KCS article page from <a href="https://access.redhat.com/solutions" target="_blank">https://access.redhat.com/solutions</a>, click on the 'Globe'
      icon from the top right menu and select a translation language.
    </li>
    <li>
      3) Click on the created bookmarklet to request for machine translations.
    </li>
  </ul>
</div>

<hr/>

<div class="container-fluid">
  <h2>Bookmarklet Javascript</h2>
  <div>This bookmarklet generated from raw javascript and using <a
    href="http://mrcoles.com/bookmarklet" target="_blank">http://mrcoles.com/bookmarklet</a></div>
    <textarea style="width: 50em;height:9em" readonly id="bookmarklet-area">
       <!--This will be filled by bookmarklet js -->
    </textarea>

  <h2>Raw Javascript</h2>
  <pre id="raw-js">
    <!--This will be filled by raw js -->
  </pre>
</div>
<script>
  displayBookmarklet();
  displayRawJS();
</script>
</body>
</html>
