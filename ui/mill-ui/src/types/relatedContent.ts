/** The kind of related object */
export type RelatedContentType = 'model' | 'concept' | 'analysis';

/** Lightweight reference to a related object shown in pills / popovers */
export interface RelatedContentRef {
  id: string;
  /** Human-readable label (entity display name, concept name, query title) */
  title: string;
  /** Discriminator so the UI can colour / icon the pill */
  type: RelatedContentType;
  /** Optional entity-type detail (e.g. 'TABLE', 'ATTRIBUTE') — only for model refs */
  entityType?: string;
}

export interface RelatedContentService {
  /**
   * Return the related content refs for a given context object.
   * @param contextType  e.g. 'model' | 'knowledge' | 'analysis'
   * @param contextId    the ID of the current object being viewed
   */
  getRelatedContent(
    contextType: string,
    contextId: string,
  ): Promise<RelatedContentRef[]>;
}
