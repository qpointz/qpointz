import {combineReducers} from 'redux';
import * as recipe from './recipe'

export const reducer = combineReducers({
    recipe : recipe.reducer
})
