# MetadataApi

All URIs are relative to *http://localhost:8080*

|Method | HTTP request | Description|
|------------- | ------------- | -------------|
|[**getAttribute**](#getattribute) | **GET** /api/metadata/v1/schemas/{schema}/tables/{table}/attributes/{attribute} | Get attribute by location|
|[**getEntities**](#getentities) | **GET** /api/metadata/v1/entities | List all entities|
|[**getEntityById**](#getentitybyid) | **GET** /api/metadata/v1/entities/{id} | Get entity by ID|
|[**getFacet**](#getfacet) | **GET** /api/metadata/v1/entities/{id}/facets/{facetType} | Get facet by type|
|[**getFacetScopes**](#getfacetscopes) | **GET** /api/metadata/v1/entities/{id}/facets/{facetType}/scopes | Get facet scopes|
|[**getTable**](#gettable) | **GET** /api/metadata/v1/schemas/{schema}/tables/{table} | Get table by location|

# **getAttribute**
> MetadataEntityDto getAttribute()

Retrieves an attribute (column) metadata entity by its hierarchical location

### Example

```typescript
import {
    MetadataApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new MetadataApi(configuration);

let schema: string; //Schema name (default to undefined)
let table: string; //Table name (default to undefined)
let attribute: string; //Attribute name (default to undefined)
let scope: string; //Scope for facet merging (default: global) (optional) (default to 'global')

const { status, data } = await apiInstance.getAttribute(
    schema,
    table,
    attribute,
    scope
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **schema** | [**string**] | Schema name | defaults to undefined|
| **table** | [**string**] | Table name | defaults to undefined|
| **attribute** | [**string**] | Attribute name | defaults to undefined|
| **scope** | [**string**] | Scope for facet merging (default: global) | (optional) defaults to 'global'|


### Return type

**MetadataEntityDto**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: */*


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | Attribute found |  -  |
|**404** | Attribute not found |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **getEntities**
> MetadataEntityDto getEntities()

Retrieves all metadata entities, optionally filtered by type

### Example

```typescript
import {
    MetadataApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new MetadataApi(configuration);

let type: string; //Filter by metadata type (e.g., TABLE, ATTRIBUTE) (optional) (default to undefined)
let scope: string; //Scope for facet merging (default: global) (optional) (default to 'global')

const { status, data } = await apiInstance.getEntities(
    type,
    scope
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **type** | [**string**] | Filter by metadata type (e.g., TABLE, ATTRIBUTE) | (optional) defaults to undefined|
| **scope** | [**string**] | Scope for facet merging (default: global) | (optional) defaults to 'global'|


### Return type

**MetadataEntityDto**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: */*


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | List of entities |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **getEntityById**
> MetadataEntityDto getEntityById()

Retrieves a metadata entity by its unique identifier with optional scope filtering

### Example

```typescript
import {
    MetadataApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new MetadataApi(configuration);

let id: string; //Entity ID (default to undefined)
let scope: string; //Scope for facet merging (default: global) (optional) (default to 'global')

const { status, data } = await apiInstance.getEntityById(
    id,
    scope
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **id** | [**string**] | Entity ID | defaults to undefined|
| **scope** | [**string**] | Scope for facet merging (default: global) | (optional) defaults to 'global'|


### Return type

**MetadataEntityDto**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: */*


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | Entity found |  -  |
|**404** | Entity not found |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **getFacet**
> FacetDto getFacet()

Retrieves a specific facet for an entity by facet type and scope

### Example

```typescript
import {
    MetadataApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new MetadataApi(configuration);

let id: string; //Entity ID (default to undefined)
let facetType: string; //Facet type (e.g., descriptive, structural, value-mapping) (default to undefined)
let scope: string; //Scope for facet (default: global) (optional) (default to 'global')

const { status, data } = await apiInstance.getFacet(
    id,
    facetType,
    scope
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **id** | [**string**] | Entity ID | defaults to undefined|
| **facetType** | [**string**] | Facet type (e.g., descriptive, structural, value-mapping) | defaults to undefined|
| **scope** | [**string**] | Scope for facet (default: global) | (optional) defaults to 'global'|


### Return type

**FacetDto**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: */*


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | Facet found |  -  |
|**404** | Entity or facet not found |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **getFacetScopes**
> Set<string> getFacetScopes()

Retrieves all available scopes for a specific facet type on an entity

### Example

```typescript
import {
    MetadataApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new MetadataApi(configuration);

let id: string; //Entity ID (default to undefined)
let facetType: string; //Facet type (default to undefined)

const { status, data } = await apiInstance.getFacetScopes(
    id,
    facetType
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **id** | [**string**] | Entity ID | defaults to undefined|
| **facetType** | [**string**] | Facet type | defaults to undefined|


### Return type

**Set<string>**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: */*


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | List of scopes |  -  |
|**404** | Entity not found |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **getTable**
> MetadataEntityDto getTable()

Retrieves a table metadata entity by schema and table name

### Example

```typescript
import {
    MetadataApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new MetadataApi(configuration);

let schema: string; //Schema name (default to undefined)
let table: string; //Table name (default to undefined)
let scope: string; //Scope for facet merging (default: global) (optional) (default to 'global')

const { status, data } = await apiInstance.getTable(
    schema,
    table,
    scope
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **schema** | [**string**] | Schema name | defaults to undefined|
| **table** | [**string**] | Table name | defaults to undefined|
| **scope** | [**string**] | Scope for facet merging (default: global) | (optional) defaults to 'global'|


### Return type

**MetadataEntityDto**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: */*


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | Table found |  -  |
|**404** | Table not found |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

