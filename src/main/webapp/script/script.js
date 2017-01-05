/**
 * bookmarklet script to be use in browser
 *
 * This function will download script/article-script.js from MT service,
 * and execute article-script.js#requestTranslations() after page is loaded.
 *
 * ${service.url} is set in pom.xml profile
 */
function bookmarklet() {
  var BASE_URL = '${service.url}';
  var scriptUrl = BASE_URL + '/script/article-script.js';
  var script = document.createElement('script');
  script.setAttribute('src', scriptUrl);
  script.addEventListener('load', function() {
    requestTranslations();
  }, false);
  document.body.appendChild(script);
}

// To display raw js in index.jsp
function displayRawJS() {
  document.getElementById('raw-js').innerHTML = bookmarklet.toString();
}

/**
 * To display bookmarklet js in index.jsp
 *
 * This is generated from http://mrcoles.com/bookmarklet based on JS function bookmarklet()
 * Note: update var#bookmarklet whenever bookmarklet() is being upadated
 */
function displayBookmarklet() {
  var bookmarklet = "javascript:(function()%7Bvar%20BASE_URL%20%3D%20'%2F%2Fmt-zanata.itos.redhat.com%2F'%3Bvar%20scriptUrl%20%3D%20BASE_URL%20%2B%20'script%2Fbookmarklet.js'%3Bvar%20script%20%3D%20document.createElement('script')%3Bscript.setAttribute('src'%2C%20scriptUrl)%3Bscript.addEventListener('load'%2C%20function()%20%7BrequestTranslations()%3B%7D%2C%20false)%3Bdocument.body.appendChild(script)%7D)()";

  document.getElementById('bookmarklet-link').href = bookmarklet;
  document.getElementById('bookmarklet-area').value = bookmarklet;
}
