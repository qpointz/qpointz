import React from 'react'
import {Col, Row, Layout, Menu, PageHeader, Divider, Card} from 'antd';
import * as aicons from '@ant-design/icons';
import {FontAwesomeIcon} from '@fortawesome/react-fontawesome';
import {BrowserRouter as Router} from 'react-router-dom';
const {Header, Content, Sider} = Layout

const { SubMenu } = Menu;

const routes = [
    {
        path: 'index',
        breadcrumbName: 'First-level Menu',
    },
    {
        path: 'first',
        breadcrumbName: 'Second-level Menu',
    },
    {
        path: 'second',
        breadcrumbName: 'Third-level Menu',
    },
];

export const Project = (props) => {
    return (
        <Row gutter={[0,0]}>
            <Col span={24}>
                <PageHeader
                    title="Title"
                    style={{borderBottom:'1px solid #f0f0f0'}}
                    breadcrumb={{ routes }}
                    subTitle="This is a subtitle"
                     footer={"klklk"}
                />
                <Divider></Divider>

            </Col>
            <Col span={24}>
                <Layout style={{padding:'12px',backgroundColor:'transparent'}}>
                    <Sider width={200}  style={{ backgroundColor: 'transparent' }}>
                        <Card>
                            <Menu
                                mode="inline"
                                multiple={false}
                                defaultSelectedKeys={['1']}
                                defaultOpenKeys={['sub1']}
                                style={{ backgroundColor: 'transparent' }}
                            >
                                <SubMenu key="sub1" icon={<aicons.UserAddOutlined/>} title="subnav 1">
                                    <Menu.Item key="1">option1</Menu.Item>
                                    <Menu.Item key="2">option2</Menu.Item>
                                    <Menu.Item key="3">option3</Menu.Item>
                                    <Menu.Item key="4">option4</Menu.Item>
                                </SubMenu>
                                <SubMenu key="sub2" icon={<aicons.LaptopOutlined/>} title="subnav 2">
                                    <Menu.Item key="5">option5</Menu.Item>
                                    <Menu.Item key="6">option6</Menu.Item>
                                    <Menu.Item key="7">option7</Menu.Item>
                                    <Menu.Item key="8">option8</Menu.Item>
                                </SubMenu>
                                <SubMenu key="sub3" icon={<FontAwesomeIcon icon="coffee"/>} title="subnav 3">
                                    <Menu.Item icon={<FontAwesomeIcon icon="coffee" />} key="9">option9</Menu.Item>
                                    <Menu.Item key="10">option10</Menu.Item>
                                    <Menu.Item key="11">option11</Menu.Item>
                                    <Menu.Item key="12">option12</Menu.Item>
                                </SubMenu>
                            </Menu>
                        </Card>
                    </Sider>
                    <Content style={{ padding: '0 24px', minHeight: 280 }}>
                        <Card>content</Card>
                    </Content>
                </Layout>
            </Col>
        </Row>

    )
}
