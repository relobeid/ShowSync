'use client';

import { useState, useEffect } from 'react';
import Layout from "@/components/layout/Layout";

export default function Home() {
  const [currentTestimonialPage, setCurrentTestimonialPage] = useState(0);
  const [currentWord, setCurrentWord] = useState(0);
  const [isAnimating, setIsAnimating] = useState(false);
  const [showCurtains, setShowCurtains] = useState(true);
  
  const flickerWords = ['Movie', 'Book', 'TV', 'Anime', 'Media'];
  
  useEffect(() => {
    const interval = setInterval(() => {
      setIsAnimating(true);
      setTimeout(() => {
        setCurrentWord((prev) => (prev + 1) % flickerWords.length);
        setIsAnimating(false);
      }, 300); // Halfway through animation
    }, 2500); // Change word every 2.5 seconds
    
    return () => clearInterval(interval);
  }, [flickerWords.length]);

  // Scroll listener to hide curtains
  useEffect(() => {
    const handleScroll = () => {
      const heroSection = window.innerHeight * 0.8; // Hide after scrolling past hero
      if (window.scrollY > heroSection) {
        setShowCurtains(false);
      } else {
        setShowCurtains(true);
      }
    };

    window.addEventListener('scroll', handleScroll);
    return () => window.removeEventListener('scroll', handleScroll);
  }, []);

  // All testimonials with varied writing styles
  const allTestimonials = [
    // Page 1 - Mix of casual and formal
    [
      {
        initial: "J",
        name: "Jake @filmjunkie23",
        bio: "Horror movie addict",
        review: "rated hereditary 5 stars on showsync and immediately got matched with 3 other people who also loved psychological horror 💀 we ended up talking for hours about ari aster's cinematography and now we have a group chat that's basically our own film school",
        rating: "found my horror movie soulmates",
        bgColor: "from-blue-500 to-blue-700"
      },
      {
        initial: "S",
        name: "Sofia @kdramaobsessed",
        bio: "Anime & K-drama stan",
        review: "the ai somehow knew from my ratings that i'd love parasite even tho i mostly rated anime 5 stars?? got instantly connected with people who also appreciate dark social commentary and now we rate everything together and discuss in real time",
        rating: "AI actually reads my taste",
        bgColor: "from-purple-500 to-purple-700"
      }
    ],
    // Page 2 - More formal/analytical
    [
      {
        initial: "A",
        name: "Alex Chen",
        bio: "Film studies graduate",
        review: "As someone who studies cinema professionally, I was skeptical about AI taste matching. However, ShowSync's algorithm connected me with viewers who share my appreciation for complex narratives and visual storytelling. The discussions we have about cinematography and direction are incredibly insightful.",
        rating: "surprisingly sophisticated matching",
        bgColor: "from-green-500 to-green-700"
      },
      {
        initial: "M",
        name: "Maya Rodriguez",
        bio: "Independent bookstore owner",
        review: "I tend to gravitate toward literary fiction and experimental narratives—both in books and films. ShowSync found people with eerily similar reading/viewing habits. We've created this amazing cross-pollination between book discussions and their film adaptations.",
        rating: "found my intellectual tribe",
        bgColor: "from-indigo-500 to-indigo-700"
      }
    ],
    // Page 3 - Very casual/Gen Z
    [
      {
        initial: "T",
        name: "Tyler @chaoscinema",
        bio: "Unhinged movie takes",
        review: "bruh this app is UNREAL... gave the room 5 stars as a joke and got matched with other people who unironically love so-bad-it's-good movies 😭😭 we're planning a neil breen marathon and i've never been more excited about anything in my life",
        rating: "chaos energy matched perfectly",
        bgColor: "from-pink-500 to-pink-700"
      },
      {
        initial: "Z",
        name: "Zara ✨",
        bio: "Comfort show connoisseur",
        review: "ok but like... how did it know i rewatch the office AND parks and rec on repeat??? matched me with ppl who also need background noise shows and we've created the ultimate comfort viewing rotation. finally people who get it 🥺",
        rating: "comfort show solidarity",
        bgColor: "from-yellow-500 to-orange-600"
      }
    ]
  ];

  const totalPages = allTestimonials.length;

  const nextPage = () => {
    setCurrentTestimonialPage((prev) => (prev + 1) % totalPages);
  };

  const prevPage = () => {
    setCurrentTestimonialPage((prev) => (prev - 1 + totalPages) % totalPages);
  };

  const goToPage = (page: number) => {
    setCurrentTestimonialPage(page);
  };

  return (
    <Layout>
      {/* Cinema Hero Section with Theater Curtains */}
      <div className={`theater-curtains ${showCurtains ? 'curtains-visible' : 'curtains-hidden'} spotlight relative flex items-center justify-center pt-16 sm:pt-20 lg:pt-24 pb-8 px-4 sm:px-6`} style={{ minHeight: 'calc(100vh - 60px)' }}>
        {/* Floating Media Symbols */}
        <div className="absolute inset-0 overflow-hidden pointer-events-none">
          {/* Left side floating symbols */}
          <div className="absolute left-8 sm:left-16 lg:left-32 top-1/4 w-12 h-12 sm:w-14 sm:h-14 lg:w-16 lg:h-16 opacity-10 animate-bounce text-gray-400" style={{ animationDelay: '0s', animationDuration: '3s' }}>
            <svg fill="currentColor" viewBox="0 0 24 24">
              <path d="M18 4l2 4h-3l-2-4h-2l2 4h-3l-2-4H8l2 4H7L5 4H4c-1.1 0-1.99.9-1.99 2L2 18c0 1.1.9 2 2 2h16c1.1 0 2-.9 2-2V4h-4z"/>
            </svg>
          </div>
          <div className="absolute left-12 sm:left-32 lg:left-48 top-1/2 w-8 h-8 sm:w-10 sm:h-10 lg:w-12 lg:h-12 opacity-20 animate-bounce text-blue-400" style={{ animationDelay: '1s', animationDuration: '4s' }}>
            <svg fill="currentColor" viewBox="0 0 24 24">
              <path d="M21 3H3c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h18c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm0 16H3V5h18v14zm-10-7l5 3-5 3V9z"/>
            </svg>
          </div>
          <div className="absolute left-10 sm:left-24 lg:left-40 top-3/4 w-10 h-10 sm:w-12 sm:h-12 lg:w-14 lg:h-14 opacity-15 animate-bounce text-green-400" style={{ animationDelay: '2s', animationDuration: '3.5s' }}>
            <svg fill="currentColor" viewBox="0 0 24 24">
              <path d="M18 2H6c-1.1 0-2 .9-2 2v16c0 1.1.9 2 2 2h12c1.1 0 2-.9 2-2V4c0-1.1-.9-2-2-2zM6 4h5v8l-2.5-1.5L6 12V4z"/>
            </svg>
          </div>
          <div className="absolute left-16 sm:left-36 lg:left-60 top-1/3 w-6 h-6 sm:w-8 sm:h-8 lg:w-10 lg:h-10 opacity-25 animate-bounce text-purple-400" style={{ animationDelay: '0.5s', animationDuration: '4.5s' }}>
            <svg fill="currentColor" viewBox="0 0 24 24">
              <path d="M15 3H6c-.83 0-1.54.5-1.84 1.22l-3.02 7.05c-.09.23-.14.47-.14.73v2c0 1.1.9 2 2 2h6.31l-.95 4.57-.03.32c0 .41.17.79.44 1.06L9.83 23l6.59-6.59c.36-.36.58-.86.58-1.41V5c0-1.1-.9-2-2-2z"/>
            </svg>
          </div>
          <div className="absolute left-6 sm:left-16 lg:left-28 top-2/3 w-8 h-8 sm:w-10 sm:h-10 lg:w-12 lg:h-12 opacity-20 animate-bounce text-yellow-400" style={{ animationDelay: '1.5s', animationDuration: '3.8s' }}>
            <svg fill="currentColor" viewBox="0 0 24 24">
              <path d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z"/>
            </svg>
          </div>
          
          {/* Right side floating symbols */}
          <div className="absolute right-8 sm:right-16 lg:right-32 top-1/4 w-10 h-10 sm:w-12 sm:h-12 lg:w-14 lg:h-14 opacity-15 animate-bounce text-red-400" style={{ animationDelay: '2.5s', animationDuration: '3.2s' }}>
            <svg fill="currentColor" viewBox="0 0 24 24">
              <path d="M21 6H3c-1.1 0-2 .9-2 2v8c0 1.1.9 2 2 2h18c1.1 0 2-.9 2-2V8c0-1.1-.9-2-2-2zm-10 7.5v-3l4 1.5-4 1.5z"/>
            </svg>
          </div>
          <div className="absolute right-12 sm:right-32 lg:right-48 top-1/2 w-12 h-12 sm:w-14 sm:h-14 lg:w-16 lg:h-16 opacity-10 animate-bounce text-pink-400" style={{ animationDelay: '3s', animationDuration: '4.2s' }}>
            <svg fill="currentColor" viewBox="0 0 24 24">
              <path d="M12 3v10.55c-.59-.34-1.27-.55-2-.55-2.21 0-4 1.79-4 4s1.79 4 4 4 4-1.79 4-4V7h4V3h-6z"/>
            </svg>
          </div>
          <div className="absolute right-10 sm:right-24 lg:right-40 top-3/4 w-8 h-8 sm:w-10 sm:h-10 lg:w-12 lg:h-12 opacity-20 animate-bounce text-indigo-400" style={{ animationDelay: '0.8s', animationDuration: '3.7s' }}>
            <svg fill="currentColor" viewBox="0 0 24 24">
              <path d="M19 3H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm-5 14H7v-2h7v2zm3-4H7v-2h10v2zm0-4H7V7h10v2z"/>
            </svg>
          </div>
          <div className="absolute right-16 sm:right-36 lg:right-60 top-1/3 w-6 h-6 sm:w-8 sm:h-8 lg:w-10 lg:h-10 opacity-25 animate-bounce text-teal-400" style={{ animationDelay: '1.8s', animationDuration: '4.1s' }}>
            <svg fill="currentColor" viewBox="0 0 24 24">
              <path d="M9 11H7v6h2v-6zm4 0h-2v6h2v-6zm4 0h-2v6h2v-6zm2.5-5H4v16.5c0 .83.67 1.5 1.5 1.5h13c.83 0 1.5-.67 1.5-1.5V6zm-3-3V1.5c0-.83-.67-1.5-1.5-1.5h-5C10.67 0 10 .67 10 1.5V3H7.5C6.67 3 6 3.67 6 4.5V6h12V4.5c0-.83-.67-1.5-1.5-1.5z"/>
            </svg>
          </div>
          <div className="absolute right-6 sm:right-16 lg:right-28 top-2/3 w-10 h-10 sm:w-12 sm:h-12 lg:w-14 lg:h-14 opacity-15 animate-bounce text-orange-400" style={{ animationDelay: '2.2s', animationDuration: '3.9s' }}>
            <svg fill="currentColor" viewBox="0 0 24 24">
              <path d="M9.5 6.5v3h-2v-3h2zm5.5.5c0-.55-.45-1-1-1h-2v4h2c.55 0 1-.45 1-1V7zM12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1.5 12h-3v4h-1V8h4c1.1 0 2 .9 2 2v2c0 1.1-.9 2-2 2z"/>
            </svg>
          </div>
          
          {/* Background sparkles */}
          <div className="absolute left-1/4 top-1/6 w-4 h-4 sm:w-5 sm:h-5 lg:w-6 lg:h-6 opacity-10 animate-pulse text-yellow-300" style={{ animationDelay: '4s' }}>
            <svg fill="currentColor" viewBox="0 0 24 24">
              <path d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z"/>
            </svg>
          </div>
          <div className="absolute right-1/4 top-1/6 w-4 h-4 sm:w-5 sm:h-5 lg:w-6 lg:h-6 opacity-10 animate-pulse text-yellow-300" style={{ animationDelay: '5s' }}>
            <svg fill="currentColor" viewBox="0 0 24 24">
              <path d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z"/>
            </svg>
          </div>
          <div className="absolute left-1/3 bottom-1/6 w-3 h-3 sm:w-4 sm:h-4 opacity-15 animate-pulse text-blue-200" style={{ animationDelay: '6s' }}>
            <svg fill="currentColor" viewBox="0 0 24 24">
              <path d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z"/>
            </svg>
          </div>
          <div className="absolute right-1/3 bottom-1/6 w-3 h-3 sm:w-4 sm:h-4 opacity-15 animate-pulse text-blue-200" style={{ animationDelay: '7s' }}>
            <svg fill="currentColor" viewBox="0 0 24 24">
              <path d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z"/>
            </svg>
          </div>
        </div>
        
        <div className="relative z-20 text-center animate-fade-in w-full max-w-6xl mx-auto">
          <div className="text-center mb-6 sm:mb-8">
            <div className="text-5xl sm:text-6xl lg:text-7xl xl:text-8xl font-bold text-white mb-2 sm:mb-3">Your AI</div>
            <div className="text-6xl sm:text-7xl lg:text-8xl xl:text-9xl font-bold h-20 sm:h-24 flex items-center justify-center mb-2 sm:mb-3">
                <span 
                  className={`word-animation ${isAnimating ? 'word-exit' : 'word-enter'}`}
                  style={{
                    background: 'linear-gradient(45deg, #e50914, #f40612, #ff6b6b)',
                    WebkitBackgroundClip: 'text',
                    WebkitTextFillColor: 'transparent',
                    backgroundClip: 'text',
                  textShadow: '0 0 40px rgba(229, 9, 20, 0.7)',
                  filter: 'drop-shadow(0 6px 12px rgba(229, 9, 20, 0.5))'
                  }}
                >
                  {flickerWords[currentWord]}
                </span>
              </div>
            <div className="text-5xl sm:text-6xl lg:text-7xl xl:text-8xl font-bold text-white mb-6 sm:mb-8">Club</div>
          </div>
          
          <p className="text-base sm:text-lg lg:text-xl text-gray-300 mb-3 sm:mb-4 max-w-4xl mx-auto leading-relaxed px-2">
            Rate movies, shows, and books on ShowSync. Our AI reads your taste and instantly connects you 
            with people who get exactly what you love. Never feel alone while watching again.
          </p>
          
          <p className="text-sm sm:text-base text-yellow-300 mb-8 sm:mb-10 max-w-2xl mx-auto px-2">
            <span className="font-semibold">Discord meets IMDB:</span> Rate media, get matched with your taste twins, start discussing immediately
          </p>

          <div className="flex flex-col sm:flex-row gap-3 sm:gap-4 justify-center items-center mb-12 sm:mb-16 px-2">
            <button className="btn-primary text-base sm:text-lg px-6 sm:px-8 py-3 sm:py-4 ai-glow transform hover:scale-105 transition-all duration-200 w-full sm:w-auto max-w-sm">
              <span className="movie-reel w-5 h-5 sm:w-6 sm:h-6 mr-2 sm:mr-3 inline-block scale-50"></span>
              Build My Taste Profile
            </button>
            <button className="btn-secondary text-base sm:text-lg px-6 sm:px-8 py-3 sm:py-4 transform hover:scale-105 transition-all duration-200 w-full sm:w-auto max-w-sm">
              <span className="popcorn-icon mr-2 sm:mr-3"></span>
              See How It Works
            </button>
          </div>
        </div>
      </div>

      {/* Film Strip Divider */}
      <div className="film-strip h-6 sm:h-8 mb-6 sm:mb-8 opacity-60 mt-6 sm:mt-8"></div>

      {/* How ShowSync Works */}
      <div className="mb-16 mobile-py-8 mobile-px-4">
        <div className="container-responsive">
        <h2 className="text-center text-2xl sm:text-3xl lg:text-4xl font-bold mb-4 text-white">
          How ShowSync Eliminates Media Loneliness
        </h2>
        <p className="text-center text-gray-400 mb-8 sm:mb-12 text-base sm:text-lg max-w-3xl mx-auto">
          Rate what you watch and read. Our AI finds your taste twins. Connect instantly for real conversations.
        </p>

        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6 sm:gap-8">
          {/* Step 1 - Alien/Sci-Fi Theme */}
          <div className="relative p-6 sm:p-8 text-center animate-fade-in rounded-2xl overflow-hidden" style={{ 
            animationDelay: '0.2s',
            background: 'linear-gradient(135deg, #0d1b2a 0%, #1b263b 50%, #2d3748 100%)',
            border: '2px solid #00ff9f',
            boxShadow: '0 0 30px rgba(0, 255, 159, 0.3), inset 0 0 30px rgba(0, 255, 159, 0.1)'
          }}>
            {/* Sci-Fi Background Elements */}
            <div className="absolute top-4 right-4 w-10 h-10 sm:w-12 sm:h-12 opacity-20 text-cyan-400">
              <svg fill="currentColor" viewBox="0 0 24 24">
                <path d="M12 2C13.1 2 14 2.9 14 4C14 5.1 13.1 6 12 6C10.9 6 10 5.1 10 4C10 2.9 10.9 2 12 2ZM21 9V7L15 9.5V12L21 14.5V12.5L19 11.8L21 11.1ZM3 9V7L9 9.5V12L3 14.5V12.5L5 11.8L3 11.1ZM12 8C13.1 8 14 8.9 14 10V14C14 15.1 13.1 16 12 16C10.9 16 10 15.1 10 14V10C10 8.9 10.9 8 12 8ZM12 22C10.9 22 10 21.1 10 20C10 18.9 10.9 18 12 18C13.1 18 14 18.9 14 20C14 21.1 13.1 22 12 22Z"/>
              </svg>
            </div>
            <div className="absolute bottom-4 left-4 w-6 h-6 sm:w-8 sm:h-8 opacity-30 text-green-400">
              <svg fill="currentColor" viewBox="0 0 24 24">
                <path d="M12 2L13.09 8.26L22 9L16 14L18 23L12 19L6 23L8 14L2 9L10.91 8.26L12 2Z"/>
              </svg>
            </div>
            <div className="absolute top-1/2 left-2 w-5 h-5 sm:w-6 sm:h-6 opacity-25 text-cyan-300">
              <svg fill="currentColor" viewBox="0 0 24 24">
                <circle cx="12" cy="12" r="2"/>
                <circle cx="12" cy="12" r="6" fill="none" stroke="currentColor" strokeWidth="1"/>
                <circle cx="12" cy="12" r="10" fill="none" stroke="currentColor" strokeWidth="0.5"/>
              </svg>
            </div>
            
            <div className="relative mb-6">
              <div className="w-20 h-20 sm:w-24 sm:h-24 mx-auto bg-gradient-to-br from-cyan-400 to-blue-600 rounded-full flex items-center justify-center shadow-2xl border-4 border-cyan-300" style={{ boxShadow: '0 0 20px rgba(0, 255, 255, 0.5)' }}>
                {/* Alien Figure */}
                <svg className="w-10 h-10 sm:w-14 sm:h-14 text-white" fill="currentColor" viewBox="0 0 100 100">
                  {/* Alien Head */}
                  <ellipse cx="50" cy="35" rx="25" ry="20" fill="currentColor"/>
                  {/* Large Eyes */}
                  <ellipse cx="42" cy="32" rx="6" ry="8" fill="#000"/>
                  <ellipse cx="58" cy="32" rx="6" ry="8" fill="#000"/>
                  {/* Eye Highlights */}
                  <circle cx="44" cy="30" r="2" fill="white"/>
                  <circle cx="60" cy="30" r="2" fill="white"/>
                  {/* Body */}
                  <ellipse cx="50" cy="65" rx="12" ry="20" fill="currentColor"/>
                  {/* Arms */}
                  <ellipse cx="35" cy="60" rx="4" ry="12" fill="currentColor"/>
                  <ellipse cx="65" cy="60" rx="4" ry="12" fill="currentColor"/>
                  {/* Legs */}
                  <ellipse cx="45" cy="85" rx="3" ry="10" fill="currentColor"/>
                  <ellipse cx="55" cy="85" rx="3" ry="10" fill="currentColor"/>
                </svg>
              </div>
              <div className="absolute -top-2 -right-2 w-8 h-8 sm:w-10 sm:h-10 bg-green-400 rounded-full flex items-center justify-center text-xs font-bold animate-pulse border-2 border-white">
                AI
              </div>
            </div>
            <h3 className="text-xl sm:text-2xl font-bold mb-4 text-cyan-300" style={{ textShadow: '0 0 10px rgba(0, 255, 255, 0.5)' }}>
              Rate Your Universe
            </h3>
            <p className="text-sm sm:text-base text-gray-300 leading-relaxed">
              <span className="text-cyan-400 font-semibold">&quot;The truth is out there...&quot;</span> Rate sci-fi classics, space operas, and alien encounters. Our AI will decode your cosmic preferences faster than light speed!
            </p>
          </div>

          {/* Step 2 - Cowboy/Western Theme */}
          <div className="relative p-6 sm:p-8 text-center animate-fade-in rounded-2xl overflow-hidden" style={{ 
            animationDelay: '0.4s',
            background: 'linear-gradient(135deg, #8b4513 0%, #d2691e 30%, #cd853f 100%)',
            border: '2px solid #ffd700',
            boxShadow: '0 0 30px rgba(255, 215, 0, 0.4), inset 0 0 30px rgba(139, 69, 19, 0.3)'
          }}>
            {/* Western Background Elements */}
            <div className="absolute top-4 right-4 w-10 h-10 sm:w-12 sm:h-12 opacity-30 text-yellow-300">
              <svg fill="currentColor" viewBox="0 0 24 24">
                <path d="M12 2C13.1 2 14 2.9 14 4V6C14 7.1 13.1 8 12 8C10.9 8 10 7.1 10 6V4C10 2.9 10.9 2 12 2ZM12 10C13.66 10 15 8.66 15 7H16C17.1 7 18 7.9 18 9V11C18 12.1 17.1 13 16 13H8C6.9 13 6 12.1 6 11V9C6 7.9 6.9 7 8 7H9C9 8.66 10.34 10 12 10Z"/>
              </svg>
            </div>
            <div className="absolute bottom-4 left-4 w-8 h-8 sm:w-10 sm:h-10 opacity-40 text-orange-300">
              <svg fill="currentColor" viewBox="0 0 24 24">
                <path d="M20 8h-2.81c-.45-.78-1.07-1.45-1.82-1.96L17 4.41 15.59 3l-2.17 2.17C12.96 5.06 12.49 5 12 5s-.96.06-1.42.17L8.41 3 7 4.41l1.63 1.63C7.88 6.55 7.26 7.22 6.81 8H4v2h2.09c-.05.33-.09.66-.09 1v1H4v2h2v1c0 .34.04.67.09 1H4v2h2.81c1.04 1.79 2.97 3 5.19 3s4.15-1.21 5.19-3H20v-2h-2.09c.05-.33.09-.66.09-1v-1h2v-2h-2v-1c0-.34-.04-.67-.09-1H20V8z"/>
              </svg>
            </div>
            <div className="absolute top-1/2 right-2 w-5 h-5 sm:w-6 sm:h-6 opacity-25 text-yellow-400">
              <svg fill="currentColor" viewBox="0 0 24 24">
                <path d="M12 2L13.09 8.26L22 9L16 14L18 23L12 19L6 23L8 14L2 9L10.91 8.26L12 2Z"/>
              </svg>
            </div>
            <div className="absolute bottom-1/3 right-6 w-6 h-6 sm:w-8 sm:h-8 opacity-30 text-green-600">
              <svg fill="currentColor" viewBox="0 0 24 24">
                <path d="M12 2C12.74 2 13.44 2.16 14.06 2.46L14 3C14 4.1 13.1 5 12 5S10 4.1 10 3L9.94 2.46C10.56 2.16 11.26 2 12 2ZM12 7C13.1 7 14 7.9 14 9V15C14 16.1 13.1 17 12 17S10 16.1 10 15V9C10 7.9 10.9 7 12 7ZM8 9H16V22H8V9Z"/>
              </svg>
            </div>
            
            <div className="relative mb-6">
              <div className="w-20 h-20 sm:w-24 sm:h-24 mx-auto bg-gradient-to-br from-yellow-600 to-orange-700 rounded-full flex items-center justify-center shadow-2xl border-4 border-yellow-400" style={{ boxShadow: '0 0 20px rgba(255, 215, 0, 0.6)' }}>
                {/* Cowboy Silhouette */}
                <svg className="w-10 h-10 sm:w-14 sm:h-14 text-white" fill="currentColor" viewBox="0 0 100 100">
                  {/* Cowboy Hat */}
                  <ellipse cx="50" cy="25" rx="28" ry="8" fill="currentColor"/>
                  <ellipse cx="50" cy="20" rx="15" ry="10" fill="currentColor"/>
                  {/* Head */}
                  <circle cx="50" cy="35" r="12" fill="currentColor"/>
                  {/* Bandana/Neck */}
                  <polygon points="42,45 58,45 55,50 45,50" fill="currentColor"/>
                  {/* Torso */}
                  <rect x="42" y="50" width="16" height="25" fill="currentColor"/>
                  {/* Arms */}
                  <ellipse cx="35" cy="60" rx="5" ry="15" fill="currentColor"/>
                  <ellipse cx="65" cy="60" rx="5" ry="15" fill="currentColor"/>
                  {/* Legs */}
                  <rect x="45" y="75" width="4" height="20" fill="currentColor"/>
                  <rect x="51" y="75" width="4" height="20" fill="currentColor"/>
                  {/* Boots */}
                  <ellipse cx="47" cy="95" rx="6" ry="3" fill="currentColor"/>
                  <ellipse cx="53" cy="95" rx="6" ry="3" fill="currentColor"/>
                </svg>
              </div>
              <div className="absolute -top-2 -right-2 w-8 h-8 sm:w-10 sm:h-10 bg-green-500 rounded-full flex items-center justify-center text-xs font-bold animate-pulse border-2 border-white">
                SYNC
              </div>
            </div>
            <h3 className="text-xl sm:text-2xl font-bold mb-4 text-yellow-300" style={{ textShadow: '0 0 10px rgba(255, 215, 0, 0.5)' }}>
              Round Up Your Posse
            </h3>
            <p className="text-sm sm:text-base text-orange-100 leading-relaxed">
              <span className="text-yellow-300 font-semibold">&quot;There&apos;s a new sheriff in town...&quot;</span> Our AI wrangles up folks who share your taste in westerns, gritty dramas, and classic showdowns. Saddle up, partner!
            </p>
          </div>

          {/* Step 3 - Anime Theme */}
          <div className="relative p-6 sm:p-8 text-center animate-fade-in rounded-2xl overflow-hidden" style={{ 
            animationDelay: '0.6s',
            background: 'linear-gradient(135deg, #ff6b9d 0%, #c44569 30%, #f8b500 60%, #ff9ff3 100%)',
            border: '2px solid #ff1744',
            boxShadow: '0 0 30px rgba(255, 23, 68, 0.4), inset 0 0 30px rgba(255, 107, 157, 0.2)'
          }}>
            {/* Anime Background Elements */}
            <div className="absolute top-4 right-4 w-10 h-10 sm:w-12 sm:h-12 opacity-30 text-yellow-300">
              <svg fill="currentColor" viewBox="0 0 24 24">
                <path d="M11 1L13 7L11 13L9 7L11 1ZM11 23L13 17L11 11L9 17L11 23ZM1 11L7 13L13 11L7 9L1 11ZM23 11L17 13L11 11L17 9L23 11Z"/>
                <circle cx="12" cy="12" r="3" fill="currentColor"/>
              </svg>
            </div>
            <div className="absolute bottom-4 left-4 w-6 h-6 sm:w-8 sm:h-8 opacity-40 text-pink-300">
              <svg fill="currentColor" viewBox="0 0 24 24">
                <path d="M12 2C13.1 2 14 2.9 14 4C14 5.1 13.1 6 12 6C10.9 6 10 5.1 10 4C10 2.9 10.9 2 12 2ZM12 22C10.9 22 10 21.1 10 20C10 18.9 10.9 18 12 18C13.1 18 14 18.9 14 20C14 21.1 13.1 22 12 22ZM6 10C7.1 10 8 10.9 8 12C8 13.1 7.1 14 6 14C4.9 14 4 13.1 4 12C4 10.9 4.9 10 6 10ZM18 10C19.1 10 20 10.9 20 12C20 13.1 19.1 14 18 14C16.9 14 16 13.1 16 12C16 10.9 16.9 10 18 10Z"/>
              </svg>
            </div>
            <div className="absolute top-1/2 left-2 w-5 h-5 sm:w-6 sm:h-6 opacity-25 text-purple-200">
              <svg fill="currentColor" viewBox="0 0 24 24">
                <path d="M12 2L15.09 8.26L22 9L16 14L18 23L12 17L6 23L8 14L2 9L8.91 8.26L12 2Z"/>
                <circle cx="12" cy="10" r="2" fill="rgba(255,255,255,0.6)"/>
              </svg>
            </div>
            <div className="absolute bottom-1/3 right-6 w-6 h-6 sm:w-8 sm:h-8 opacity-35 text-orange-300">
              <svg fill="currentColor" viewBox="0 0 24 24">
                <path d="M12 2C17.52 2 22 6.48 22 12C22 17.52 17.52 22 12 22C6.48 22 2 17.52 2 12C2 6.48 6.48 2 12 2ZM12 4C7.58 4 4 7.58 4 12C4 16.42 7.58 20 12 20C16.42 20 20 16.42 20 12C20 7.58 16.42 4 12 4ZM12 6L14 10L18 10L15 13L16 17L12 15L8 17L9 13L6 10L10 10L12 6Z"/>
              </svg>
            </div>
            
            <div className="relative mb-6">
              <div className="w-20 h-20 sm:w-24 sm:h-24 mx-auto bg-gradient-to-br from-pink-500 to-purple-600 rounded-full flex items-center justify-center shadow-2xl border-4 border-pink-300" style={{ boxShadow: '0 0 20px rgba(255, 20, 147, 0.6)' }}>
                {/* Dragon Ball Inspired Symbol */}
                <svg className="w-10 h-10 sm:w-14 sm:h-14 text-white" fill="currentColor" viewBox="0 0 100 100">
                  {/* Main Dragon Ball Circle */}
                  <circle cx="50" cy="50" r="35" fill="orange" stroke="currentColor" strokeWidth="3"/>
                  {/* 4-Star Pattern */}
                  <circle cx="35" cy="35" r="4" fill="red"/>
                  <circle cx="65" cy="35" r="4" fill="red"/>
                  <circle cx="35" cy="65" r="4" fill="red"/>
                  <circle cx="65" cy="65" r="4" fill="red"/>
                  {/* Center Highlight */}
                  <circle cx="45" cy="45" r="8" fill="rgba(255,255,255,0.3)"/>
                  {/* Anime Character Silhouette */}
                  <g transform="translate(50, 50) scale(0.4)">
                    {/* Spiky Hair */}
                    <polygon points="-15,-25 -10,-35 -5,-30 0,-40 5,-30 10,-35 15,-25 10,-20 0,-22 -10,-20" fill="currentColor"/>
                    {/* Head */}
                    <circle cx="0" cy="-15" r="12" fill="currentColor"/>
                    {/* Body */}
                    <rect x="-8" y="-5" width="16" height="20" fill="currentColor"/>
                    {/* Arms in fighting pose */}
                    <ellipse cx="-15" cy="5" rx="4" ry="10" transform="rotate(-30)" fill="currentColor"/>
                    <ellipse cx="15" cy="5" rx="4" ry="10" transform="rotate(30)" fill="currentColor"/>
                    {/* Legs */}
                    <rect x="-4" y="15" width="3" height="15" fill="currentColor"/>
                    <rect x="1" y="15" width="3" height="15" fill="currentColor"/>
                  </g>
                </svg>
              </div>
              <div className="absolute -top-2 -right-2 w-8 h-8 sm:w-10 sm:h-10 bg-yellow-400 rounded-full flex items-center justify-center text-xs font-bold animate-pulse border-2 border-white text-black">
                NOW
              </div>
            </div>
            <h3 className="text-xl sm:text-2xl font-bold mb-4 text-white" style={{ textShadow: '0 0 10px rgba(255, 20, 147, 0.8)' }}>
              Anime Community
            </h3>
            <p className="text-sm sm:text-base text-pink-100 leading-relaxed">
              <span className="text-yellow-300 font-semibold">&quot;The power of friendship!&quot;</span> Connect with anime fans instantly! Discuss epic battles, share favorite series, and discover hidden gems. Join passionate discussions about everything from classics to seasonal hits.
            </p>
          </div>
          </div>
        </div>
      </div>

      {/* Real User Experiences - Carousel */}
      <div className="mb-20 mobile-px-4">
        <div className="container-responsive">
        <h2 className="text-center text-2xl sm:text-3xl lg:text-4xl font-bold mb-8 sm:mb-12 text-white">
          Real Stories From Real Viewers
        </h2>
        
        <div className="relative">
          {/* Navigation arrows */}
          <button 
            onClick={prevPage}
            className="absolute left-0 top-1/2 transform -translate-y-1/2 z-10 bg-gray-800 hover:bg-gray-700 rounded-full p-2 sm:p-3 transition-colors"
          >
            <svg className="w-5 h-5 sm:w-6 sm:h-6 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 19l-7-7 7-7" />
            </svg>
          </button>
          
          <button 
            onClick={nextPage}
            className="absolute right-0 top-1/2 transform -translate-y-1/2 z-10 bg-gray-800 hover:bg-gray-700 rounded-full p-2 sm:p-3 transition-colors"
          >
            <svg className="w-5 h-5 sm:w-6 sm:h-6 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5l7 7-7 7" />
            </svg>
          </button>

          {/* Testimonial Content */}
          <div className="mx-8 sm:mx-12">
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6 sm:gap-8">
              {allTestimonials[currentTestimonialPage].map((testimonial, index) => (
                <div key={index} className="glass-effect rounded-2xl p-6 sm:p-8 border border-yellow-600/30 animate-fade-in">
                  <div className="flex items-center mb-4">
                    <div className={`w-10 h-10 sm:w-12 sm:h-12 bg-gradient-to-br ${testimonial.bgColor} rounded-full flex items-center justify-center text-white font-bold mr-4`}>
                      {testimonial.initial}
                    </div>
                    <div>
                      <p className="font-semibold text-white text-sm sm:text-base">{testimonial.name}</p>
                      <p className="text-xs sm:text-sm text-gray-400">{testimonial.bio}</p>
                    </div>
                  </div>
                  <p className="text-sm sm:text-base text-gray-300 leading-relaxed mb-4">
                    &quot;{testimonial.review}&quot;
                  </p>
                  <div className="text-xs sm:text-sm text-yellow-400">
                    ⭐⭐⭐⭐⭐ &quot;{testimonial.rating}&quot;
                  </div>
                </div>
              ))}
            </div>
          </div>

          {/* Clickable indicators */}
          <div className="flex justify-center mt-8 space-x-3">
            {Array.from({ length: totalPages }).map((_, index) => (
              <button
                key={index}
                onClick={() => goToPage(index)}
                className={`w-3 h-3 rounded-full transition-all duration-200 ${
                  currentTestimonialPage === index 
                    ? 'bg-red-500 scale-125' 
                    : 'bg-gray-600 hover:bg-gray-500'
                }`}
              />
            ))}
          </div>
        </div>
        </div>
      </div>

      {/* CTA Section */}
      <div className="relative overflow-hidden py-16 sm:py-20 mb-8">
        {/* Background Elements */}
        <div className="absolute inset-0 bg-gradient-to-b from-transparent via-gray-900/50 to-black"></div>
        <div className="absolute top-1/4 left-1/4 w-32 h-32 bg-red-600/20 rounded-full blur-3xl"></div>
        <div className="absolute bottom-1/4 right-1/4 w-40 h-40 bg-red-500/10 rounded-full blur-3xl"></div>
        
        <div className="relative z-10 max-w-4xl mx-auto text-center px-4 sm:px-6">
          <div className="glass-effect rounded-2xl p-8 sm:p-12 border border-gray-700/50 shadow-2xl">
            <h3 className="text-3xl sm:text-4xl lg:text-5xl font-bold mb-6 text-white">
              Ready to Find Your <span className="gradient-text">Taste Twins</span>?
            </h3>
            <p className="text-lg sm:text-xl text-gray-300 mb-10 leading-relaxed max-w-2xl mx-auto">
              Stop watching movies and shows in isolation. Start connecting with people who actually understand your taste in media.
            </p>
            
            <div className="flex flex-col sm:flex-row gap-4 justify-center mb-8">
              <button className="btn-primary text-base sm:text-lg px-8 py-4 transform hover:scale-105 transition-all duration-200 shadow-lg shadow-red-600/25">
                <svg className="w-5 h-5 mr-2 inline-block" fill="currentColor" viewBox="0 0 24 24">
                  <path d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z"/>
                </svg>
                Start Rating & Connecting
              </button>
              <button className="btn-secondary text-base sm:text-lg px-8 py-4 transform hover:scale-105 transition-all duration-200 border border-gray-600 hover:border-gray-500">
                <svg className="w-5 h-5 mr-2 inline-block" fill="currentColor" viewBox="0 0 24 24">
                  <path d="M12 2C13.1 2 14 2.9 14 4C14 5.1 13.1 6 12 6C10.9 6 10 5.1 10 4C10 2.9 10.9 2 12 2ZM21 9V7L15 9.5V12L21 14.5V12.5L19 11.8L21 11.1ZM3 9V7L9 9.5V12L3 14.5V12.5L5 11.8L3 11.1Z"/>
                </svg>
                Browse Active Discussions
              </button>
            </div>
            
            <div className="flex flex-wrap justify-center items-center gap-3 text-sm text-gray-400">
              <span className="flex items-center gap-1">
                <div className="w-2 h-2 bg-red-500 rounded-full"></div>
                Rate media
              </span>
              <span className="text-gray-600">•</span>
              <span className="flex items-center gap-1">
                <div className="w-2 h-2 bg-red-500 rounded-full"></div>
                AI taste matching
              </span>
              <span className="text-gray-600">•</span>
              <span className="flex items-center gap-1">
                <div className="w-2 h-2 bg-red-500 rounded-full"></div>
                Instant discussion
              </span>
              <span className="text-gray-600">•</span>
              <span className="flex items-center gap-1">
                <div className="w-2 h-2 bg-red-500 rounded-full"></div>
                Never watch alone
              </span>
            </div>
          </div>
        </div>
      </div>

      {/* Film Strip Footer */}
      <div className="film-strip h-4 sm:h-6 mt-12 sm:mt-16 opacity-40"></div>
    </Layout>
  );
}
