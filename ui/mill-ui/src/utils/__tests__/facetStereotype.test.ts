import { describe, expect, it } from 'vitest';
import type { FacetPayloadSchema } from '../../types/facetTypes';
import {
  facetEmailLooksValid,
  facetFieldIsHyperlinkStringField,
  facetHyperlinkHref,
  facetHyperlinkPresentationActive,
  facetMailtoHref,
  facetStereotypeAppliesToValueSchema,
  facetStringStereotype,
  facetTagsPresentationActive,
  stereotypeTagsFromWire,
} from '../facetStereotype';

const strField = (): FacetPayloadSchema => ({
  type: 'STRING',
  title: 't',
  description: 'd',
});

const strListField = (): FacetPayloadSchema => ({
  type: 'ARRAY',
  title: 't',
  description: 'd',
  items: { type: 'STRING', title: 'i', description: 'id' },
});

const numListField = (): FacetPayloadSchema => ({
  type: 'ARRAY',
  title: 't',
  description: 'd',
  items: { type: 'NUMBER', title: 'i', description: 'id' },
});

const linkObjectField = (): FacetPayloadSchema => ({
  type: 'OBJECT',
  title: 'link',
  description: 'd',
  fields: [
    { name: 'title', schema: { type: 'STRING', title: 't', description: 'd' }, required: false },
    { name: 'href', schema: { type: 'STRING', title: 'h', description: 'd' }, required: true },
  ],
});

const linkObjectListField = (): FacetPayloadSchema => ({
  type: 'ARRAY',
  title: 'links',
  description: 'd',
  items: linkObjectField(),
});

describe('facetStereotype', () => {
  it('facetStereotypeAppliesToValueSchema for string and string list only', () => {
    expect(facetStereotypeAppliesToValueSchema(strField())).toBe(true);
    expect(facetStereotypeAppliesToValueSchema(strListField())).toBe(true);
    expect(facetStereotypeAppliesToValueSchema(numListField())).toBe(false);
  });

  it('facetStringStereotype picks email over hyperlink for STRING and ARRAY of STRING', () => {
    expect(facetStringStereotype(strField(), 'email')).toBe('email');
    expect(facetStringStereotype(strListField(), 'hyperlink,email')).toBe('email');
    expect(facetStringStereotype(strListField(), 'hyperlink')).toBe('hyperlink');
    expect(facetStringStereotype(strField(), 'tags')).toBe('none');
    expect(facetStringStereotype(numListField(), 'email')).toBe('none');
  });

  it('facetFieldIsHyperlinkStringField detects hyperlink tag', () => {
    expect(facetFieldIsHyperlinkStringField(strField(), 'Hyperlink')).toBe(true);
    expect(facetFieldIsHyperlinkStringField(strListField(), 'tags,hyperlink')).toBe(true);
    expect(facetFieldIsHyperlinkStringField(strListField(), ['hyperlink'])).toBe(true);
    expect(facetFieldIsHyperlinkStringField(strField(), 'tags')).toBe(false);
    expect(facetFieldIsHyperlinkStringField(numListField(), 'hyperlink')).toBe(false);
    expect(facetFieldIsHyperlinkStringField(strField(), 'email,hyperlink')).toBe(false);
  });

  it('facetEmailLooksValid uses pragmatic pattern', () => {
    expect(facetEmailLooksValid('a@b.co')).toBe(true);
    expect(facetEmailLooksValid('not-an-email')).toBe(false);
    expect(facetEmailLooksValid('')).toBe(false);
  });

  it('facetHyperlinkHref adds scheme and blocks javascript', () => {
    expect(facetHyperlinkHref('example.com/path')).toBe('https://example.com/path');
    expect(facetHyperlinkHref('https://a/b')).toBe('https://a/b');
    expect(facetHyperlinkHref(' javascript:alert(1)')).toBe('#');
  });

  it('facetMailtoHref builds mailto and blocks javascript', () => {
    expect(facetMailtoHref(' user@example.com ')).toBe('mailto:user@example.com');
    expect(facetMailtoHref('javascript:evil')).toBe('#');
  });

  it('stereotypeTagsFromWire splits commas', () => {
    expect(stereotypeTagsFromWire('a,hyperlink')).toEqual(['a', 'hyperlink']);
  });

  it('facetHyperlinkPresentationActive includes OBJECT and ARRAY of OBJECT when tagged hyperlink', () => {
    expect(facetHyperlinkPresentationActive(linkObjectField(), 'hyperlink')).toBe(true);
    expect(facetHyperlinkPresentationActive(linkObjectListField(), 'hyperlink')).toBe(true);
    expect(facetHyperlinkPresentationActive(strField(), 'hyperlink')).toBe(true);
    expect(facetHyperlinkPresentationActive(linkObjectField(), 'tags')).toBe(false);
    expect(facetHyperlinkPresentationActive(numListField(), 'hyperlink')).toBe(false);
    expect(facetHyperlinkPresentationActive(strField(), 'email,hyperlink')).toBe(false);
  });

  it('facetTagsPresentationActive requires tags stereotype without email/hyperlink winning', () => {
    expect(facetTagsPresentationActive(strListField(), 'tags')).toBe(true);
    expect(facetTagsPresentationActive(strField(), 'tags')).toBe(true);
    expect(facetTagsPresentationActive(strListField(), 'tags,email')).toBe(false);
    expect(facetTagsPresentationActive(strListField(), 'tags,hyperlink')).toBe(false);
    expect(facetTagsPresentationActive(numListField(), 'tags')).toBe(false);
  });
});
