import {
  Box,
  Paper,
  TextInput,
  PasswordInput,
  Button,
  Text,
  Stack,
  Alert,
  useMantineColorScheme,
} from '@mantine/core';
import { useState } from 'react';
import { useNavigate } from 'react-router';
import {
  HiOutlineEnvelope,
  HiOutlineLockClosed,
  HiOutlineUser,
  HiOutlineExclamationTriangle,
} from 'react-icons/hi2';
import { BRAND_DISPLAY_NAME, BRAND_LOGO_SRC } from '../../branding';

/**
 * Props for the [RegisterPage] component.
 */
interface RegisterPageProps {
  /**
   * Callback invoked when the user submits the registration form.
   *
   * @param email - Email address entered by the user.
   * @param password - Password entered by the user.
   * @param displayName - Optional display name entered by the user.
   */
  onRegister: (email: string, password: string, displayName?: string) => Promise<void>;
}

/**
 * Full-screen registration page with email, password, and optional display name fields.
 *
 * Renders a centred Paper card matching the [LoginPage] layout. On submit, calls
 * [onRegister] and shows a Mantine [Alert] on errors such as duplicate email
 * (`ALREADY_REGISTERED`) or unexpected failures. Includes a "Already have an
 * account?" link that navigates to `/login`.
 */
export function RegisterPage({ onRegister }: RegisterPageProps) {
  const { colorScheme } = useMantineColorScheme();
  const isDark = colorScheme === 'dark';
  const navigate = useNavigate();

  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [displayName, setDisplayName] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    setSubmitting(true);
    try {
      await onRegister(email, password, displayName || undefined);
    } catch (err) {
      if (err instanceof Error) {
        if (err.message === 'ALREADY_REGISTERED') {
          setError('An account with this email already exists.');
        } else if (err.message === 'REGISTRATION_DISABLED') {
          setError('Registration is currently disabled.');
        } else {
          setError('Registration failed. Please try again.');
        }
      } else {
        setError('Registration failed. Please try again.');
      }
    } finally {
      setSubmitting(false);
    }
  };

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
            src={BRAND_LOGO_SRC}
            alt="Mill logo"
            style={{ width: 48, height: 48 }}
          />
          <Text fw={700} size="xl" c={isDark ? 'gray.1' : 'gray.8'}>
            {BRAND_DISPLAY_NAME}
          </Text>
          <Text size="sm" c="dimmed">
            Create your account
          </Text>
        </Stack>

        {error && (
          <Alert
            icon={<HiOutlineExclamationTriangle size={16} />}
            color="red"
            mb="md"
            data-testid="register-error"
          >
            {error}
          </Alert>
        )}

        <form onSubmit={handleSubmit}>
          <Stack gap="sm">
            <TextInput
              label="Email"
              placeholder="you@example.com"
              leftSection={<HiOutlineEnvelope size={16} />}
              size="md"
              value={email}
              onChange={(e) => setEmail(e.currentTarget.value)}
              data-testid="email-input"
              required
            />
            <PasswordInput
              label="Password"
              placeholder="Choose a password"
              leftSection={<HiOutlineLockClosed size={16} />}
              size="md"
              value={password}
              onChange={(e) => setPassword(e.currentTarget.value)}
              data-testid="password-input"
              required
            />
            <TextInput
              label="Display name"
              placeholder="Your name (optional)"
              leftSection={<HiOutlineUser size={16} />}
              size="md"
              value={displayName}
              onChange={(e) => setDisplayName(e.currentTarget.value)}
              data-testid="displayname-input"
            />
            <Button
              type="submit"
              fullWidth
              size="md"
              mt="xs"
              color={isDark ? 'cyan' : 'teal'}
              loading={submitting}
              data-testid="register-button"
            >
              Create account
            </Button>
          </Stack>
        </form>

        <Text size="xs" c="dimmed" ta="center" mt="lg">
          Already have an account?{' '}
          <Text
            component="span"
            size="xs"
            c={isDark ? 'cyan.4' : 'teal.6'}
            fw={500}
            style={{ cursor: 'pointer' }}
            onClick={() => navigate('/login')}
            data-testid="signin-link"
          >
            Sign in
          </Text>
        </Text>
      </Paper>
    </Box>
  );
}
