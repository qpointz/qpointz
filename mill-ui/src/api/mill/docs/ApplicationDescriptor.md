# ApplicationDescriptor


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**services** | [**Array&lt;ServiceDescriptor&gt;**](ServiceDescriptor.md) |  | [optional] [default to undefined]
**security** | [**SecurityDescriptor**](SecurityDescriptor.md) |  | [optional] [default to undefined]
**schemas** | [**{ [key: string]: SchemaDescriptor; }**](SchemaDescriptor.md) |  | [optional] [default to undefined]

## Example

```typescript
import { ApplicationDescriptor } from './api';

const instance: ApplicationDescriptor = {
    services,
    security,
    schemas,
};
```

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)
