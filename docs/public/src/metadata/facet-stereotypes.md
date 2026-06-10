# Facet field stereotypes (presentation hints)

Facet types describe the **shape** of metadata you can attach to entities (tables, columns, concepts, and so on). Some fields in that shape can carry optional **stereotypes**: short tags that tell **Mill UI** to use a friendlier control than a plain text box.

Stereotypes are **hints for the product UI only**. The metadata API does **not** validate or enforce them; unknown tags are stored on the facet type definition but treated like normal fields in the form.

---

## Tags Mill recognises today

| Tag | Typical use | What changes in the UI |
|-----|-------------|-------------------------|
| **email** | One address or a list of addresses | Inputs expect email shape; read mode shows a **mailto:** link. If both **email** and **hyperlink** are set on the same string field, **email** wins. |
| **hyperlink** | A URL, list of URLs, or `{ title, href }` objects | Read mode shows **links** (and for objects, separate **Title** and **URL** lines). Edit mode uses URL-oriented inputs or object fields. |
| **tags** | A list of short string labels | Edit mode uses a **tag entry** control (type and commit with comma or Enter). Not used if **email** or **hyperlink** wins on that field. |

Administrators edit stereotypes on **Admin → Model → Facet types** when building or adjusting a type’s payload schema.

---

## Who this affects

- **Data Model** — Facet panels when viewing or editing metadata on a schema object.  
- **Facet type admin** — Optional stereotype tags per payload field.  
- **Imports** — Canonical YAML / JSON facet definitions may include `stereotype` on fields; see the repository design doc **mill-ui-facet-stereotypes** for technical detail and wire formats.

---

## Related

- [Metadata concepts](concepts.md) — facet types vs assignments, scopes.  
- [Mill UI](mill-ui.md) — where facet editing appears in the product.
