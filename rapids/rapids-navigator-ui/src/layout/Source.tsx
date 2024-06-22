import {useDispatch, useSelector} from "react-redux";
import {sourcesApi, useGetModelByNameQuery, useSaveModelMutation} from "../features/source";
import {useParams} from "react-router";
import React, {useEffect} from "react";
import {Button, Col, Row} from "antd";
import {revertChanges, updateCode} from "../features/experiment";
import {Editor} from "@monaco-editor/react";

export function SourcesList() {
    return (<>Sources List</>)
}

export function Source() {
    const dispatch = useDispatch()
    let params = useParams()["*"];
    let modelName: string = String(params?.substring(0, params?.lastIndexOf(".")));
    const refetch = useGetModelByNameQuery(modelName).refetch;
    const [model, loadedModelName] = useSelector((state:any) => [state.sources.model, state.sources.modelName]);
    const [code ] = useSelector((state:any) => state.sources.model?.code)
    const [saveModel] = useSaveModelMutation();

    useEffect(() => {
        refetch()
    }, [modelName]);

    return(
        <>
            <Row>
                <Col span={24}><h1>Source: {params}</h1>
                    <Button disabled={!model.dirty} onClick={e=> dispatch(revertChanges())}>revert</Button>
                    <Button disabled={!model.ok}
                            onClick={(ew) => {
                                return (
                                    saveModel({key:modelName, data:code})
                                )
                            }}>
                        save
                    </Button>
                </Col>
            </Row>
            <Row>
                <Col span={8}>
                    <Editor key={'editor'}
                            height={"90vh"}
                            defaultLanguage={"json"}
                            value={code}
                            onChange={(code: string | undefined)=> dispatch(updateCode(code)) }
                    />
                </Col>
                <Col span={16}>
                    <h2>{model?.data?.name ?? loadedModelName}</h2>
                </Col>
            </Row>
        </>
    );
}