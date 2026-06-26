import '@testing-library/jest-dom';

// Polyfill window.matchMedia for jsdom (required by Mantine's useMantineColorScheme)
Object.defineProperty(window, 'matchMedia', {
  writable: true,
  value: (query: string) => ({
    matches: false,
    media: query,
    onchange: null,
    addListener: () => {},
    removeListener: () => {},
    addEventListener: () => {},
    removeEventListener: () => {},
    dispatchEvent: () => false,
  }),
});

// Polyfill ResizeObserver for jsdom (required by Mantine's ScrollArea)
class ResizeObserverStub {
  observe() {}
  unobserve() {}
  disconnect() {}
}
globalThis.ResizeObserver = ResizeObserverStub as unknown as typeof ResizeObserver;

// Mantine 9 Textarea autosize listens to document.fonts loading events (jsdom stub).
if (typeof document.fonts?.addEventListener !== 'function') {
  const fontFaceSetStub = {
    addEventListener: () => {},
    removeEventListener: () => {},
    load: () => Promise.resolve([]),
    check: () => true,
    ready: Promise.resolve(undefined),
  };
  Object.defineProperty(document, 'fonts', {
    configurable: true,
    value: fontFaceSetStub,
  });
}

// Polyfill Element.prototype.scrollTo for jsdom (used by MessageList auto-scroll)
if (!Element.prototype.scrollTo) {
  Element.prototype.scrollTo = function () {};
}
