import {createApi, fetchBaseQuery} from "@reduxjs/toolkit/query/react";

interface Model {
    key : string,
    data: any
}

export const modelApi = (modelType:string) => {
    const path: any = modelType + "Api";
    return (createApi({
        reducerPath: path,
        baseQuery: fetchBaseQuery(        {baseUrl: '/api/model/'}),
        endpoints: (builder) => ({
            getModelByName: builder.query<any, string>({
                query: (name) => `${modelType}/${name}`,
            }),

            saveModel : builder.mutation<any, Model>({
                query: (model) => ({
                    url: `${modelType}/${model.key}`,
                    method: 'POST',
                    body: model,
                })
            })
        })
    }))
}

export const modelInitialState = (modelType:string) => {return {
    modelName : "",
    latestModel: {},
    model : {
        dirty : false,
        code : "",
        data : {},
        ok : true,
        message: ""
    }
}}

export function modelOf(obj:any | null) {
    return {
        dirty: false,
        code : JSON.stringify(obj ?? {},null, 2),
        data: obj,
        ok:true,
        message:""
    }
}

export function modelParse(code:string , dirty:boolean = true) {
    let data: any, ok: boolean , message: string;
    try {
        data = JSON.parse(code);
        ok = true;
        message = "";
    } catch (err) {
        ok = false;
        message = String(err)
    }
    return {
        dirty: dirty,
        code : code,
        data: data,
        ok:ok,
        message:message
    }
}


