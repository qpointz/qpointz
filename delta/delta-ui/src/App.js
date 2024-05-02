import './App.css';
import {Layout, Menu} from "antd";
import {Outlet} from "react-router";

const { Header, Content, Footer, Sider } = Layout;

const items1= [
    {key: "home" , label: (<a href={"/"}><b>Home</b></a>) },
    {key: "data" , label: (<a href={"/data"}><b>Data</b></a>) },
    {key: "connect" , label: (<a href={"/connect"}><b>Connect</b></a>) },
    {key: "analysis" , label: (<a href={"/analysis"}><b>Analysis</b></a>) },
];

function App(section) {
  return (
    <Layout>
        <Header style={{ display: 'flex', alignItems: 'center', 'background-color': '#aaaaaa', height: '30px' }}>
            <Menu
                theme="none"
                mode="horizontal"
                defaultSelectedKeys={['2']}
                items={items1}
                style={{ flex: 1, minWidth: 0 }}
            >
            </Menu>
        </Header>
        <Content style={{ padding: '12px 12px 12px 12px' }}>
            <Layout>
                <Sider style={{'background-color':'#ccccaa'}}>Sider:{section}</Sider>
                <Content style={{ padding: '0 24px', minHeight: 280 }}>Content:{section}
                <Outlet/>
                </Content>
            </Layout>
        </Content>
        <Footer style={{ textAlign: 'left', height:'20px', 'background-color':'#555555', color: '#999999' }}>
            QP ©{new Date().getFullYear()} Created by Ant UED
        </Footer>
    </Layout>
  );
}

export default App;

