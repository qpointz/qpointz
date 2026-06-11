import type { ChatMessageArtifact } from '../../../types/chat';
import { FacetProposalArtifactCard } from './FacetProposalArtifactCard';
import { SchemaCaptureArtifactCard } from './SchemaCaptureArtifactCard';
import { SqlArtifactCard } from './SqlArtifactCard';
import { UnknownArtifactCard } from './UnknownArtifactCard';

export function ArtifactCard({ artifact }: { artifact: ChatMessageArtifact }) {
  switch (artifact.kind) {
    case 'sql':
      return <SqlArtifactCard artifact={artifact} />;
    case 'facet-proposal':
      return <FacetProposalArtifactCard artifact={artifact} />;
    case 'schema-capture':
      return <SchemaCaptureArtifactCard artifact={artifact} />;
    case 'unknown':
      return <UnknownArtifactCard artifact={artifact} />;
  }
}
