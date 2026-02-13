import {
  Box,
  Paper,
  TextInput,
  PasswordInput,
  Button,
  Text,
  Group,
  Divider,
  Stack,
  useMantineColorScheme,
} from '@mantine/core';
import {
  HiOutlineEnvelope,
  HiOutlineLockClosed,
} from 'react-icons/hi2';
import { useFeatureFlags } from '../../features/FeatureFlagContext';

interface LoginPageProps {
  onLogin: () => void;
}

/* ── Inline SVG icons for social / cloud providers ─────────────── */

function GithubIcon({ size = 18 }: { size?: number }) {
  return (
    <svg width={size} height={size} viewBox="0 0 24 24" fill="currentColor">
      <path d="M12 0C5.37 0 0 5.37 0 12c0 5.31 3.435 9.795 8.205 11.385.6.105.825-.255.825-.57 0-.285-.015-1.23-.015-2.235-3.015.555-3.795-.735-4.035-1.41-.135-.345-.72-1.41-1.23-1.695-.42-.225-1.02-.78-.015-.795.945-.015 1.62.87 1.845 1.23 1.08 1.815 2.805 1.305 3.495.99.105-.78.42-1.305.765-1.605-2.67-.3-5.46-1.335-5.46-5.925 0-1.305.465-2.385 1.23-3.225-.12-.3-.54-1.53.12-3.18 0 0 1.005-.315 3.3 1.23.96-.27 1.98-.405 3-.405s2.04.135 3 .405c2.295-1.56 3.3-1.23 3.3-1.23.66 1.65.24 2.88.12 3.18.765.84 1.23 1.905 1.23 3.225 0 4.605-2.805 5.625-5.475 5.925.435.375.81 1.095.81 2.22 0 1.605-.015 2.895-.015 3.3 0 .315.225.69.825.57A12.02 12.02 0 0024 12c0-6.63-5.37-12-12-12z" />
    </svg>
  );
}

function GoogleIcon({ size = 18 }: { size?: number }) {
  return (
    <svg width={size} height={size} viewBox="0 0 24 24">
      <path fill="#4285F4" d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92a5.06 5.06 0 01-2.2 3.32v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.1z" />
      <path fill="#34A853" d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z" />
      <path fill="#FBBC05" d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18A11.96 11.96 0 001 12c0 1.94.46 3.77 1.18 5.42l3.66-2.84z" />
      <path fill="#EA4335" d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z" />
    </svg>
  );
}

function MicrosoftIcon({ size = 18 }: { size?: number }) {
  return (
    <svg width={size} height={size} viewBox="0 0 24 24">
      <rect fill="#F25022" x="1" y="1" width="10" height="10" />
      <rect fill="#7FBA00" x="13" y="1" width="10" height="10" />
      <rect fill="#00A4EF" x="1" y="13" width="10" height="10" />
      <rect fill="#FFB900" x="13" y="13" width="10" height="10" />
    </svg>
  );
}

function AwsIcon({ size = 18 }: { size?: number }) {
  return (
    <svg width={size} height={size} viewBox="0 0 24 24" fill="none">
      <path d="M6.77 17.09c-2.14-.94-3.6-2.57-4.2-4.28-.08-.22.12-.36.24-.24 1.2 1.04 5.16 3.56 9.66 2.65 .18-.04.22.16.06.24-1.68.98-3.72 1.51-5.76 1.63z" fill="#F90"/>
      <path d="M18.42 18.42c-.62.72-1.82.82-2.08.34-.06-.12.06-.24.34-.34 1.08-.38 3.42-1.3 3.84-2.82.06-.22.24-.18.3.02.36 1.14-.72 2.04-2.4 2.8z" fill="#F90"/>
      <path d="M7.8 10.26c0-1.14.24-2.04.72-2.7.48-.66 1.26-1.02 2.22-1.02 1.02 0 1.74.36 2.16 1.02.42.66.66 1.56.66 2.7v.48c0 1.14-.24 2.04-.66 2.7-.42.66-1.14 1.02-2.16 1.02-.96 0-1.74-.36-2.22-1.02-.48-.66-.72-1.56-.72-2.7v-.48zm1.56.54c0 .72.12 1.26.3 1.62.18.36.48.54.9.54s.72-.18.9-.54c.18-.36.3-.9.3-1.62v-.66c0-.72-.12-1.26-.3-1.62-.18-.36-.48-.54-.9-.54s-.72.18-.9.54c-.18.36-.3.9-.3 1.62v.66zM14.28 14.04l-.48-6.96h1.56l.24 4.68.06.6 1.14-5.28h1.44l1.08 5.28.06-.6.3-4.68h1.5l-.54 6.96h-1.92l-1.02-4.74-.06-.42-1.08 5.16h-1.88z" fill="#252F3E"/>
      <path d="M3.72 14.04h1.56l.36-1.56h1.98l.36 1.56h1.56l-1.92-6.96H5.64L3.72 14.04zm2.22-2.82l.66-2.88h.06l.66 2.88H5.94z" fill="#252F3E"/>
    </svg>
  );
}

