import {Box, Menu, NavLink, ScrollArea, UnstyledButton, useMantineTheme, Button, Text} from "@mantine/core";
import {useMediaQuery} from "@mantine/hooks";
import {TbDotsVertical, TbStar, TbStarFilled, TbTrash, TbLayoutSidebarRightExpand, TbLayoutSidebarLeftExpand, TbCirclePlus} from "react-icons/tb";
import {Link} from "react-router";
import {useState} from "react";
import type {Chat} from "../../api/mill";
import {useChatContext} from "./ChatProvider.tsx";
import TopNavigation from "../data-model/components/TopNavigation";
import SidebarNavSection from "../data-model/components/SidebarNavSection";


export function ChatList() {
    const theme = useMantineTheme();
    const {chats} = useChatContext();
    const [isCollapsed, setIsCollapsed] = useState(false);
    const isMobile = useMediaQuery('(max-width: 768px)');
        
    // Auto-collapse on mobile only, manual toggle on tablet and desktop
    const shouldCollapse = isMobile || isCollapsed;
    
    
    const linkMenu = (chat:Chat) => {
        return (
            <Menu shadow="md" width={200} position="right-start">
                <Menu.Target>
                    <UnstyledButton w={20} h={20}><TbDotsVertical/></UnstyledButton>
                </Menu.Target>

                <Menu.Dropdown>
                    {chat.isFavorite
                        ? (<Menu.Item onClick={()=> chats.unFavorite(chat)} leftSection={<TbStar size={14}/>}>Unmark favorite</Menu.Item>)
                        : (<Menu.Item onClick={()=>chats.favorite(chat)} leftSection={<TbStarFilled size={14}/>}>Mark favorite</Menu.Item>)
                    }
                    <Menu.Item onClick={()=> chats.delete(chat)} leftSection={<TbTrash color="red" size={14}/>}>Delete</Menu.Item>
                </Menu.Dropdown>
            </Menu>
        )
    }

    return (
        <Box 
            w={shouldCollapse ? 50 : 350} 
            style={{ 
                height: "100vh", 
                boxSizing: "border-box", 
                borderRight: `1px solid ${theme.colors.gray[3]}`,
                transition: 'width 0.3s ease, border 0.3s ease',
                overflow: 'hidden',
                position: 'relative'
            }}
        >
            {/* Toggle Button - only show on tablet and larger */}
            {!isMobile && (
                <Button
                    variant="subtle"
                    size="xs"
                    pos="absolute"
                    top={10}
                    right={shouldCollapse ? 0 : -5}
                    onClick={() => setIsCollapsed(!isCollapsed)}
                    style={{ 
                        transition: 'right 0.3s ease',
                        zIndex: 10
                    }}
                >
                    {shouldCollapse ? <TbLayoutSidebarLeftExpand size={20} /> : <TbLayoutSidebarRightExpand size={20} />}
                </Button>
            )}

            <ScrollArea type="hover" style={{ minHeight: "100%", height: "100vh" }}>
                <Box m={0} pl={shouldCollapse ? 5 : 10} pr={shouldCollapse ? 5 : 10} pt={40} style={{ width: "100%", boxSizing: "border-box" }}>
                    
                    {/* Top Navigation */}
                    <TopNavigation collapsed={shouldCollapse} />
                    
                    {!shouldCollapse && (
                        <SidebarNavSection collapsed={shouldCollapse}>
                            {/* New Chat Button */}
                            <Box mt={15}>
                                <Box p={1} mb={8} bg="transparent" style={{borderRadius: 6}}>
                                    <NavLink 
                                        to="/chat" 
                                        component={Link} 
                                        label="New chat" 
                                        p={0} 
                                        m={0} 
                                        leftSection={<TbCirclePlus size={20} />}
                                    />
                                </Box>
                                
                                <Text mt={15} size="sm" color="gray.5">Chats</Text>

                                {chats.list.map((chat: Chat) => (
                                    <Box m={0}mt={6} p={0} bg={ chats.activeId === chat.id ? "gray.3" : "transparent"} style={{borderRadius: 6}} key={chat.id}>
                                        <NavLink
                                            to={`/chat/${chat.id}`}
                                            key={chat.id}
                                            component={Link}
                                            label={chat.name}
                                            p={3} m={0}
                                            leftSection={chat.isFavorite ? <TbStar size={14} color={theme.colors.yellow[6]} /> : <></>}
                                            rightSection={linkMenu(chat)}
                                            style={{
                                                borderRadius: 8,
                                                marginBottom: 4,
                                                background: chats.activeId === String(chat.id) ? theme.colors.blue[0] : undefined,
                                                transition: 'background 0.2s',
                                            }}
                                            onMouseEnter={e => e.currentTarget.style.background = theme.colors.gray[1]}
                                            onMouseLeave={e => e.currentTarget.style.background = chats.activeId === String(chat.id) ? theme.colors.blue[0] : ''}
                                        />
                                    </Box>
                                ))}
                            </Box>
                        </SidebarNavSection>
                    )}
                </Box>
            </ScrollArea>
        </Box>
    );
}