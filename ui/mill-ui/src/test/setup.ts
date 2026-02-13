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

// Polyfill Element.prototype.scrollTo for jsdom (used by MessageList auto-scroll)
if (!Element.prototype.scrollTo) {
  Element.prototype.scrollTo = function () {};
}