function AzureIcon({ size = 18 }: { size?: number }) {
  return (
    <svg width={size} height={size} viewBox="0 0 24 24" fill="none">
      <path d="M9.01 3H3l6.2 17.32L3.42 21h17.16l-4.66-4H10.1L15.68 6.2 9.01 3z" fill="#0078D4"/>
      <path d="M15.68 6.2l-5.58 10.8h5.82L21 21H9.21L15.68 6.2z" fill="#50E6FF" opacity=".8"/>
      <path d="M13.14 8.54L9.21 21l6.71-4H10.1l5.58-10.8-2.54 2.34z" fill="#0078D4" opacity=".6"/>
    </svg>
  );
}

/* ── Provider config ─────────────────────────────────────────────── */

interface SocialProvider {
  key: string;
  label: string;
  icon: React.ComponentType<{ size?: number }>;
  flagKey: 'loginGithub' | 'loginGoogle' | 'loginMicrosoft' | 'loginAws' | 'loginAzure';
}

const socialProviders: SocialProvider[] = [
  { key: 'github', label: 'Continue with GitHub', icon: GithubIcon, flagKey: 'loginGithub' },
  { key: 'google', label: 'Continue with Google', icon: GoogleIcon, flagKey: 'loginGoogle' },
  { key: 'microsoft', label: 'Continue with Microsoft', icon: MicrosoftIcon, flagKey: 'loginMicrosoft' },
  { key: 'aws', label: 'Continue with AWS', icon: AwsIcon, flagKey: 'loginAws' },
  { key: 'azure', label: 'Continue with Azure AD', icon: AzureIcon, flagKey: 'loginAzure' },
];

/* ── Component ───────────────────────────────────────────────────── */

export function LoginPage({ onLogin }: LoginPageProps) {
  const { colorScheme } = useMantineColorScheme();
  const isDark = colorScheme === 'dark';
  const flags = useFeatureFlags();

  const visibleProviders = socialProviders.filter((p) => flags[p.flagKey]);
  const showPassword = flags.loginPassword;
  const showDivider = visibleProviders.length > 0 && showPassword;

  return (
    <Box
      style={{
        minHeight: '100vh',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        background: isDark
          ? 'linear-gradient(135deg, var(--mantine-color-dark-8) 0%, var(--mantine-color-dark-9) 100%)'
          : 'linear-gradient(135deg, var(--mantine-color-gray-0) 0%, var(--mantine-color-teal-0) 100%)',
      }}
    >
      <Paper
        shadow="xl"
        radius="lg"
        p="xl"
        w={420}
        style={{
          backgroundColor: 'var(--mantine-color-body)',
          border: `1px solid var(--mantine-color-default-border)`,
        }}
      >
        {/* Brand */}
        <Stack align="center" gap="xs" mb="xl">
          <img
            src="/mill.svg"
            alt="Mill logo"
            style={{
              width: 48,
              height: 48,
            }}
          />
          <Text fw={700} size="xl" c={isDark ? 'gray.1' : 'gray.8'}>
            DataChat
          </Text>
          <Text size="sm" c="dimmed">
            Sign in to your workspace
          </Text>
        </Stack>

        {/* Social / SSO login buttons */}
        {visibleProviders.length > 0 && (
          <Stack gap="xs" mb={showPassword ? 'lg' : 0}>
            {visibleProviders.map((provider) => {
              const Icon = provider.icon;
              return (
                <Button
                  key={provider.key}
                  variant="default"
                  leftSection={<Icon size={16} />}
                  fullWidth
                  onClick={onLogin}
                  styles={{
                    root: {
                      borderColor: isDark ? 'var(--mantine-color-gray-6)' : 'var(--mantine-color-gray-3)',
                    },
                  }}
                >
                  {provider.label}
                </Button>
              );
            })}
          </Stack>
        )}

        {showDivider && (
          <Divider
            label="or sign in with email"
            labelPosition="center"
            mb="lg"
            color={isDark ? 'gray.6' : 'gray.3'}
          />
        )}

        {/* Email / Password form */}
        {showPassword && (
          <form
            onSubmit={(e) => {
              e.preventDefault();
              onLogin();
            }}
          >
            <Stack gap="sm">
              <TextInput
                label="Email"
                placeholder="you@example.com"
                leftSection={<HiOutlineEnvelope size={16} />}
                size="md"
              />
              <PasswordInput
                label="Password"
                placeholder="Your password"
                leftSection={<HiOutlineLockClosed size={16} />}
                size="md"
              />
              <Group justify="space-between" mt={4}>
                <Text
                  size="xs"
                  c={isDark ? 'cyan.4' : 'teal.6'}
                  style={{ cursor: 'pointer' }}
                >
                  Forgot password?
                </Text>
              </Group>
              <Button
                type="submit"
                fullWidth
                size="md"
                mt="xs"
                color={isDark ? 'cyan' : 'teal'}
              >
                Sign in
              </Button>
            </Stack>
          </form>
        )}

        {showPassword && (
          <Text size="xs" c="dimmed" ta="center" mt="lg">
            Don&apos;t have an account?{' '}
            <Text
              component="span"
              size="xs"
              c={isDark ? 'cyan.4' : 'teal.6'}
              fw={500}
              style={{ cursor: 'pointer' }}
            >
              Sign up
            </Text>
          </Text>
        )}
      </Paper>
    </Box>
  );
}
