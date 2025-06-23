# ApplicationDescriptorControllerApi

All URIs are relative to *http://localhost:8080*

|Method | HTTP request | Description|
|------------- | ------------- | -------------|
|[**getInfo**](#getinfo) | **GET** /.well-known/mill | |

# **getInfo**
> ApplicationDescriptor getInfo()


### Example

```typescript
import {
    ApplicationDescriptorControllerApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new ApplicationDescriptorControllerApi(configuration);

const { status, data } = await apiInstance.getInfo();
```

### Parameters
This endpoint does not have any parameters.


### Return type

**ApplicationDescriptor**

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

