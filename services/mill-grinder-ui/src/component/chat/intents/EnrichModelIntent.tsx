import {Title, Text, Group, Pill, Tabs, Button, NavLink, Box, Stack} from "@mantine/core";
import {
    TbBlocks,
    TbBucket,
    TbFileTypeSql, TbHierarchy2, TbLabel,
    TbRulerMeasure, TbSpeakerphone, TbTable, TbTarget,
    TbVocabulary
} from "react-icons/tb";
import {Link} from "react-router";
import {format} from "sql-formatter";
import {CodeHighlight} from "@mantine/code-highlight";
import type {ReactElement} from "react";

export default function EnrichModelIntent(data:any = {}) {
    const {content} = data.message;
    const {enrichment} = content || {};

    const enrichments = (enrichment || [])
        .sort((a: any, b: any) => a.type > b.type);

    const enrichmentIcon = (e:any) => {
        const {type} = e;
        switch (type) {
            case 'concept': return (<TbVocabulary size={30}/>);
            case 'model': return (<TbBlocks size={30}/>);
            case 'rule' : return (<TbRulerMeasure size={30}/>)
            case 'relation' : return (<TbHierarchy2 size={30}/> )
        }
    }

    const renderObjectLink = (target:string, location:string, icon:ReactElement) => {
        return (
            <NavLink
                to={location}
                component={Link}
                label={target}
                mt={10}
                p={0} m={0}
                w={400}
                leftSection={<>{icon}</>}  />
        )
    }

    const renderTargetLink = (target:string) => {
        const segments = target.split('.');
        if (segments.length === 1) {
            return renderObjectLink(target, `/data/schema/${segments[0]}`, <TbBucket size={20}/>)
        }

        if (segments.length === 2) {
            return renderObjectLink(target, `/data/schema/${segments[0]}/table/${segments[1]}`, <TbTable size={20}/>)
        }

        if (segments.length === 3) {
            return renderObjectLink(target, `/data/schema/${segments[0]}/table/${segments[1]}/attributes/${segments[2]}`, <TbLabel size={20}/>)
        }

        return (
            <Text>{ JSON.stringify(segments, null, 2) }</Text>
        )
    }

    const renderTarget = (target:string)=> {
        return (
            <Group>
                {renderTargetLink(target)}
            </Group>
        )
    }

    const renderRelation = (relation:any) => {
        return (
            <Group>
                {renderTargetLink(relation.source)}
                <Text>{relation.cardinality}</Text>
                {renderTargetLink(relation.target)}
                <Stack pl={40}>
                    {relation.columns.map ((col:any) =>{
                        const sourcecolumn = `${relation.source}.${col.source}`;
                        const targetcolumn = `${relation.target}.${col.target}`;
                        return (
                            <Group key={sourcecolumn}>
                                {renderTargetLink(sourcecolumn)}
                                <Text>→</Text>
                                {renderTargetLink(targetcolumn)}
                            </Group>
                        )
                    })}
                </Stack>
            </Group>
        )
    }

    const renderEnrichmentDetails = (e:any)=> {
        return (
            <Tabs defaultValue={e.type === 'relation' ? 'relation' : (e.targets ? "targets" : "")} mt={10} mb={10}>
                    <Tabs.List>
                        { (e.targets || e.target) && (<Tabs.Tab value="targets" leftSection={<TbTarget size={12}/>}>
                            Targets
                        </Tabs.Tab>)}

                        {e.type === 'relation' && (<Tabs.Tab value="relation" leftSection={<TbHierarchy2 size={12}/>}>
                            Relation
                        </Tabs.Tab>) }

                        {e.sql && (<Tabs.Tab value="sql" leftSection={<TbFileTypeSql size={12}/>}>
                            SQL
                        </Tabs.Tab>)}

                    </Tabs.List>
                    {(e.targets || e.target) && (<Tabs.Panel value="targets">
                        {e.targets && e.targets.map((target:any) => renderTarget(target))}
                        {e.target && [e.target].map((target:any) => renderTarget(target))}
                    </Tabs.Panel>)}

                    {e.type === 'relation' && (<Tabs.Panel value="relation">
                        {e.relation.map((rel:any) => renderRelation(rel))}
                    </Tabs.Panel>)}

                    {e.sql && (<Tabs.Panel value="sql">
                        <CodeHighlight code={format(e.sql)} language="sql" expandCodeLabel=""/>
                    </Tabs.Panel>)}
            </Tabs>
        )}

    const renderTags = (tags:string[]) => {
        return tags.map((tag:string) => (<Link to={"/explore/tags/" + tag}><Pill bg="blue.1">{'#' + tag}</Pill></Link>));
    }

    const renderEnrichment = (e:any) => {
        const tags = e.tags || [];
        return (
            <>
                <Box bg="white" p={20} m={10} maw={"95%"} >
                    <Group>
                        {(enrichmentIcon(e))}
                        <Title order={5}>{e.type}</Title>
                        <Text fs="italic">{e.description}</Text>
                    </Group>
                    <Group mt={12}>
                        { e.category && (<Group><Text c="primary.4" size="xs">Category:</Text><Link to={"/explore/category/" + e.category}><Pill bg="success.1">{ e.category }</Pill></Link></Group>)}
                        { (tags && tags.length>0) && <Group><Text c="primary.4" size="xs">Tags:</Text>{renderTags(tags)}</Group> }
                    </Group>
                    { renderEnrichmentDetails(e)}                    
                </Box>
                <Group mt={12} justify="flex-end">
                    <Button variant="outline" size="xs" color="danger">Reject</Button>
                    <Button variant="outline" size="xs"color="success">Accept</Button>
                    <Button variant="outline" size="xs">Promote</Button>
                </Group>
            </>
        );
    }

    return (
        <>
            <Box bg="white" p={20} m={10} key={data.message.id} maw={"70%"} style={{borderRadius: 10}}> 
                <Group><TbSpeakerphone/><Title order={4}>Enrichment</Title></Group>
                { enrichments.map((e:any) => (renderEnrichment(e))) }
            </Box>

        </>
    )
}
