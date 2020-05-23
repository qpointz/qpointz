import React from 'react';
import './App.less';
import {Breadcrumb, Col, Layout, Menu, Row} from 'antd';
import * as aicons from '@ant-design/icons'

import {
    Switch,
    Route,
    BrowserRouter as Router} from 'react-router-dom'
import {Home} from './components/home'
import {Project} from './components/project'
import {FontAwesomeIcon} from '@fortawesome/react-fontawesome';

const { SubMenu } = Menu;
const {Header, Content, Footer, Sider} = Layout

const App = () => (
    <>
        <Row id={'page-header'}>
            <Col span={24}>
                <div className="logo" />
                <Menu theme="dark" mode="horizontal" defaultSelectedKeys={['2']}>
                    <Menu.Item key="1">nav 1</Menu.Item>
                    <Menu.Item key="2">nav 2</Menu.Item>
                    <Menu.Item key="3">nav 3</Menu.Item>
                </Menu>
            </Col>
        </Row>
        <Row id={'page-content'}>
            <Col span={24}>
                 <Router>
                    <Switch>
                        <Route path={"/project/"}>
                            <Project/>
                        </Route>
                        <Route path={"/"}>
                            <Home/>
                        </Route>
                    </Switch>
                </Router>
            </Col>
        </Row>
        <Row id={'page-footer'}>
            <Col span={24}>Footer content</Col>
        </Row>
    </>
);

export default App;

