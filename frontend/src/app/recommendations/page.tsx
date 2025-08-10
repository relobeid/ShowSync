'use client';

import { useEffect, useState } from 'react';
import api from '@/lib/api';
import RecommendationCard from '@/components/media/RecommendationCard';

export default function RecommendationsPage() {
  type ContentRec = {
    recommendationId?: number;
    mediaId: number;
    mediaTitle: string;
    mediaType?: string;
    mediaPoster?: string | null;
    mediaOverview?: string;
    mediaYear?: number;
    mediaRating?: number;
    explanation?: string;
  };

  const [personal, setPersonal] = useState<ContentRec[]>([]);
  const [trending, setTrending] = useState<ContentRec[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    let canceled = false;
    (async () => {
      try {
        setLoading(true);
        const [personalPage, trendingList] = await Promise.all([
          api.recommendations.getPersonal(0, 12),
          api.recommendations.getTrending(12),
        ]);
        if (!canceled) {
          setPersonal(personalPage?.content || []);
          setTrending(trendingList || []);
        }
      } catch (e) {
        if (!canceled) setError(e instanceof Error ? e.message : 'Failed to load recommendations');
      } finally {
        if (!canceled) setLoading(false);
      }
    })();
    return () => { canceled = true; };
  }, []);

  if (loading) return <div className="container mx-auto p-6 text-gray-300">Loading recommendations...</div>;
  if (error) return <div className="container mx-auto p-6 text-red-400">{error}</div>;

  return (
    <div className="container mx-auto p-6 space-y-10">
      <section>
        <h2 className="text-xl font-semibold text-white mb-4">For You</h2>
        {personal.length === 0 ? (
          <p className="text-gray-400">No personal recommendations yet.</p>
        ) : (
          <div className="grid grid-cols-2 md:grid-cols-4 lg:grid-cols-6 gap-6">
            {personal.map((rec) => (
              <RecommendationCard key={`${rec.recommendationId}-${rec.mediaId}`} rec={rec} />
            ))}
          </div>
        )}
      </section>

      <section>
        <h2 className="text-xl font-semibold text-white mb-4">Trending</h2>
        {trending.length === 0 ? (
          <p className="text-gray-400">No trending items right now.</p>
        ) : (
          <div className="grid grid-cols-2 md:grid-cols-4 lg:grid-cols-6 gap-6">
            {trending.map((rec) => (
              <RecommendationCard key={`${rec.recommendationId}-${rec.mediaId}`} rec={rec} />
            ))}
          </div>
        )}
      </section>
    </div>
  );
}


