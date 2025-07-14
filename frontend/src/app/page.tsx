import Layout from "@/components/layout/Layout";

export default function Home() {
  return (
    <Layout>
      <div className="text-center">
        <h1 className="text-4xl font-bold text-gray-900 mb-4">
          Welcome to ShowSync
        </h1>
        <p className="text-xl text-gray-600 mb-8">
          Your personal media tracking and social platform
        </p>
        <div className="grid grid-cols-1 md:grid-cols-3 gap-8 max-w-4xl mx-auto">
          <div className="bg-white p-6 rounded-lg shadow-md">
            <h2 className="text-xl font-semibold mb-2">Track Your Media</h2>
            <p className="text-gray-600">
              Keep track of movies, TV shows, and books you&apos;ve watched or want to watch.
            </p>
          </div>
          <div className="bg-white p-6 rounded-lg shadow-md">
            <h2 className="text-xl font-semibold mb-2">Connect with Friends</h2>
            <p className="text-gray-600">
              Join groups and see what your friends are watching and reading.
            </p>
          </div>
          <div className="bg-white p-6 rounded-lg shadow-md">
            <h2 className="text-xl font-semibold mb-2">Share Reviews</h2>
            <p className="text-gray-600">
              Write reviews and rate your favorite (or least favorite) media.
            </p>
          </div>
        </div>
    </div>
    </Layout>
  );
}
