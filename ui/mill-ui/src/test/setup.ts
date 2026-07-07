import '@testing-library/jest-dom';
import { configure } from '@testing-library/react';

configure({ asyncUtilTimeout: 10_000 });

function stubDomRect(): DOMRect {
  return {
    x: 0,
    y: 0,
    width: 0,
    height: 0,
    top: 0,
    left: 0,
    bottom: 0,
    right: 0,
    toJSON() {
      return {};
    },
  } as DOMRect;
}

const emptyDomRectList = {
  length: 0,
  item: () => null,
  [Symbol.iterator]: function* emptyRects() {},
} as DOMRectList;

// CodeMirror layout measurement in jsdom (missing on Node 24 CI; causes exit 1 after tests pass).
if (typeof Range !== 'undefined') {
  if (!Range.prototype.getBoundingClientRect) {
    Range.prototype.getBoundingClientRect = stubDomRect;
  }
  if (!Range.prototype.getClientRects) {
    Range.prototype.getClientRects = () => emptyDomRectList;
  }
}
if (!document.elementFromPoint) {
  document.elementFromPoint = () => null;
}

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
