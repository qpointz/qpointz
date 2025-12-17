import {useReactTable, flexRender, createColumnHelper, getCoreRowModel, getPaginationRowModel} from "@tanstack/react-table";
import {ScrollArea, Table, Group, Button, Text} from "@mantine/core";
import {TbChevronLeft, TbChevronRight} from "react-icons/tb";
import {useMemo, useEffect, useRef, memo} from "react";

function DataContainerComponent(input: any) {
    const { data: inputData, fullHeight = false } = input || {};
    const mountRef = useRef(true);
    
    useEffect(() => {
        mountRef.current = true;
        return () => {
            mountRef.current = false;
        };
    }, []);
    // Safely extract data with comprehensive validation
    let container: any = {};
    let rawData: any[] = [];
    let fields: any[] = [];
    
    try {
        container = inputData || {};
        rawData = Array.isArray(container?.data) ? container.data : [];
        fields = Array.isArray(container?.fields) ? container.fields : [];
    } catch (error) {
        return (
            <Group justify="center" p="xl">
                <Text c="red" size="sm">
                    Error reading data structure
                </Text>
            </Group>
        );
    }

    // Early return if fields are missing - prevents useReactTable from being called with invalid data
    if (!Array.isArray(fields) || fields.length === 0) {
        return (
            <Group justify="center" p="xl">
                <Text c="dimmed" size="sm">
                    No data available. The query may have returned an error or no results.
                </Text>
            </Group>
        );
    }

    // Limit data to prevent browser freezing with very large datasets
    // Pagination will handle showing more data
    const MAX_INITIAL_ROWS = 10000;
    const data = useMemo(() => {
        if (!Array.isArray(rawData)) {
            return [];
        }
        return rawData.slice(0, MAX_INITIAL_ROWS);
    }, [rawData]);

    // Memoize columnHelper to prevent unnecessary re-creation
    const columnHelper = useMemo(() => createColumnHelper<Array<any>>(), []);
    
    const columns = useMemo(() => {
        if (!Array.isArray(fields) || fields.length === 0) {
            return [];
        }
        return fields.map((field: string, idx: number) => {
            return columnHelper.accessor(row => row[idx], {
                id: field,
                header: field,
                cell: info => info.renderValue()
            });
        });
    }, [fields, columnHelper]);

    // Validate data structure before creating table
    if (!Array.isArray(data)) {
        return (
            <Group justify="center" p="xl">
                <Text c="red" size="sm">
                    Invalid data structure: expected array
                </Text>
            </Group>
        );
    }
    
    // Check if data rows match column count
    if (data.length > 0 && !data.every(row => Array.isArray(row))) {
        return (
            <Group justify="center" p="xl">
                <Text c="red" size="sm">
                    Invalid data structure: rows must be arrays
                </Text>
            </Group>
        );
    }
    
    // Additional safety check: if columns is empty but we passed the fields check, something is wrong
    if (columns.length === 0) {
        return (
            <Group justify="center" p="xl">
                <Text c="red" size="sm">
                    Invalid data: no columns available
                </Text>
            </Group>
        );
    }
    const table = useReactTable({
        data,
        columns,
        getCoreRowModel: getCoreRowModel(),
        getPaginationRowModel: getPaginationRowModel(),
        initialState: {
            pagination: {
                pageSize: 100,
            },
        },
    });

    // Memoize header groups and rows to prevent re-computation issues
    const headerGroups = useMemo(() => {
        if (!mountRef.current || !table) {
            return [];
        }
        try {
            const groups = table.getHeaderGroups();
            return Array.isArray(groups) ? groups : [];
        } catch (error) {
            return [];
        }
    }, [table]);

    const rows = useMemo(() => {
        if (!mountRef.current || !table) {
            return [];
        }
        try {
            const rowModel = table.getRowModel();
            const rowArray = rowModel?.rows;
            return Array.isArray(rowArray) ? rowArray : [];
        } catch (error) {
            return [];
        }
    }, [table]);

    return (
        <div style={fullHeight ? { display: 'flex', flexDirection: 'column', height: '100%', overflow: 'hidden' } : undefined}>
        <ScrollArea
            scrollbars="xy"
            type="hover"
            offsetScrollbars={true}
            scrollbarSize={12}
            h={fullHeight ? "100%" : "60vh"}
            style={{ width: "100%", flex: fullHeight ? 1 : undefined, minHeight: fullHeight ? 0 : undefined }}
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
                    {headerGroups.map(headerGroup => (
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
                    {rows.map(row => (
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
                    {table.getRowModel().rows.length === 0 && (
                        <Table.Tr>
                            <Table.Td colSpan={Math.max(1, columns.length)} style={{ textAlign: 'center', padding: '20px' }}>
                                <Text c="dimmed">No data to display</Text>
                            </Table.Td>
                        </Table.Tr>
                    )}
                </Table.Tbody>
            </Table>
        </ScrollArea>
        {/* Toolbar with paging and export menu */}
        <Group justify="space-between" mt="xs" px="xs" style={fullHeight ? { flexShrink: 0 } : undefined}>
            <Group>
                <Button
                    variant="subtle"
                    leftSection={<TbChevronLeft size={16} />}
                    size="xs"
                    onClick={() => table.previousPage()}
                    disabled={!table.getCanPreviousPage()}
                >
                    Prev
                </Button>
                <Button
                    variant="subtle"
                    rightSection={<TbChevronRight size={16} />}
                    size="xs"
                    onClick={() => table.nextPage()}
                    disabled={!table.getCanNextPage()}
                >
                    Next
                </Button>
                <Text size="sm" c="dimmed">
                    Page {table.getState().pagination.pageIndex + 1} of {Math.max(1, table.getPageCount())} 
                    {rawData.length > MAX_INITIAL_ROWS && ` (showing first ${MAX_INITIAL_ROWS} of ${rawData.length} rows)`}
                </Text>
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
        </div>
    )
    // Tiny toolbar with paging links
    
}

// Memoize to prevent unnecessary re-renders during tab switches
// Use simple reference equality - if the data object reference hasn't changed, don't re-render
export default memo(DataContainerComponent);