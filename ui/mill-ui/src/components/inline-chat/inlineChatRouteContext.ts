import type { InlineChatContextType } from '../../types/inlineChat';

/** Route context type for inline-chat-capable views, or null elsewhere. */
export function resolveInlineChatRouteContextType(
  pathname: string,
): InlineChatContextType | null {
  if (pathname === '/analysis' || pathname.startsWith('/analysis/')) {
    return 'analysis';
  }
  if (pathname === '/model' || pathname.startsWith('/model/')) {
    return 'model';
  }
  if (pathname === '/knowledge' || pathname.startsWith('/knowledge/')) {
    return 'knowledge';
  }
  return null;
}

/** Context id for the current route host (mirrors AppHeader / InlineChatDrawer). */
export function resolveInlineChatRouteContextId(pathname: string): string | null {
  if (pathname === '/analysis') {
    return '__analysis__';
  }
  if (pathname.startsWith('/analysis/')) {
    const id = pathname.replace('/analysis/', '').split('/').filter(Boolean)[0];
    return id || '__analysis__';
  }
  if (pathname === '/model') {
    return '__model__';
  }
  if (pathname.startsWith('/model/')) {
    const segments = pathname.replace('/model/', '').split('/').filter(Boolean);
    return segments.length > 0 ? segments.join('.') : '__model__';
  }
  if (pathname === '/knowledge') {
    return '__knowledge__';
  }
  if (pathname.startsWith('/knowledge/')) {
    const id = pathname.replace('/knowledge/', '').split('/').filter(Boolean)[0];
    return id || '__knowledge__';
  }
  return null;
}
