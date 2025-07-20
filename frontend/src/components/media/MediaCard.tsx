'use client';

interface MediaCardProps {
  id: number;
  type: 'MOVIE' | 'TV_SHOW' | 'BOOK';
  title: string;
  originalTitle?: string;
  overview?: string;
  releaseDate?: string;
  voteAverage?: number;
  voteCount?: number;
  posterPath?: string | null;
  backdropPath?: string | null;
  onAddToLibrary?: (id: number) => void;
  onViewDetails?: (id: number) => void;
}

export default function MediaCard({
  id,
  type,
  title,
  originalTitle,
  overview,
  releaseDate,
  voteAverage,
  voteCount,
  posterPath,
  backdropPath,
  onAddToLibrary,
  onViewDetails,
}: MediaCardProps) {
  const getTypeIcon = () => {
    switch (type) {
      case 'MOVIE': return '🎬';
      case 'TV_SHOW': return '📺';
      case 'BOOK': return '📖';
      default: return '🎭';
    }
  };

  const getTypeColor = () => {
    switch (type) {
      case 'MOVIE': return 'from-red-500 to-red-700';
      case 'TV_SHOW': return 'from-blue-500 to-blue-700';
      case 'BOOK': return 'from-green-500 to-green-700';
      default: return 'from-gray-500 to-gray-700';
    }
  };

  const formatRating = (rating?: number) => {
    if (!rating) return 'N/A';
    return rating.toFixed(1);
  };

  const formatYear = (date?: string) => {
    if (!date) return '';
    return new Date(date).getFullYear();
  };

  const handleImageError = (e: React.SyntheticEvent<HTMLImageElement, Event>) => {
    e.currentTarget.src = '/placeholder-media.jpg'; // You can add a placeholder image
  };

  return (
    <div className="media-card group relative overflow-hidden">
      {/* Poster/Image */}
      <div className="relative aspect-[2/3] overflow-hidden bg-gray-800">
        {posterPath ? (
          <img
            src={`https://image.tmdb.org/t/p/w500${posterPath}`}
            alt={title}
            className="w-full h-full object-cover transition-transform duration-300 group-hover:scale-110"
            onError={handleImageError}
          />
        ) : (
          <div className="w-full h-full flex items-center justify-center bg-gradient-to-br from-gray-700 to-gray-900">
            <span className="text-6xl opacity-50">{getTypeIcon()}</span>
          </div>
        )}
        
        {/* Overlay */}
        <div className="absolute inset-0 bg-gradient-to-t from-black/80 via-transparent to-transparent opacity-0 group-hover:opacity-100 transition-opacity duration-300">
          <div className="absolute bottom-4 left-4 right-4">
            <div className="flex gap-2">
              <button
                onClick={() => onViewDetails?.(id)}
                className="btn-secondary text-sm px-3 py-2 flex-1"
              >
                👁️ Details
              </button>
              <button
                onClick={() => onAddToLibrary?.(id)}
                className="btn-primary text-sm px-3 py-2 flex-1"
              >
                + Add
              </button>
            </div>
          </div>
        </div>

        {/* Type Badge */}
        <div className={`absolute top-3 left-3 bg-gradient-to-r ${getTypeColor()} px-2 py-1 rounded-full text-xs font-semibold text-white shadow-lg`}>
          <span className="mr-1">{getTypeIcon()}</span>
          {type.replace('_', ' ')}
        </div>

        {/* Rating Badge */}
        {voteAverage && voteAverage > 0 && (
          <div className="absolute top-3 right-3 bg-black/70 backdrop-blur-sm px-2 py-1 rounded-full text-xs font-semibold text-white border border-gray-600">
            ⭐ {formatRating(voteAverage)}
          </div>
        )}
      </div>

      {/* Content */}
      <div className="p-4">
        {/* Title */}
        <h3 className="font-semibold text-white text-lg mb-2 line-clamp-2 leading-tight">
          {title}
        </h3>

        {/* Original Title */}
        {originalTitle && originalTitle !== title && (
          <p className="text-sm text-gray-400 mb-2 italic">
            {originalTitle}
          </p>
        )}

        {/* Release Year */}
        {releaseDate && (
          <p className="text-sm text-gray-500 mb-2">
            {formatYear(releaseDate)}
          </p>
        )}

        {/* Overview */}
        {overview && (
          <p className="text-sm text-gray-400 line-clamp-3 leading-relaxed">
            {overview}
          </p>
        )}

        {/* Vote Count */}
        {voteCount && voteCount > 0 && (
          <div className="mt-3 flex items-center text-xs text-gray-500">
            <span className="mr-1">👥</span>
            {voteCount.toLocaleString()} votes
          </div>
        )}
      </div>
    </div>
  );
} 