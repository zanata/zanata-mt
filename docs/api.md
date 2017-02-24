# API

| Methods | Path |  Description |
|---|---|---|
|  {POST} | /api/translate?targetLang={targetLocale} |  [Translate document](#TranslateDocument) |
|  {GET} | /api/backend/attribution?id={backendId} | [Get attribution image](#GetAttributionImage) |

----

## Translate document<a name="TranslateDocument"></a>

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
    "value": String {REQUIRED: value},
    "type": String {REQUIRED: 'text/html', 'text/plain'},
    "metadata": String {Optional: metadata for entry}
}
```

### Error<a name="ErrorType"></a>
```
{
    "status": integer {http status code},
    "title": String {error title},
    "details": String {message},
    "timestamp": String {format: dd-MM-yyyy HH:mm:ssZ}
}
```
