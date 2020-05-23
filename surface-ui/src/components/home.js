import React from 'react'
import {Layout} from 'antd';
const {Content} = Layout

export const Home = (props) => {
    return (
        <Layout style={{height:"100%"}}>
            <Content style={{backgroundColor:'#fafafa'}}>
                HOME Content
            </Content>
        </Layout>
    )
}
