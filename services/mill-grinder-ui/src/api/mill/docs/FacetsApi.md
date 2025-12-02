# FacetsApi

All URIs are relative to *http://localhost:8080*

|Method | HTTP request | Description|
|------------- | ------------- | -------------|
|[**getFacetByScope**](#getfacetbyscope) | **GET** /api/metadata/v1/facets/entities/{entityId}/types/{facetType}/scopes/{scope} | Get facet by scope|
|[**getMergedFacet**](#getmergedfacet) | **GET** /api/metadata/v1/facets/entities/{entityId}/types/{facetType} | Get merged facet|

# **getFacetByScope**
> FacetDto getFacetByScope()

Retrieves a facet for a specific scope (e.g., global, user:alice@company.com, team:engineering)

### Example

```typescript
import {
    FacetsApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new FacetsApi(configuration);

let entityId: string; //Entity ID (default to undefined)
let facetType: string; //Facet type (default to undefined)
let scope: string; //Scope (e.g., global, user:alice@company.com) (default to undefined)

const { status, data } = await apiInstance.getFacetByScope(
    entityId,
    facetType,
    scope
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **entityId** | [**string**] | Entity ID | defaults to undefined|
| **facetType** | [**string**] | Facet type | defaults to undefined|
| **scope** | [**string**] | Scope (e.g., global, user:alice@company.com) | defaults to undefined|


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

# **getMergedFacet**
> FacetDto getMergedFacet()

Retrieves a facet merged from multiple scopes (user > team > role > global) for the specified user context

### Example

```typescript
import {
    FacetsApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new FacetsApi(configuration);

let entityId: string; //Entity ID (default to undefined)
let facetType: string; //Facet type (default to undefined)
let userId: string; //User ID for user-scoped facets (optional) (default to undefined)
let teams: Array<string>; //List of team names for team-scoped facets (optional) (default to undefined)
let roles: Array<string>; //List of role names for role-scoped facets (optional) (default to undefined)

const { status, data } = await apiInstance.getMergedFacet(
    entityId,
    facetType,
    userId,
    teams,
    roles
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **entityId** | [**string**] | Entity ID | defaults to undefined|
| **facetType** | [**string**] | Facet type | defaults to undefined|
| **userId** | [**string**] | User ID for user-scoped facets | (optional) defaults to undefined|
| **teams** | **Array&lt;string&gt;** | List of team names for team-scoped facets | (optional) defaults to undefined|
| **roles** | **Array&lt;string&gt;** | List of role names for role-scoped facets | (optional) defaults to undefined|


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
|**200** | Merged facet found |  -  |
|**404** | Entity or facet not found |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

