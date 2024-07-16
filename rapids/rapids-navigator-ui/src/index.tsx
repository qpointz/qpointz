import React from 'react';
import ReactDOM from 'react-dom/client';
import './index.css';
import App from './App';
import reportWebVitals from './reportWebVitals';
import {createBrowserRouter, createRoutesFromElements, Route, RouterProvider} from 'react-router-dom';
import {Dashboard} from "./layout/Dashboard";
import {ProjectFile} from "./layout/ProjectFile";
import {Experiment, ExperimentList} from "./layout/Experiment";
import store from "./features/store"
import {Provider} from "react-redux";
import {Source, SourcesList} from "./layout/Source";

const container = document.getElementById('root')
const root = ReactDOM.createRoot(container!);
const app = <Provider store={store}><App/></Provider>

const router = createBrowserRouter(createRoutesFromElements(
    <Route path={"/"} element={app}>
           <Route path={`project/sources`} element={<SourcesList/>} />
           <Route path={`project/sources/*`} element={<Source/>} />
           <Route path={`project/experiments`} element={<ExperimentList/>} />
           <Route path={`project/experiments/*`} element={<Experiment/>} />
           <Route path={`project/dashboards/*`} element={<Dashboard/>} />
           <Route path={`project/*`} element={<ProjectFile/>} />
      </Route>
));

root.render(
    <React.StrictMode>
        <RouterProvider router={router} />
    </React.StrictMode>
);

// If you want to start measuring performance in your app, pass a function
// to log results (for example: reportWebVitals(console.log))
// or send to an analytics endpoint. Learn more: https://bit.ly/CRA-vitals
reportWebVitals();
