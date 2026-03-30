import js from '@eslint/js';
import globals from 'globals';
import reactHooks from 'eslint-plugin-react-hooks';
import reactRefresh from 'eslint-plugin-react-refresh';
import tseslint from 'typescript-eslint';
import { globalIgnores } from 'eslint/config';

export default tseslint.config([
  globalIgnores(['dist', 'coverage', 'node_modules']),
  {
    files: ['**/*.{ts,tsx}'],
    extends: [
      js.configs.recommended,
      ...tseslint.configs.recommended,
      reactHooks.configs['recommended-latest'],
      reactRefresh.configs.vite,
    ],
    languageOptions: {
      ecmaVersion: 2020,
      globals: globals.browser,
    },
    rules: {
      '@typescript-eslint/no-unused-vars': [
        'error',
        {
          argsIgnorePattern: '^_',
          varsIgnorePattern: '^_',
          caughtErrorsIgnorePattern: '^_',
        },
      ],
    },
  },
  /**
   * Context modules and App export hooks alongside providers; RelatedModelTree exports a pure helper.
   * Fast refresh is a dev convenience — disabling the rule avoids false positives without splitting every file.
   */
  {
    files: [
      'src/App.tsx',
      'src/context/**/*.tsx',
      'src/features/FeatureFlagContext.tsx',
      'src/theme/ThemeContext.tsx',
      'src/components/common/RelatedModelTree.tsx',
    ],
    rules: {
      'react-refresh/only-export-components': 'off',
    },
  },
]);
