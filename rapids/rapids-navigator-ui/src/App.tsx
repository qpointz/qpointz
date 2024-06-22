import React from 'react';
import logo from './logo.svg';
import './App.css';
import {Layout, Menu} from "antd";
import {Outlet} from "react-router";
import {TreeNavigation} from "./layout/TreeNavigation";
const {Content, Footer, Sider } = Layout;

function App() {
  return (
      <Layout>
        <Sider style={{backgroundColor:'#ccccaa'}}>
          <TreeNavigation/>
        </Sider>
        <Content style={{ padding: '0 24px', minHeight: 280 }}>
          <Outlet/>
        </Content>
      </Layout>
  );
}

export default App;
