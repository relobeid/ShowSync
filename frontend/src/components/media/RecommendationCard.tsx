'use client';

import MediaCard from './MediaCard';

type Rec = {
  recommendationId?: number;
  mediaId: number;
  mediaTitle: string;
  mediaType?: string;
  mediaPoster?: string | null;
  mediaOverview?: string;
  mediaYear?: number;
  mediaRating?: number;
  explanation?: string;
  relevanceScore?: number; // as decimal 0..1 (optional)
};

export default function RecommendationCard({ rec, onAdd }: { rec: Rec; onAdd?: (id: number) => void }) {
  return (
    <div className="space-y-2">
      <MediaCard
        id={rec.mediaId}
        type={(rec.mediaType as 'MOVIE' | 'TV_SHOW' | 'BOOK') || 'MOVIE'}
        title={rec.mediaTitle}
        overview={rec.mediaOverview}
        posterPath={rec.mediaPoster || undefined}
        releaseDate={rec.mediaYear ? `${rec.mediaYear}-01-01` : undefined}
        voteAverage={rec.mediaRating || undefined}
        onAddToLibrary={onAdd}
      />
      {rec.explanation && (
        <p className="text-sm text-gray-400 pl-1">Why: {rec.explanation}</p>
      )}
    </div>
  );
}


