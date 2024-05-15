import { useNavigate } from "react-router";
import {Tree} from "antd";
import {CaretDownOutlined, DownOutlined} from "@ant-design/icons";
import {Link} from "react-router-dom";
import {Component} from "react";
import axios from "axios";
import React from "react";

const treeData = [
    { title: 'pages', key: 'pages', selectable: false },
    { title: 'dashboards', key: 'dashboards', selectable: false},
    {
        title: 'experiments', key: 'experiments', selectable: false, children: [
            {title: "sample.yaml", key: 'analysis/simple.yaml' }
        ]} ,
    { title: 'catalogs', key: 'catalogs', selectable: false}
];

type TreeNavigationState = {
    loaded: boolean,
    treeData: any[]
}

export class TreeNavigation extends React.Component<{}, TreeNavigationState> {

    constructor(p:{}) {
        super(p);
    }

    renderTitle(node:any) {
        return  <Link to={`project/${node.key}`}>{node.title}</Link>
    }

    componentDidMount() {
        axios.get('/api/content')
            .then((response)=> {
               this.setState({loaded:true, treeData: response.data})
            });
    }

    treeData() {
        return this.state?.treeData || [];
    }

    render() {
        return <Tree showLine={false}
              showIcon={false}
              treeData={this.treeData()}
              titleRender={this.renderTitle}
              switcherIcon={<CaretDownOutlined/>}
        />
    }
}

// function TreeNavigation() {
//     const navigate = useNavigate();
//     const renderTitle = (node:any) => {
//         return (
//             <Link to={`edit/${node.key}`}>{node.title}</Link>
//         )
//     }
//     return(
//         <Tree showLine={false}
//               showIcon={false}
//               treeData={treeData}
//               titleRender={renderTitle}
//               switcherIcon={<CaretDownOutlined/>}
//         >
//
//         </Tree>
//     )
// }
//
// export default TreeNavigation;