import {useReactTable, flexRender, createColumnHelper, getCoreRowModel} from "@tanstack/react-table";
import {ScrollArea, Table} from "@mantine/core";

export default function DataContainer(input: any) {
    const container = input.data || {} ;
    const data = container.data ?? [];
    const fields = container.fields ?? [];

    const columnHelper = createColumnHelper<Array<any>>();
    const columns = fields.map((field: string, idx: number) => {
        return columnHelper.accessor(row => row[idx], {
            id: field,
            header: field,
            cell: info => info.renderValue()
        });
    });

    const table = useReactTable({
        data,
        columns,
        getCoreRowModel: getCoreRowModel(),
    })

    return (
        <ScrollArea scrollbars="xy" type={"auto"} offsetScrollbars={true} scrollbarSize={15} mah={"80%"} >
            <Table>
                <Table.Thead>
                    {table.getHeaderGroups().map(headerGroup => (
                        <Table.Tr key={headerGroup.id}>
                            {headerGroup.headers.map(header => (
                                <Table.Th key={header.column.columnDef.id} style={{position: 'sticky', top: 0, background: 'white', zIndex: 1}}>
                                    {header.isPlaceholder
                                        ? null
                                        : flexRender(
                                            header.column.columnDef.header,
                                            header.getContext()
                                        )}
                                </Table.Th>
                            ))}
                        </Table.Tr>
                    ))}
                </Table.Thead>
                <Table.Tbody>
                    {table.getRowModel().rows.map(row => (
                        <Table.Tr key={row.id}>
                            {row.getVisibleCells().map(cell => (
                                <Table.Td key={cell.column.columnDef.id}>
                                    {flexRender(cell.column.columnDef.cell, cell.getContext())}
                                </Table.Td>
                            ))}
                        </Table.Tr>))}
                </Table.Tbody>
            </Table>
        </ScrollArea>
    )
}