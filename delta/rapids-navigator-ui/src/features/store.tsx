import {configureStore} from "@reduxjs/toolkit";
import experimentReducer from "./experiment";
import {experimentApi} from "./experimentApi";
import {sourcesApi, sourcesReducer} from "./source";


const middleware:any = [
    experimentApi.middleware,
    sourcesApi.middleware];

export default configureStore({
    reducer: {
        experiment: experimentReducer,
        [experimentApi.reducerPath] : experimentApi.reducer,

        sources: sourcesReducer,
        [sourcesApi.reducerPath] : sourcesApi.reducer
    },
    middleware : (getDefaultMiddleware) =>
        getDefaultMiddleware({})
            .concat(middleware)

})