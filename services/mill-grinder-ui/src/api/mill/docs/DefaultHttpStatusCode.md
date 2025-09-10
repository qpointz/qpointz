# DefaultHttpStatusCode


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**error** | **boolean** |  | [optional] [default to undefined]
**is5xxServerError** | **boolean** |  | [optional] [default to undefined]
**is4xxClientError** | **boolean** |  | [optional] [default to undefined]
**is2xxSuccessful** | **boolean** |  | [optional] [default to undefined]
**is3xxRedirection** | **boolean** |  | [optional] [default to undefined]
**is1xxInformational** | **boolean** |  | [optional] [default to undefined]

## Example

```typescript
import { DefaultHttpStatusCode } from './api';

const instance: DefaultHttpStatusCode = {
    error,
    is5xxServerError,
    is4xxClientError,
    is2xxSuccessful,
    is3xxRedirection,
    is1xxInformational,
};
```

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)
