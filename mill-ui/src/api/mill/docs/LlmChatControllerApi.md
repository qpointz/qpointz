# LlmChatControllerApi

All URIs are relative to *http://localhost:8080*

|Method | HTTP request | Description|
|------------- | ------------- | -------------|
|[**chat**](#chat) | **POST** /data-bot/chat | |
|[**ping**](#ping) | **GET** /data-bot/chat | |

# **chat**
> { [key: string]: any; } chat(requestBody)


### Example

```typescript
import {
    LlmChatControllerApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new LlmChatControllerApi(configuration);

let requestBody: { [key: string]: string; }; //

const { status, data } = await apiInstance.chat(
    requestBody
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **requestBody** | **{ [key: string]: string; }**|  | |


### Return type

**{ [key: string]: any; }**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: */*


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | OK |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **ping**
> string ping()


### Example

```typescript
import {
    LlmChatControllerApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new LlmChatControllerApi(configuration);

const { status, data } = await apiInstance.ping();
```

### Parameters
This endpoint does not have any parameters.


### Return type

**string**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: */*


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | OK |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

