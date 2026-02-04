# AccessServiceControllerApi

All URIs are relative to *http://localhost:8080*

|Method | HTTP request | Description|
|------------- | ------------- | -------------|
|[**fetchQueryResult**](#fetchqueryresult) | **POST** /services/jet/FetchQueryResult | |
|[**getSchema**](#getschema) | **POST** /services/jet/GetSchema | |
|[**handshake**](#handshake) | **POST** /services/jet/Handshake | |
|[**listSchemas**](#listschemas) | **POST** /services/jet/ListSchemas | |
|[**parseSql**](#parsesql) | **POST** /services/jet/ParseSql | |
|[**submitQuery**](#submitquery) | **POST** /services/jet/SubmitQuery | |

# **fetchQueryResult**
> object fetchQueryResult(body)


### Example

```typescript
import {
    AccessServiceControllerApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new AccessServiceControllerApi(configuration);

let body: string; //
let accept: string; // (optional) (default to 'application/json')
let contentType: string; // (optional) (default to 'application/json')

const { status, data } = await apiInstance.fetchQueryResult(
    body,
    accept,
    contentType
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **body** | **string**|  | |
| **accept** | [**string**] |  | (optional) defaults to 'application/json'|
| **contentType** | [**string**] |  | (optional) defaults to 'application/json'|


### Return type

**object**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json, application/x-protobuf
 - **Accept**: application/json, application/x-protobuf


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | OK |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **getSchema**
> object getSchema(body)


### Example

```typescript
import {
    AccessServiceControllerApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new AccessServiceControllerApi(configuration);

let body: string; //
let accept: string; // (optional) (default to 'application/json')
let contentType: string; // (optional) (default to 'application/json')

const { status, data } = await apiInstance.getSchema(
    body,
    accept,
    contentType
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **body** | **string**|  | |
| **accept** | [**string**] |  | (optional) defaults to 'application/json'|
| **contentType** | [**string**] |  | (optional) defaults to 'application/json'|


### Return type

**object**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json, application/x-protobuf
 - **Accept**: application/json, application/x-protobuf


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | OK |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **handshake**
> object handshake()


### Example

```typescript
import {
    AccessServiceControllerApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new AccessServiceControllerApi(configuration);

let accept: string; // (optional) (default to 'application/json')
let contentType: string; // (optional) (default to 'application/json')
let body: string; // (optional)

const { status, data } = await apiInstance.handshake(
    accept,
    contentType,
    body
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **body** | **string**|  | |
| **accept** | [**string**] |  | (optional) defaults to 'application/json'|
| **contentType** | [**string**] |  | (optional) defaults to 'application/json'|


### Return type

**object**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json, application/x-protobuf
 - **Accept**: application/json, application/x-protobuf


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | OK |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **listSchemas**
> object listSchemas()


### Example

```typescript
import {
    AccessServiceControllerApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new AccessServiceControllerApi(configuration);

let accept: string; // (optional) (default to 'application/json')
let contentType: string; // (optional) (default to 'application/json')
let body: string; // (optional)

const { status, data } = await apiInstance.listSchemas(
    accept,
    contentType,
    body
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **body** | **string**|  | |
| **accept** | [**string**] |  | (optional) defaults to 'application/json'|
| **contentType** | [**string**] |  | (optional) defaults to 'application/json'|


### Return type

**object**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json, application/x-protobuf
 - **Accept**: application/json, application/x-protobuf


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | OK |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **parseSql**
> object parseSql(body)


### Example

```typescript
import {
    AccessServiceControllerApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new AccessServiceControllerApi(configuration);

let body: string; //
let accept: string; // (optional) (default to 'application/json')
let contentType: string; // (optional) (default to 'application/json')

const { status, data } = await apiInstance.parseSql(
    body,
    accept,
    contentType
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **body** | **string**|  | |
| **accept** | [**string**] |  | (optional) defaults to 'application/json'|
| **contentType** | [**string**] |  | (optional) defaults to 'application/json'|


### Return type

**object**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json, application/x-protobuf
 - **Accept**: application/json, application/x-protobuf


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | OK |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **submitQuery**
> object submitQuery(body)


### Example

```typescript
import {
    AccessServiceControllerApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new AccessServiceControllerApi(configuration);

let body: string; //
let accept: string; // (optional) (default to 'application/json')
let contentType: string; // (optional) (default to 'application/json')

const { status, data } = await apiInstance.submitQuery(
    body,
    accept,
    contentType
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **body** | **string**|  | |
| **accept** | [**string**] |  | (optional) defaults to 'application/json'|
| **contentType** | [**string**] |  | (optional) defaults to 'application/json'|


### Return type

**object**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json, application/x-protobuf
 - **Accept**: application/json, application/x-protobuf


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | OK |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

