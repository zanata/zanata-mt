# API

## Translate article

{POST} /translate?targetLang={targetLocale}&inlineAttribution={true|false}

### Request

- Headers
    - X-Auth-User {[ZANATA_MT_ID](/system-properties.md)}
    - X-Auth-Token {[ZANATA_MT_API_KEY](/system-properties.md)}

- Body
   ```json
   {
     url: String {url of article},
     titleText: String {title text of the page},
     contentHTML: String {html content},
     articleType: String {'KCS_ARTICLE'},
     locale: String {source locale id}
   }
   ```

### Response

- Body
  ```json
  {
   url: String {url of article},
   titleText: String {translated titleText},
   contentHTML: String {translated contentHTML},
   articleType: String {'KCS_ARTICLE'},
   locale: String {translated locale id},
   backendId: String {'MS'}
  }
  ```

- Error
  ```json
  {
    status: integer {http status code},
    title: String {error title},
    details: String {message},
    timestamp: String {format: dd-MM-yyyy HH:mm:ssZ}
  }
   ```

----
