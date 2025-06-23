import {Title, Text, Card, Group, Pill, Tabs, Button, NavLink, Box} from "@mantine/core";
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

    const renderTarget = (target:string)=> {
        const segments = target.split('.');
        if (segments.length === 1) {
            return (
                <Group>
                    <NavLink
                        to={`/data/schema/${segments[0]}`}
                        component={Link}
                        label={target}
                        mt={10}
                        p={0} m={0}
                        leftSection={<><TbBucket size={20}/><Title order={6}>Schema:</Title></>}  />
                </Group>
            )
        }

        if (segments.length === 2) {
            return (
                <Group>
                    <NavLink
                        to={`/data/schema/${segments[0]}/table/${segments[1]}`}
                        component={Link}
                        label={target}
                        mt={10}
                        p={0} m={0}
                        leftSection={<><TbTable size={20}/><Title order={6}>Table:</Title></>} />
                </Group>
            )
        }

        if (segments.length === 3) {
            return (
                <Group>
                    <NavLink
                        to={`/data/schema/${segments[0]}/table/${segments[1]}`}
                        component={Link}
                        label={target}
                        mt={10}
                        p={0} m={0}
                        leftSection={<><TbLabel size={20}/><Title order={6}>Table:</Title></>} />
                </Group>
            )
        }



        return (
            <Text>{ JSON.stringify(segments, null, 2) }</Text>
        )
    }

    const renderEnrichmentDetails = (e:any)=> {
        return (
            <Tabs defaultValue="targets">
                    <Tabs.List>
                        { (e.targets || e.target) && (<Tabs.Tab value="targets" leftSection={<TbTarget size={12}/>}>
                            Targets
                        </Tabs.Tab>)}
                        {e.sql && (<Tabs.Tab value="sql" leftSection={<TbFileTypeSql size={12}/>}>
                            SQL
                        </Tabs.Tab>)}
                    </Tabs.List>
                    {(e.targets || e.target) && (<Tabs.Panel value="targets">
                        {e.targets && e.targets.map((target:any) => renderTarget(target))}
                        {e.target && [e.target].map((target:any) => renderTarget(target))}
                    </Tabs.Panel>)}


                    {e.sql && (<Tabs.Panel value="sql">
                        <CodeHighlight code={format(e.sql)} language="sql"/>
                        <Text>{e.sql}</Text>
                    </Tabs.Panel>)}
            </Tabs>
        )}

    const renderTags = (tags:string[]) => {
        return tags.map((tag:string) => (<Link to={"/explore/tags/" + tag}><Pill bg="blue.1">{'#' + tag}</Pill></Link>));
    }

    const renderEnrichment = (e:any) => {
        const tags = e.tags || [];
        console.log(e);
        return (
                <Card>
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
                    <Group mt={12} justify="flex-end">
                        <Button variant="outline" color="danger">Reject</Button>
                        <Button variant="outline" color="success">Accept</Button>
                        <Button variant="outline" >Promote</Button>
                    </Group>
                </Card>
        );
    }

    return (
        <>
            <Box bg="white" p={20} m={10} key={data.message.id}>
                <Group><TbSpeakerphone/><Title order={4}>Enrichment</Title></Group>
                { enrichments.map((e:any) => (renderEnrichment(e))) }
            </Box>

        </>
    )
}
