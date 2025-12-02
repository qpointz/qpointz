import {useReactTable, flexRender, createColumnHelper, getCoreRowModel} from "@tanstack/react-table";
import {ScrollArea, Table, Group, Button} from "@mantine/core";
import {TbChevronLeft, TbChevronRight} from "react-icons/tb";

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
        <>
        <ScrollArea
            scrollbars="xy"
            type="auto"
            offsetScrollbars={true}
            scrollbarSize={15}
            mah="80%"
            style={{ width: "100%" }}
        >
            <Table
                style={{
                    tableLayout: "auto",
                    minWidth: "max-content",
                    width: "100%",
                    whiteSpace: "nowrap"
                }}
            >
                <Table.Thead>
                    {table.getHeaderGroups().map(headerGroup => (
                        <Table.Tr key={headerGroup.id}>
                            {headerGroup.headers.map(header => (
                                <Table.Th
                                    key={header.column.columnDef.id}
                                    style={{
                                        position: 'sticky',
                                        top: 0,
                                        background: 'white',
                                        zIndex: 1,
                                        whiteSpace: 'nowrap',
                                        overflow: 'hidden',
                                        textOverflow: 'ellipsis',
                                        maxWidth: 300
                                    }}
                                >
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
                                <Table.Td
                                    key={cell.column.columnDef.id}
                                    style={{
                                        whiteSpace: 'nowrap',
                                        overflow: 'hidden',
                                        textOverflow: 'ellipsis',
                                        maxWidth: 300
                                    }}
                                >
                                    {flexRender(cell.column.columnDef.cell, cell.getContext())}
                                </Table.Td>
                            ))}
                        </Table.Tr>
                    ))}
                </Table.Tbody>
            </Table>
        </ScrollArea>
        {/* Toolbar with paging and export menu */}
        <Group justify="space-between" mt="xs" px="xs">
            <Group>
                <Button
                    variant="subtle"
                    leftSection={<TbChevronLeft size={16} />}
                    size="xs"
                    // onClick={handlePrevPage}
                    disabled
                >
                    Prev
                </Button>
                <Button
                    variant="subtle"
                    rightSection={<TbChevronRight size={16} />}
                    size="xs"
                    // onClick={handleNextPage}
                    disabled
                >
                    Next
                </Button>
            </Group>
            <Group mt={12} justify="flex-end" hidden={false}>
            {/*<Button*/}
            {/*            variant="outline"*/}
            {/*            leftSection={<TbShare size={16} />}*/}
            {/*            size="xs"*/}
            {/*            hidden={true}*/}
            {/*        >*/}
            {/*            Share*/}
            {/*        </Button>*/}
            {/*<Menu shadow="md" width={160} position="bottom-end">*/}
            {/*    <Menu.Target>*/}
            {/*        <Button*/}
            {/*            variant="outline"*/}
            {/*            leftSection={<TbDownload size={16} />}*/}
            {/*            size="xs"*/}
            {/*            hidden={true}*/}
            {/*        >*/}
            {/*            Export*/}
            {/*        </Button>*/}
            {/*    </Menu.Target>*/}
            {/*    <Menu.Dropdown>*/}
            {/*        <Menu.Item>Export as Excel</Menu.Item>*/}
            {/*        <Menu.Item>Export as CSV</Menu.Item>*/}
            {/*    </Menu.Dropdown>*/}
            {/*</Menu>*/}
            </Group>
        </Group>
        </>
    )
    // Tiny toolbar with paging links
    
}