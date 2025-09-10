# ErrorResponse


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**typeMessageCode** | **string** |  | [optional] [default to undefined]
**detailMessageArguments** | **Array&lt;any&gt;** |  | [optional] [default to undefined]
**titleMessageCode** | **string** |  | [optional] [default to undefined]
**detailMessageCode** | **string** |  | [optional] [default to undefined]
**headers** | [**ErrorResponseHeaders**](ErrorResponseHeaders.md) |  | [optional] [default to undefined]
**statusCode** | [**ErrorResponseStatusCode**](ErrorResponseStatusCode.md) |  | [optional] [default to undefined]
**body** | [**ProblemDetail**](ProblemDetail.md) |  | [optional] [default to undefined]

## Example

```typescript
import { ErrorResponse } from './api';

const instance: ErrorResponse = {
    typeMessageCode,
    detailMessageArguments,
    titleMessageCode,
    detailMessageCode,
    headers,
    statusCode,
    body,
};
```

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)
