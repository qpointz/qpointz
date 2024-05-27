import {createApi, fetchBaseQuery} from "@reduxjs/toolkit/query/react";

export const experimentApi = createApi({
    reducerPath: 'experimentApi',
    baseQuery: fetchBaseQuery(        {baseUrl: '/api/model/'}),
    endpoints: (builder) => ({
        getExperimentByName: builder.query<any, string>({
            query: (name) => `experiments/${name}`,
        })
    })
})

export const {useGetExperimentByNameQuery} = experimentApi
