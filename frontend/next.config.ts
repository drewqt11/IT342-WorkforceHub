import type { NextConfig } from "next";

const nextConfig: NextConfig = {
  reactStrictMode: true,
  images: {
    domains: ['localhost', 'workforcehub.vercel.app'],
  },
  async rewrites() {
    return [
      {
        source: '/api/:path*',
        destination: 'https://java-app-6sj5.onrender.com/api/:path*',
      },
    ];
  },
};

export default nextConfig;
