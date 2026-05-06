/// <reference types="vite/client" />

interface ImportMetaEnv {
  /** `mock` forces the in-browser mock chat stack; omit for REST (production default). Tests always mock. */
  readonly VITE_CHAT_API?: string;
  /** Dev-only default agent profile forwarded as `profileId` on chat create when not selected in UI. */
  readonly VITE_MILL_AI_PROFILE?: string;
}

declare module '*.module.css' {
  const classes: { readonly [key: string]: string };
  export default classes;
}
