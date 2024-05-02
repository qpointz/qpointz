import React from 'react';
import ReactDOM from 'react-dom/client';
import './index.css';
import App from './App';
import reportWebVitals from './reportWebVitals';
import {createBrowserRouter, RouterProvider} from "react-router-dom";

const router = createBrowserRouter([
    {index: true, path:"/", element: App("home")},
    {path: 'data', element: App("data"), children: [
            {index: true, path: "sources/", element: App("data/sources") },
            {path: "sources/:id", element: App("data/sources/id") }
        ]},
    {path: 'analysis', element: App("analysis")},
    {path: 'connect', element: App("connect")}
]);

const root = ReactDOM.createRoot(document.getElementById('root'));
root.render(
  <React.StrictMode>
      <RouterProvider router={router} />
  </React.StrictMode>
);

// If you want to start measuring performance in your app, pass a function
// to log results (for example: reportWebVitals(console.log))
// or send to an analytics endpoint. Learn more: https://bit.ly/CRA-vitals
reportWebVitals();
