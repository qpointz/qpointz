
Desired structure for SQL artifact , High level artifact contains following items

## sql
element describes generated sql  and its validation . this obkect contains
- generated sql
- dialect
these all existing items

## schema
object describes result schema , as per story definition. element is also optional and may "occasionaly" appears as side effect of validation (e.g. chart config validation)

## visualizations[]
section to comtain configuration of eleement to visualize results of sql .
Examples:
- charts
- sql query plan visualization
- query lineage visualization
section is optional and

## profiling[]
this section may contain non LLM-generated data , like lineage , execution plan etc. section is also optional.

This is indicative structure to be used to review current layout.

Obviously this will affect protocol and persistence. Biggest gap and conflict i see in the moment
that validate_sql persists whole sql artifact , However in this story we definied validation of chart as well which has validate_Sql as prerequisite.
In current architecture it leads to situation that sql validation for chart makes artifact persisted BEFORE chart configuration validated.
I want you to rethink protocol such that
- only final and completed state of artifact persisted
- there is option to plug "postprocessing" capabilities (for profiling, before persistance)

It is big architectural shift, i agree to introduce breaking changes , no need for deprecation or backward compatibility
