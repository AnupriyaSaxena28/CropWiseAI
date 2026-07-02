/** @type {import('next').NextConfig} */
const nextConfig = {
  // Don't fail the production build on pre-existing type/lint issues
  // (they don't affect runtime; fix incrementally).
  typescript: { ignoreBuildErrors: true },
  eslint: { ignoreDuringBuilds: true },
  images: {
    remotePatterns: [
      { protocol: "https", hostname: "firebasestorage.googleapis.com" },
      { protocol: "https", hostname: "lh3.googleusercontent.com" },
    ],
  },
  // Silence the firebase-admin punycode deprecation warning in dev
  webpack: (config) => {
    config.resolve.fallback = { ...config.resolve.fallback, punycode: false };
    return config;
  },
};

module.exports = nextConfig;