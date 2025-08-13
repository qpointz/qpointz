import {useParams} from "react-router";
import React, {useEffect, useState} from "react";
import {Alert, Button, Col, Row} from "antd";
import {Editor} from "@monaco-editor/react";
import {useGetExperimentByNameQuery} from "../features/experimentApi";
import {useDispatch, useSelector} from "react-redux";
import {revertChanges, updateCode} from "../features/experiment";

interface ModelItem {
    type:string
    title:string
}

export function ExperimentList() {
    return(
        <>
            Experiment Overview
        </>
    )
}

interface ModelModel {
    ok: boolean,
    message: string
}

export function Experiment() {
    const dispatch = useDispatch()
    let params = useParams()["*"];
    let modelName = String(params?.substring(0, params?.lastIndexOf(".")));
    const refetch = useGetExperimentByNameQuery(modelName).refetch;
    const [model, loadedModelName] = useSelector((state:any) => [state.experiment.model, state.experiment.modelName]);

    useEffect(() => {
        refetch()
    }, [modelName]);

    const asComp = (model:ModelItem, idx:number) => {

    }
    const modelList = model?.data?.models?.map((k:ModelItem, index:number)=> {
        return (
            <Row id={`comp${index}`} key={`comp${index}`}>
                <Col span={22}>{k.title}</Col>
                <Col span={2}>#{index}</Col>
            </Row>
        )
    });

    return(
        <>
            <Row>
                <Col span={24}><h1>Experiment: {params}</h1>
                    <Button disabled={!model.dirty} onClick={e=> dispatch(revertChanges())}>revert</Button>
                    <Button disabled={!model.ok}>save</Button>
                </Col>
            </Row>
            <Row>
                <Col span={8}>
                    <Editor height={"90vh"}
                            defaultLanguage={"json"}
                            value={model?.code}
                            onChange={(code: string | undefined)=> dispatch(updateCode(code)) }
                    />
                </Col>
                <Col span={16}>
                    <h2>{model?.data?.name ?? loadedModelName}</h2>
                    <ErrorMessage model={model}/>
                    {modelList}
                </Col>
            </Row>
        </>
    );
}

function ErrorMessage(model:any) {
    if (model.model.ok!=true) {
        return(<Alert type={"error"} message={model.model.message}></Alert>);
    } else {
        return(<div></div>);
    }
}
