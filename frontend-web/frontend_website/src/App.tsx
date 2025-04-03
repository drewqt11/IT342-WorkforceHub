import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from './components/auth/AuthContext';
import Navbar from './components/layout/Navbar';
import LoginForm from './components/auth/LoginForm';
import RegisterForm from './components/auth/RegisterForm';
import Dashboard from './pages/Dashboard';
import LandingPage from './pages/LandingPage';
import OAuth2RedirectHandler from './components/auth/OAuth2RedirectHandler';

// Protected route component
const ProtectedRoute = ({ children }: { children: React.ReactNode }) => {
  const isAuthenticated = localStorage.getItem('token') !== null;
  return isAuthenticated ? <>{children}</> : <Navigate to="/login" />;
};

function App() {
  return (
    <Router>
      <AuthProvider>
        <div className="min-h-screen bg-gray-50">
          <Navbar />
          <main>
            <Routes>
              {/* Public routes */}
              <Route path="/" element={<Navigate to="/dashboard" />} />
              <Route path="/login" element={<LoginForm />} />
              <Route path="/register" element={<RegisterForm />} />
              <Route path="/oauth2/redirect" element={<OAuth2RedirectHandler />} />
              
              {/* Protected routes */}
              <Route 
                path="/dashboard" 
                element={
                  <ProtectedRoute>
                    <Dashboard />
                  </ProtectedRoute>
                } 
              />
              <Route 
                path="/landing" 
                element={
                  <ProtectedRoute>
                    <LandingPage />
                  </ProtectedRoute>
                } 
              />
              
              {/* Catch-all redirect to dashboard */}
              <Route path="*" element={<Navigate to="/dashboard" />} />
            </Routes>
          </main>
        </div>
      </AuthProvider>
    </Router>
  );
}

export default App;
