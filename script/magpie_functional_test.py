import unittest
import requests
import json
from PIL import Image
from io import BytesIO
import warnings
import os

warnings.simplefilter("ignore", ResourceWarning)


class MagpieFunctionalTest(unittest.TestCase):
    """
    Execute tests for Magpie based on the specification
    These tests require the following environment variables:
    MAGPIEUSER: The header X-Auth-User value
    MAGPIEKEY: The header X-Auth-Token value
    MAGPIEURL: Target test environment, eg. http://magpiehost/api
    """
    magpie_user = os.environ['MAGPIEUSER']
    magpie_key = os.environ['MAGPIEKEY']
    headers = {'X-Auth-User': magpie_user, 'X-Auth-Token': magpie_key,
               'Content-Type': 'application/json'}
    size = 400, 200
    url = os.environ['MAGPIEURL']

    def describe(self, description, endpoint, expecting):
        print("---")
        print("Execute: " + description)
        print("Using: " + self.url+endpoint)
        print("With: " + str(self.headers))
        print("Expected: " + expecting)

    def test_zan6271(self):
        description = "The user can request an attribution image of a " \
                      "specified MT provider"
        endpoint = "backend/attribution?id=ms"
        self.describe(description, endpoint, "200 response and image")
        req = requests.get(self.url+endpoint, headers=self.headers)
        req.close()
        assert req.status_code == 200
        img = Image.open(BytesIO(req.content))
        img = img.resize(size=self.size)
        img.show(title="ZAN-6271")

    def test_zan6276(self):
        description = "Magpie will respond with 400 'id required' if the id " \
                      "parameter is not given​"
        endpoint = "backend/attribution"
        expected = "400 response"
        self.describe(description, endpoint, expected)
        req = requests.get(self.url+endpoint, headers=self.headers)
        req.close()
        assert req.status_code == 400

    def test_zan_6281(self):
        description = "Magpie will respond with 404 'id required' if the id " \
                      "parameter is not given"
        endpoint = "backend/attribution?id=bad"
        self.describe(description, endpoint, "404 response")
        req = requests.get(self.url+endpoint, headers=self.headers)
        req.close()
        assert req.status_code == 404

    def test_zan_6282(self):
        description = "The user can request a list of the available " \
                      "translator back-ends"
        endpoint = "backend"
        self.describe(description, endpoint,
                      "200 response and a list of back-ends")
        req = requests.get(self.url+endpoint, headers=self.headers)
        req.close()
        assert req.status_code == 200
        print("Actual: " + req.text)
        assert req.text == "[\"MS\",\"GOOGLE\"]"

    def test_zan_6283(self):
        description = "The user can request a list of supported " \
                      "translation locales"
        endpoint = "languages"
        self.describe(description, endpoint,
                      "200 response and a list of languages")
        req = requests.get(self.url+endpoint, headers=self.headers)
        req.close()
        assert req.status_code == 200
        print("Actual: " + req.text)
        assert req.text == "[{\"localeCode\":\"en-us\",\"name\":\"English (United States)\"},{\"localeCode\":\"de\",\"name\":\"German\"},{\"localeCode\":\"es\",\"name\":\"Spanish\"},{\"localeCode\":\"fr\",\"name\":\"French\"},{\"localeCode\":\"it\",\"name\":\"Italian\"},{\"localeCode\":\"ja\",\"name\":\"Japanese\"},{\"localeCode\":\"ko\",\"name\":\"Korean\"},{\"localeCode\":\"pt\",\"name\":\"Portuguese\"},{\"localeCode\":\"ru\",\"name\":\"Russian\"},{\"localeCode\":\"zh-hans\",\"name\":\"Chinese (Simplified)\"},{\"localeCode\":\"zh-hant\",\"name\":\"Chinese (Traditional)\"},{\"localeCode\":\"hi\",\"name\":\"Hindi\"}]"

    def test_zan_6284(self):
        description = "The user can request a list of previously translated " \
                      "document identifiers"
        endpoint = "documents"
        self.describe(description, endpoint, "200 response and a list of urls")
        req = requests.get(self.url+endpoint, headers=self.headers)
        req.close()
        assert req.status_code == 200
        print("Actual: " + req.text)
        assert "http://example.com" in (json.loads(str(req.text)))

    def test_zan_6285(self):
        description = "The user can request a list of previously translated " \
                      "document identifiers, within a given date range"
        endpoint = "documents?dateRange=2018-01-01..2020-01-01"
        self.describe(description, endpoint, "200 response and a list of urls")
        req = requests.get(self.url+endpoint, headers=self.headers)
        req.close()
        assert req.status_code == 200
        print("Actual: " + req.text)
        assert "http://example.com" in (json.loads(str(req.text)))

    def test_zan_6290(self):
        description = "Magpie will respond with 400 and 'range invalid' if " \
                      "the date range is incorrectly specified"
        endpoint = "documents?dateRange=2020-01-..2018-01-01"
        self.describe(description, endpoint, "400 response and range invalid")
        req = requests.get(self.url+endpoint, headers=self.headers)
        req.close()
        assert req.status_code == 400
        print("Actual: " + req.content.decode("utf-8"))
        assert json.loads(req.content).get('details') == \
            "Invalid data range: 2020-01-..2018-01-01"

    def test_zan_6289(self):
        description = "The user can request the translation of a phrase"
        endpoint = "document/translate?toLocaleCode=es"
        expected = "200 response and a translated phrase"
        self.describe(description, endpoint, expected)
        payload = {'url': 'http://example.com', 'contents': [{'value': 'Horizon', 'type': 'text/plain', 'metadata': 'test'}], 'localeCode': 'en-us', 'backendId': 'ms'}
        req = requests.post(self.url+endpoint, headers=self.headers, data=json.dumps(payload))
        req.close()
        assert req.status_code == 200
        print("Actual: " + str(req.content))
        assert json.loads(req.content).get('contents')[0].get('value') == \
            "Horizonte"

    def test_zan_6296(self):
        description = "Magpie will respond with 400 'bad request' for " \
                      "a missing request body"
        endpoint = "document/translate?toLocaleCode=es"
        expected = "400 response and an Empty content error"
        self.describe(description, endpoint, expected)
        req = requests.post(self.url+endpoint, headers=self.headers)
        req.close()
        assert req.status_code == 400
        print("Actual: " + str(req.content))
        assert json.loads(req.content).get('title') == "Empty content:null"

    def test_zan_6295(self):
        description = "Magpie will respond with 400 'invalid query param' if " \
                      "the target locale is not given"
        endpoint = "document/translate"
        expected = "400 response and an invalid toLocaleCode error"
        self.describe(description, endpoint, expected)
        payload = {'url': 'http://example.com', 'contents': [{'value': 'Horizon', 'type': 'text/plain', 'metadata': 'test'}], 'localeCode': 'en-us', 'backendId': 'ms'}
        req = requests.post(self.url+endpoint, headers=self.headers, data=json.dumps(payload))
        req.close()
        assert req.status_code == 400
        print("Actual: " + str(req.content))
        assert json.loads(req.content).get('title') == \
            "Invalid query param: toLocaleCode"

    def test_zan_6294(self):
        description = "Magpie will respond with 400 'invalid url' if the " \
                      "url attribute is not given"
        endpoint = "document/translate?toLocaleCode=es"
        expected = "400 response and an invalid url error"
        self.describe(description, endpoint, expected)
        payload = {'contents': [{'value': 'Horizon', 'type': 'text/plain', 'metadata': 'test'}], 'localeCode': 'en-us', 'backendId': 'ms'}
        req = requests.post(self.url+endpoint, headers=self.headers, data=json.dumps(payload))
        req.close()
        assert req.status_code == 400
        print("Actual: " + str(req.content))
        assert json.loads(req.content).get('title') == "Invalid url:null"

    def test_zan_6293(self):
        description = "Magpie will respond with 400 'Empty localeCode' if " \
                      "the localeCode attribute is not given"
        endpoint = "document/translate?toLocaleCode=es"
        expected = "400 response and an invalid localeCode error"
        self.describe(description, endpoint, expected)
        payload = {'url': 'http://www.example.com', 'contents': [{'value': 'Horizon', 'type': 'text/plain', 'metadata': 'test'}], 'backendId': 'ms'}
        req = requests.post(self.url+endpoint, headers=self.headers, data=json.dumps(payload))
        req.close()
        assert req.status_code == 400
        print("Actual: " + str(req.content))
        assert json.loads(req.content).get('title') == "Empty localeCode"

    def test_zan_6288(self):
        description = "Magpie will respond with 400 'Empty content' if there " \
                      "is a missing type/value attribute in the array"
        endpoint = "document/translate?toLocaleCode=es"
        expected = "400 response and an empty content error"
        self.describe(description, endpoint, expected)
        payload = {'url': 'http://www.example.com', 'contents': [{'type': 'text/plain', 'metadata': 'test'}], 'localeCode': 'en-us', 'backendId': 'ms'}
        req = requests.post(self.url+endpoint, headers=self.headers, data=json.dumps(payload))
        req.close()
        assert req.status_code == 400
        print("Actual: " + str(req.content))
        assert str(json.loads(req.content).get('title')).startswith("Empty content:")

    def test_zan_6287(self):
        description = "Magpie will use a default backend if backend is " \
                      "not specified"
        endpoint = "document/translate?toLocaleCode=es"
        expected = "200 response and a default backendId (Google)"
        self.describe(description, endpoint, expected)
        payload = {'url': 'http://www.example.com', 'contents': [{'value': 'Horizon', 'type': 'text/plain', 'metadata': 'test'}], 'localeCode': 'en-us'}
        req = requests.post(self.url+endpoint, headers=self.headers, data=json.dumps(payload))
        req.close()
        assert req.status_code == 200
        print("Actual: " + str(req.content))
        assert json.loads(req.content).get('contents')[0].get('value') == \
               "Horizonte"
        assert json.loads(req.content).get('backendId') == "GOOGLE"

    def test_zan_6286(self):
        alt_headers = {'X-Auth-Token': 'test',
                      'Content-Type': 'application/json'}
        description = "The user must provide an X-Auth-User header for " \
                      "all requests"
        endpoint = "backend/attribution?id=ms"
        expected = "401 response - not authorized"
        self.describe(description, endpoint, expected)
        req = requests.get(self.url+endpoint, headers=alt_headers)
        req.close()
        print("Actual: " + str(req.content))
        assert req.status_code == 401

    def test_zan_6292(self):
        alt_headers = {'X-Auth-User': 'test',
                      'Content-Type': 'application/json'}
        description = "The user must provide an X-Auth-token header for " \
                      "all requests"
        endpoint = "backend/attribution?id=ms"
        expected = "401 response - not authorized"
        self.describe(description, endpoint, expected)
        req = requests.get(self.url+endpoint, headers=alt_headers)
        req.close()
        print("Actual: " + str(req.content))
        assert req.status_code == 401

    def test_zan_6291(self):
        alt_headers = {'X-Auth-User': 'bad',  'X-Auth-Token': 'pair',
                      'Content-Type': 'application/json'}
        description = "Magpie must validate an X-Auth-User/Token pair for " \
                      "all requests"
        endpoint = "backend/attribution?id=ms"
        expected = "401 response - not authorized"
        self.describe(description, endpoint, expected)
        req = requests.get(self.url+endpoint, headers=alt_headers)
        req.close()
        print("Actual: " + str(req.content))
        assert req.status_code == 401


if __name__ == '__main__':
    unittest.main()
