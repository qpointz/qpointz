# NlSqlChatControllerApi

All URIs are relative to *http://localhost:8080*

|Method | HTTP request | Description|
|------------- | ------------- | -------------|
|[**chatStream**](#chatstream) | **GET** /api/nl2sql/chats/{chatId}/stream | |
|[**createChat**](#createchat) | **POST** /api/nl2sql/chats | Creates new chat|
|[**deleteChat**](#deletechat) | **DELETE** /api/nl2sql/chats/{chatId} | Deletes chat chat|
|[**getChat**](#getchat) | **GET** /api/nl2sql/chats/{chatId} | |
|[**listChatMessages**](#listchatmessages) | **GET** /api/nl2sql/chats/{chatId}/messages | |
|[**listChats**](#listchats) | **GET** /api/nl2sql/chats | |
|[**postChatMessages**](#postchatmessages) | **POST** /api/nl2sql/chats/{chatId}/messages | |
|[**updateChat**](#updatechat) | **PATCH** /api/nl2sql/chats/{chatId} | Updates chat|

# **chatStream**
> Array<any> chatStream()


### Example

```typescript
import {
    NlSqlChatControllerApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new NlSqlChatControllerApi(configuration);

let chatId: string; // (default to undefined)

const { status, data } = await apiInstance.chatStream(
    chatId
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **chatId** | [**string**] |  | defaults to undefined|


### Return type

**Array<any>**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: */*, text/event-stream


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**404** | Not Found |  -  |
|**200** | OK |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **createChat**
> Chat createChat(createChatRequest)


### Example

```typescript
import {
    NlSqlChatControllerApi,
    Configuration,
    CreateChatRequest
} from './api';

const configuration = new Configuration();
const apiInstance = new NlSqlChatControllerApi(configuration);

let createChatRequest: CreateChatRequest; //

const { status, data } = await apiInstance.createChat(
    createChatRequest
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **createChatRequest** | **CreateChatRequest**|  | |


### Return type

**Chat**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | OK |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **deleteChat**
> deleteChat()


### Example

```typescript
import {
    NlSqlChatControllerApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new NlSqlChatControllerApi(configuration);

let chatId: string; // (default to undefined)

const { status, data } = await apiInstance.deleteChat(
    chatId
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **chatId** | [**string**] |  | defaults to undefined|


### Return type

void (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: */*


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**404** | Not Found |  -  |
|**204** | No Content |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **getChat**
> Chat getChat()


### Example

```typescript
import {
    NlSqlChatControllerApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new NlSqlChatControllerApi(configuration);

let chatId: string; // (default to undefined)

const { status, data } = await apiInstance.getChat(
    chatId
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **chatId** | [**string**] |  | defaults to undefined|


### Return type

**Chat**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: */*, application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**404** | Not Found |  -  |
|**200** | OK |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **listChatMessages**
> Array<ChatMessage> listChatMessages()


### Example

```typescript
import {
    NlSqlChatControllerApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new NlSqlChatControllerApi(configuration);

let chatId: string; // (default to undefined)

const { status, data } = await apiInstance.listChatMessages(
    chatId
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **chatId** | [**string**] |  | defaults to undefined|


### Return type

**Array<ChatMessage>**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: */*, application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**404** | Not Found |  -  |
|**200** | OK |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **listChats**
> Array<Chat> listChats()


### Example

```typescript
import {
    NlSqlChatControllerApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new NlSqlChatControllerApi(configuration);

const { status, data } = await apiInstance.listChats();
```

### Parameters
This endpoint does not have any parameters.


### Return type

**Array<Chat>**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | OK |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **postChatMessages**
> ChatMessage postChatMessages(sendChatMessageRequest)


### Example

```typescript
import {
    NlSqlChatControllerApi,
    Configuration,
    SendChatMessageRequest
} from './api';

const configuration = new Configuration();
const apiInstance = new NlSqlChatControllerApi(configuration);

let chatId: string; // (default to undefined)
let sendChatMessageRequest: SendChatMessageRequest; //

const { status, data } = await apiInstance.postChatMessages(
    chatId,
    sendChatMessageRequest
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **sendChatMessageRequest** | **SendChatMessageRequest**|  | |
| **chatId** | [**string**] |  | defaults to undefined|


### Return type

**ChatMessage**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: */*, application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**404** | Not Found |  -  |
|**200** | OK |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **updateChat**
> Chat updateChat(updateChatRequest)


### Example

```typescript
import {
    NlSqlChatControllerApi,
    Configuration,
    UpdateChatRequest
} from './api';

const configuration = new Configuration();
const apiInstance = new NlSqlChatControllerApi(configuration);

let chatId: string; // (default to undefined)
let updateChatRequest: UpdateChatRequest; //

const { status, data } = await apiInstance.updateChat(
    chatId,
    updateChatRequest
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **updateChatRequest** | **UpdateChatRequest**|  | |
| **chatId** | [**string**] |  | defaults to undefined|


### Return type

**Chat**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: */*, application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**404** | Not Found |  -  |
|**200** | OK |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

