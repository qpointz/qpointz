import {modelApi, modelInitialState, modelOf, modelParse} from "./modelApi";
import {createSlice, isAnyOf} from "@reduxjs/toolkit";

const sliceName:string = "sources"

export const sourcesApi = modelApi(sliceName);

export const sourcesSlice = createSlice({
    name: sliceName,
    initialState: modelInitialState(sliceName),
    reducers : {
        revertChanges: (state) => {
            state.model = modelOf(state.latestModel);
        },
        updateCode: (state, action) => {
            state.model = modelParse(action.payload)
        }
    },
    extraReducers : (builder) => {
        builder.addMatcher(
            isAnyOf(sourcesApi.endpoints.getModelByName.matchFulfilled),
            (state, action) => {
                state.latestModel = action.payload;
                state.modelName = action.meta.arg.originalArgs;
                state.model = modelOf(action.payload);
            }
        )
    }
})

export const {updateCode, revertChanges} = sourcesSlice.actions
export const sourcesReducer = sourcesSlice.reducer
export const {useGetModelByNameQuery, useSaveModelMutation} = sourcesApi