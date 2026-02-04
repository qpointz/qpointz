import { Component } from 'react';
import type { ErrorInfo, ReactNode } from 'react';
import { Text, Group, Card } from '@mantine/core';

interface Props {
    children: ReactNode;
    fallback?: ReactNode;
}

interface State {
    hasError: boolean;
    error: Error | null;
    errorInfo: ErrorInfo | null;
}

export class ErrorBoundary extends Component<Props, State> {
    constructor(props: Props) {
        super(props);
        this.state = {
            hasError: false,
            error: null,
            errorInfo: null,
        };
    }

    static getDerivedStateFromError(error: Error): State {
        return {
            hasError: true,
            error,
            errorInfo: null,
        };
    }

    componentDidCatch(error: Error, errorInfo: ErrorInfo) {
        console.error('[ErrorBoundary] Caught error:', error, errorInfo);
        this.setState({
            error,
            errorInfo,
        });
    }

    render() {
        if (this.state.hasError) {
            if (this.props.fallback) {
                return this.props.fallback;
            }

            return (
                <Card p="md" withBorder>
                    <Group>
                        <Text c="red" fw={600}>Error rendering data:</Text>
                        <Text size="sm" c="dimmed">
                            {this.state.error?.message || 'Unknown error'}
                        </Text>
                    </Group>
                    {this.state.errorInfo && (
                        <Text size="xs" c="dimmed" mt="xs" style={{ fontFamily: 'monospace' }}>
                            {this.state.errorInfo.componentStack}
                        </Text>
                    )}
                </Card>
            );
        }

        return this.props.children;
    }
}
