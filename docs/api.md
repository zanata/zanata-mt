# API

| Methods | Path |  Description |
|---|---|---|
|  {POST} | /api/translate?targetLang={targetLocale} |  [Translate article](#TranslateArticle) |
|  {POST} | /api/translate?targetLang={targetLocale} |  [Translate raw article](#TranslateRawArticle) |
|  {GET} | /api/backend/attribution?id={backendId} | [Get attribution image](#GetAttributionImage) |

----

## Translate article<a name="TranslateArticle"></a>

{POST} /api/translate?targetLang={targetLocale}

- [TypeString](#TypeString)

### Request

- Headers
    - X-Auth-User {[ZANATA_MT_ID](/docs/system-properties.md)}
    - X-Auth-Token {[ZANATA_MT_API_KEY](/docs/system-properties.md)}

- Body
   ```
   {
     'url': String {REQUIRED: url of article},
     'contents': TypeString[] {REQUIRED, MAX-TOTAL-LENGTH(6000):  Array of TypeString},
     'locale': String {REQUIRED, MAX-LENGTH(255): source locale id}
   }
   ```

### Response

- Body
  ```
  {
    'url': String {url of article},
    'contents': [TypeString](#typeString)[] {Array of translated strings},
    'articleType': String {'KCS_ARTICLE'},
    'locale': String {translated locale id},
    'backendId': String {'MS', use for attribution insertion}
  }
  ```

- [Error](#ErrorType)

----

## Translate raw article<a name="TranslateRawArticle"></a>

{POST} /api/translate?targetLang={targetLocale}

### Request

- Headers
    - X-Auth-User {[ZANATA_MT_ID](/docs/system-properties.md)}
    - X-Auth-Token {[ZANATA_MT_API_KEY](/docs/system-properties.md)}

- Body
   ```
   {
       'url': String {REQUIRED: url of article},
       'titleText': String {title text of the page},
       'contentHTML': String {html content},
       'articleType': String {REQUIRED: 'KCS_ARTICLE'},
       'locale': String {REQUIRED, MAX-LENGTH(255): source locale id},
   }
   ```

### Response

- Body
  ```
  {
    'url': String {url of article},
    'titleText': String {translated title text of the page},
    'contentHTML': String {translated html content},
    'locale': String {translated locale id},
    'backendId': String {'MS', use for attribution insertion}
  }
  ```

- [Error](#ErrorType)

----

## Backend Attribution<a name="GetAttributionImage"></a>

{GET} /api/backend/attribution?id={backendId}

### Request

- Headers
    - X-Auth-User {[ZANATA_MT_ID](/docs/system-properties.md)}
    - X-Auth-Token {[ZANATA_MT_API_KEY](/docs/system-properties.md)}

### Response

- Body: type `image/png`

- [Error](#ErrorType)

----

## Types

### TypeString<a name="TypeString"></a>
```
{
    'value': String {REQUIRED: value},
    'type': String {REQUIRED: 'text/html', 'text/plain'}
}
```

### Error<a name="ErrorType"></a>
```
{
    'status': integer {http status code},
    'title': String {error title},
    'details': String {message},
    'timestamp': String {format: dd-MM-yyyy HH:mm:ssZ}
}
```
