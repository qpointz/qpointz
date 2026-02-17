import { createTheme, type CSSVariablesResolver } from "@mantine/core";

export function buildTheme() {
  const lightNeutrals = [
    "#f5f7fb",
    "#eef2f8",
    "#dde5f0",
    "#c9d4e5",
    "#b2c0d8",
    "#90a3bf",
    "#7083a0",
    "#556983",
    "#3b4f67",
    "#27384d"
  ];

  const darkNeutrals = [
    "#e7eef8",
    "#d0dbea",
    "#b2bfd2",
    "#90a0b8",
    "#73859f",
    "#5a6e8a",
    "#455874",
    "#33445d",
    "#223349",
    "#152439"
  ];

  const darkPalette = [
    "#dce9ff",
    "#b8d4ff",
    "#8eb6f4",
    "#699ad9",
    "#4c80bc",
    "#3e699a",
    "#32547b",
    "#263f5c",
    "#1a2c42",
    "#0f1a2a"
  ];

  const resolver: CSSVariablesResolver = () => {
    const light: Record<string, string> = {
      "--mantine-color-body": lightNeutrals[0]
    };
    const dark: Record<string, string> = {};

    for (let i = 0; i < 10; i += 1) {
      dark[`--mantine-color-gray-${i}`] = darkNeutrals[i]!;
      dark[`--mantine-color-dark-${i}`] = darkPalette[i]!;
    }

    return { variables: {}, light, dark };
  };

  const theme = createTheme({
    primaryColor: "teal",
    colors: {
      teal: ["#e8fcfa", "#cff8f3", "#a4f0e8", "#74e7dd", "#50dfd2", "#35d9ca", "#1ec6b7", "#14a795", "#11857a", "#0f6b62"],
      cyan: ["#e8f8ff", "#cceefe", "#9adefc", "#63cdf8", "#3bbef5", "#24b5f3", "#13a2df", "#008cbe", "#0075a0", "#006084"],
      gray: lightNeutrals,
      dark: darkPalette
    },
    fontFamily:
      '-apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif',
    headings: {
      fontFamily:
        '-apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif'
    },
    radius: {
      xs: "4px",
      sm: "6px",
      md: "8px",
      lg: "12px",
      xl: "16px"
    },
    defaultRadius: "md",
    components: {
      Button: {
        defaultProps: {
          radius: "md"
        }
      },
      ActionIcon: {
        defaultProps: {
          radius: "md"
        }
      },
      Paper: {
        defaultProps: {
          radius: "lg"
        }
      }
    }
  });

  return { theme, cssVariablesResolver: resolver };
}
