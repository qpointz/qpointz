import {createAction, createReducer} from '@reduxjs/toolkit';

export const AddStep = createAction('recipe/add-step')
export const RemoveStep = createAction('recipe/remove-step')
export const SelectStep = createAction('recipe/select-step')

const initialState = {}

export const reducer = (action, state) => createReducer(initialState, {
    [AddStep]: s => addStep(action,s),
    [RemoveStep] : s=> removeStep(action, s),
    [SelectStep] : s=> selectStep(action, s)
})

function addStep(a,s) {
    return s;
}

function removeStep(a,s) {
    return s;
}

function selectStep(a,s) {
    return s;
}
