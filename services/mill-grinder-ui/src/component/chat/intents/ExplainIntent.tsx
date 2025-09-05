import {Box, Group, Title} from "@mantine/core";
import { TbSchool } from "react-icons/tb";
import Markdown from "react-markdown";
//import mermaid, {type ParseResult} from "mermaid";
//import {MermaidDiagram} from "@lightenna/react-mermaid-diagram";

function ExplainIntent(data: any) {
    const renderDescription = (description:any) => {
        const blocks:Array<string> = description.split(/```/);
        return blocks.map((block:string, i:number) => {
            if (block.trim().startsWith('mermaid')) {
                //const cnt = block.trim().replace('mermaid', '').trim();
                //const lala = mermaid.parse(cnt)
                //    .then( ()=> (<Box p={20} bg="primary.3"><MermaidDiagram theme="neutral">{cnt}</MermaidDiagram></Box>))
                //    .catch( ()=> (<Alert color="danger" title="Diagram">Failed to draw diagram</Alert>));
                return (<></>)
            } else {
                return (
                    <Markdown key={i}>{block}</Markdown>
                )
            }
        });
    }

    return (
        <Box bg="white" p={12} m={4} style={{borderRadius: 10}} maw={"95%"}>
            <Group><TbSchool/><Title order={4}>Explain</Title></Group>
            {(renderDescription(data?.message?.content?.description || ''))}
        </Box>
    );
}

export default ExplainIntent