import {useParams} from "react-router";
import React, {useEffect} from "react";

export function Experiment() {
    let params = useParams();

    useEffect(()=> {
        console.log("mounted");
        return function() {
            console.log("unmounted");
        }
    },[]);

    return(
        <div>Experiment</div>
    );
}
