/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{js,jsx}'],
  theme: {
    extend: {
      colors: {
        paper: {
          DEFAULT: '#F5F5F7',
          50: '#FFFFFF',
          100: '#F5F5F7',
          200: '#E8E8ED',
        },
        ink: {
          DEFAULT: '#1D1D1F',
          soft: '#6E6E73',
          faint: '#86868B',
        },
        aqua: {
          50: '#FAFAFA',
          100: '#F2F2F2',
          200: '#E5E5E5',
          300: '#D2D2D7',
          400: '#A1A1A6',
          500: '#8E8E93',
          600: '#636366',
          700: '#48484A',
          800: '#2C2C2E',
          900: '#1D1D1F',
        },
        sand: {
          50: '#FAFAFA',
          100: '#F2F2F2',
          200: '#E5E5E5',
          400: '#A1A1A6',
          600: '#6E6E73',
        },
      },
      fontFamily: {
        sans: [
          '-apple-system',
          'BlinkMacSystemFont',
          'SF Pro Display',
          'SF Pro Text',
          'Manrope Variable',
          'Manrope',
          'Helvetica Neue',
          'sans-serif',
        ],
        serif: [
          '-apple-system',
          'BlinkMacSystemFont',
          'SF Pro Display',
          'Manrope Variable',
          'sans-serif',
        ],
      },
      borderRadius: {
        '4xl': '2rem',
        '5xl': '2.75rem',
      },
      boxShadow: {
        glass: '0 20px 50px -36px rgba(0, 0, 0, 0.22), inset 0 1px 0 rgba(255,255,255,0.8)',
        float: '0 18px 36px -28px rgba(0, 0, 0, 0.22)',
        soft: '0 8px 24px -20px rgba(0, 0, 0, 0.2)',
      },
      transitionTimingFunction: {
        silk: 'cubic-bezier(0.16, 1, 0.3, 1)',
      },
      keyframes: {
        drift: {
          '0%, 100%': { transform: 'translate3d(0, 0, 0) scale(1)' },
          '50%': { transform: 'translate3d(2%, -4%, 0) scale(1.08)' },
        },
        breathe: {
          '0%, 100%': { opacity: '0.55' },
          '50%': { opacity: '0.9' },
        },
      },
      animation: {
        drift: 'drift 24s ease-in-out infinite',
        breathe: 'breathe 8s ease-in-out infinite',
      },
    },
  },
  plugins: [],
}
