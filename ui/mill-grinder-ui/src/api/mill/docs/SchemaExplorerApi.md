# SchemaExplorerApi

All URIs are relative to *http://localhost:8080*

|Method | HTTP request | Description|
|------------- | ------------- | -------------|
|[**getLineage**](#getlineage) | **GET** /api/metadata/v1/explorer/lineage | Get data lineage|
|[**getTree**](#gettree) | **GET** /api/metadata/v1/explorer/tree | Get schema tree|
|[**search**](#search) | **GET** /api/metadata/v1/explorer/search | Search metadata|

# **getLineage**
> { [key: string]: any; } getLineage()

Retrieves data lineage information for a table, showing upstream and downstream dependencies

### Example

```typescript
import {
    SchemaExplorerApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new SchemaExplorerApi(configuration);

let table: string; //Table fully qualified name (default to undefined)
let depth: number; //Lineage traversal depth (default: 1) (optional) (default to 1)

const { status, data } = await apiInstance.getLineage(
    table,
    depth
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **table** | [**string**] | Table fully qualified name | defaults to undefined|
| **depth** | [**number**] | Lineage traversal depth (default: 1) | (optional) defaults to 1|


### Return type

**{ [key: string]: any; }**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: */*


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | Lineage information |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **getTree**
> TreeNodeDto getTree()

Retrieves a hierarchical tree structure of schemas and tables, optionally filtered by schema

### Example

```typescript
import {
    SchemaExplorerApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new SchemaExplorerApi(configuration);

let schema: string; //Filter by schema name (optional) (default to undefined)
let scope: string; //Scope for facet merging (default: global) (optional) (default to 'global')

const { status, data } = await apiInstance.getTree(
    schema,
    scope
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **schema** | [**string**] | Filter by schema name | (optional) defaults to undefined|
| **scope** | [**string**] | Scope for facet merging (default: global) | (optional) defaults to 'global'|


### Return type

**TreeNodeDto**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: */*


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | Tree structure |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **search**
> SearchResultDto search()

Searches metadata entities by query string, optionally filtered by type

### Example

```typescript
import {
    SchemaExplorerApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new SchemaExplorerApi(configuration);

let q: string; //Search query string (default to undefined)
let type: string; //Filter by metadata type (e.g., TABLE, ATTRIBUTE) (optional) (default to undefined)
let scope: string; //Scope for facet merging (default: global) (optional) (default to 'global')

const { status, data } = await apiInstance.search(
    q,
    type,
    scope
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **q** | [**string**] | Search query string | defaults to undefined|
| **type** | [**string**] | Filter by metadata type (e.g., TABLE, ATTRIBUTE) | (optional) defaults to undefined|
| **scope** | [**string**] | Scope for facet merging (default: global) | (optional) defaults to 'global'|


### Return type

**SearchResultDto**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: */*


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | Search results |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

