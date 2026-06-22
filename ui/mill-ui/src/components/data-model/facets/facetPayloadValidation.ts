import type { FacetPayloadSchema } from '../../../types/facetTypes';
import {
  facetEmailLooksValid,
  facetHyperlinkPresentationActive,
  facetStringStereotype,
} from '../../../utils/facetStereotype';

function facetHyperlinkObjectHrefMissing(value: unknown): boolean {
  if (value == null || typeof value !== 'object' || Array.isArray(value)) return true;
  const href = (value as Record<string, unknown>).href;
  const s = href == null ? '' : typeof href === 'string' ? href.trim() : String(href).trim();
  return s.length === 0;
}

/** Appends errors for non-empty values that fail email format when stereotype is `email` (STRING or STRING[]). */
export function appendEmailStereotypeValidationErrors(
  schema: FacetPayloadSchema,
  value: unknown,
  errors: string[],
  path: string,
): void {
  if (schema.type !== 'OBJECT') return;
  const obj = (value && typeof value === 'object' ? value : {}) as Record<string, unknown>;
  (schema.fields ?? []).forEach((field) => {
    const nextPath = path ? `${path}.${field.name}` : field.name;
    const fv = obj[field.name];
    if (facetStringStereotype(field.schema, field.stereotype) === 'email') {
      if (field.schema.type === 'STRING') {
        const trimmed = String(fv ?? '').trim();
        if (trimmed.length > 0 && !facetEmailLooksValid(trimmed)) {
          errors.push(`${nextPath} must be a valid email address`);
        }
      } else if (field.schema.type === 'ARRAY' && field.schema.items?.type === 'STRING') {
        const arr = Array.isArray(fv) ? fv : [];
        arr.forEach((item, i) => {
          const trimmed = String(item ?? '').trim();
          if (trimmed.length > 0 && !facetEmailLooksValid(trimmed)) {
            errors.push(`${nextPath}[${i}] must be a valid email address`);
          }
        });
      }
    }
    if (field.schema.type === 'OBJECT' && fv != null && typeof fv === 'object' && !Array.isArray(fv)) {
      appendEmailStereotypeValidationErrors(field.schema, fv, errors, nextPath);
    }
    if (field.schema.type === 'ARRAY' && Array.isArray(fv) && field.schema.items?.type === 'OBJECT') {
      fv.forEach((el, i) => {
        if (el != null && typeof el === 'object') {
          appendEmailStereotypeValidationErrors(field.schema.items!, el, errors, `${nextPath}[${i}]`);
        }
      });
    }
  });
}

/** Appends errors for missing `href` on OBJECT / ARRAY-of-OBJECT values with hyperlink stereotype. */
export function appendHyperlinkStereotypeValidationErrors(
  schema: FacetPayloadSchema,
  value: unknown,
  errors: string[],
  path: string,
): void {
  if (schema.type !== 'OBJECT') return;
  const obj = (value && typeof value === 'object' ? value : {}) as Record<string, unknown>;
  (schema.fields ?? []).forEach((field) => {
    const nextPath = path ? `${path}.${field.name}` : field.name;
    const fv = obj[field.name];
    if (facetHyperlinkPresentationActive(field.schema, field.stereotype)) {
      if (field.schema.type === 'OBJECT') {
        if (facetHyperlinkObjectHrefMissing(fv)) {
          errors.push(`${nextPath}: wrong link`);
        }
      } else if (field.schema.type === 'ARRAY' && field.schema.items?.type === 'OBJECT') {
        const arr = Array.isArray(fv) ? fv : [];
        arr.forEach((item, i) => {
          if (facetHyperlinkObjectHrefMissing(item)) {
            errors.push(`${nextPath}[${i}]: wrong link`);
          }
        });
      }
    }
    if (field.schema.type === 'OBJECT' && fv != null && typeof fv === 'object' && !Array.isArray(fv)) {
      appendHyperlinkStereotypeValidationErrors(field.schema, fv, errors, nextPath);
    }
    if (field.schema.type === 'ARRAY' && Array.isArray(fv) && field.schema.items?.type === 'OBJECT') {
      fv.forEach((el, i) => {
        if (el != null && typeof el === 'object') {
          appendHyperlinkStereotypeValidationErrors(field.schema.items!, el, errors, `${nextPath}[${i}]`);
        }
      });
    }
  });
}
