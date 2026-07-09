
Visually I want it looks "pill alike" , it is acceptable if it takes up to "1.5 row". Matching size of user  bubble (not exactly, but to match optical weight)

I want it to be compact . with minimal information e.g. 

```
========================================================================
|  (type)   head line                    [action icon1] [action icon1] |
========================================================================
```

## (type)
UX  round pill with type avatar 
	- SQL - sql artifact 
	 - DQ metadata  - data quality facet (by facet type)
	 - C - concept - concept by facet type 
	 - AI - ai category meta
	 - M - metadata , fallback meta
Text for metadata should be implemented as single ffunction or class , which definies mapping facet => text . and trying to match to first match otherwise returning M , 
This logic should be documented and easy to extend. 

## (head line)
Very Short string slug , truncated name . some non destructive text

## acion icons 

No text, only icons . If action is overwridden by copilot configuration it must not be visible 
e.g. if copilot sets "auto apply" - "apply" action is irrelevant and can be hidden , same with 
"apply run" - for auto apply and run 

This should be hidden such that it provides more space for (head line) 

Those actioons which is non related to copilot config ,. must be always shown (depends on type)

## expand view 
like how it looks now , keep it same . and show when user clicks (Type) or (head line) - may be add hover effect to both elements 