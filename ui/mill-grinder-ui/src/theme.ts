import { createTheme } from '@mantine/core';

// Theme configuration - shared between app and tests
export const theme = createTheme({
    fontFamily: 'Inter, sans-serif',
    primaryColor: 'primary',
    defaultRadius: 'md',

    colors: {
        // Professional blue palette
        primary: [
            '#e7f5ff',
            '#d0ebff',
            '#a5d8ff',
            '#74c0fc',
            '#4dabf7',
            '#339af0',
            '#228be6',
            '#1c7ed6',
            '#1971c2',
            '#1864ab'
        ],
        // Dark mode colors
        dark: [
            '#C1C2C5',
            '#A6A7AB',
            '#909296',
            '#5c5f66',
            '#373A40',
            '#2C2E33',
            '#25262b',
            '#1A1B1E',
            '#141517',
            '#101113'
        ],
        success: ['#e6f9f0', '#ccf2e1', '#99e5c3', '#66d9a4', '#33cc85', '#00bf66', '#00994f', '#007339', '#004d26', '#002613'],
        danger: ['#fdecea', '#f9d3ce', '#f4a89e', '#ef7d6e', '#ea5240', '#e6271a', '#bf1f15', '#991710', '#730f0b', '#4d0806'],
        warning: ['#fff4e5', '#ffe8cc', '#ffd699', '#ffc266', '#ffad33', '#ff9900', '#cc7a00', '#995c00', '#663d00', '#331f00'],
        info: ['#e0f7fa', '#b2ebf2', '#80deea', '#4dd0e1', '#26c6da', '#00bcd4', '#00acc1', '#0097a7', '#00838f', '#006064']
    },

    shadows: {
        xs: '0 1px 2px rgba(0, 0, 0, 0.05)',
        sm: '0 1px 3px rgba(0, 0, 0, 0.1)',
        md: '0 4px 6px rgba(0, 0, 0, 0.1)',
    },

    components: {
        Card: {
            defaultProps: {
                shadow: 'xs',
                radius: 'md',
            },
        },
        Button: {
            defaultProps: {
                radius: 'md',
            },
        },
        NavLink: {
            defaultProps: {
                radius: 'sm',
            },
        },
    },
});
