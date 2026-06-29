import Link from "next/link";
import { Wheat } from "lucide-react";

export default function RootPage() {
  return (
    <div className="min-h-screen bg-cropwise-bg flex flex-col items-center justify-center p-4 relative overflow-hidden">
      {/* Icon with Glow */}
      <div className="relative mb-6">
        <div className="absolute inset-0 bg-cropwise-green rounded-3xl blur-2xl opacity-30"></div>
        <div className="relative bg-[#16271a] p-5 rounded-3xl shadow-green-lg flex items-center justify-center border border-cropwise-border">
          <Wheat className="w-12 h-12 text-[#f59e0b]" strokeWidth={1.5} />
        </div>
      </div>

      {/* Title */}
      <h1 className="text-5xl md:text-6xl font-bold text-cropwise-text mb-4 tracking-tight">
        CropWise <span className="text-cropwise-green">AI</span>
      </h1>

      {/* Subtitle */}
      <p className="text-xl md:text-2xl text-cropwise-muted mb-10 text-center font-light">
        Smart Farming. Sustainable Future. Prosperous Farmers.
      </p>

      {/* Button */}
      <Link 
        href="/login"
        className="bg-cropwise-green hover:bg-cropwise-green-light text-white px-8 py-3 rounded-full font-medium transition-all duration-300 shadow-green-md hover:shadow-green-lg mb-16 flex items-center gap-2"
      >
        🚀 Start Journey
      </Link>

      {/* Quote */}
      <p className="text-cropwise-green italic opacity-90 text-lg md:text-xl font-serif">
        "Every healthy harvest begins with the right decision."
      </p>
    </div>
  );
}