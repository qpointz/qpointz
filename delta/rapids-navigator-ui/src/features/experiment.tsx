import {createSlice, isAnyOf} from "@reduxjs/toolkit";
import {experimentApi} from "./experimentApi";

export const experimentSlice = createSlice({
    name: 'experiment',
    initialState: {
        modelName: "",
        latestModel : {},
        model : {
            dirty : false,
            code : "",
            data : {},
            ok : true,
            message: ""
        }

    },
    reducers : {
        setModel: (state, action) => {
              if(state.modelName!=action.payload) {
                  state.modelName = action.payload;
              }
        },
        revertChanges: (state) => {
            state.model = {
                dirty: false,code : JSON.stringify(state.latestModel, null, 2), data: state.latestModel, ok:true, message:""
            }
        },
        updateCode: (state, action) => {
            const newCode = action.payload;
            try {
                state.model  = {
                    dirty : true,
                    code: newCode, data: JSON.parse(newCode),
                    ok: true, message: ""
                }
            } catch (err: any | null) {
                state.model  = {
                    dirty : true,
                    code: newCode, data: {},
                    ok: false, message: String(err)
                }
            }
        }
    },
    extraReducers : (builder) => {
        builder.addMatcher(
            isAnyOf(experimentApi.endpoints.getExperimentByName.matchFulfilled),
            (state, action) => {
                state.latestModel = action.payload;
                state.modelName = action.meta.arg.originalArgs;
                state.model = {
                    dirty: false, code : JSON.stringify(action.payload ?? {}, null, 2), data: action.payload, ok:true, message:""
                }
            }
        )
    }
})

export const {updateCode, setModel, revertChanges} = experimentSlice.actions
export default experimentSlice.reducer