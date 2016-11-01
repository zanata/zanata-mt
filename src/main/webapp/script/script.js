// bookmarklet script to be use in browser
function bookmarklet() {
  // var BASE_URL = '//localhost:8080/';
  var BASE_URL = '//mt-zanata.itos.redhat.com/';
  var scriptUrl = BASE_URL + 'script/bookmarklet.js';
  var script = document.createElement('script');
  script.setAttribute('src', scriptUrl);
  script.addEventListener('load', function() {
    requestTranslations();
  }, false);
  document.body.appendChild(script);
}

function displayRawJS() {
  document.getElementById('raw-js').innerHTML = bookmarklet.toString();
}
