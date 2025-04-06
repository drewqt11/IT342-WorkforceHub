import React, { useState } from 'react';
import { NavLink, Outlet, useNavigate } from 'react-router-dom';
import { useAuth } from '../../hooks/useAuth';
import Logo from '../../assets/logo.svg';

const DashboardLayout = () => {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const [isMobileMenuOpen, setIsMobileMenuOpen] = useState(false);
  
  const isHRAdmin = user?.role === 'HR_ADMIN';
  
  const navigation = [
    { name: 'Dashboard', href: '/dashboard', icon: '🏠' },
    { name: 'Employees', href: '/employees', icon: '👥' },
    { name: 'My Profile', href: '/profile', icon: '👤' },
    { name: 'Certifications', href: '/certifications', icon: '🏆' },
  ];
  
  const adminNavigation = [
    { name: 'Departments', href: '/settings/departments', icon: '🏢' },
    { name: 'Job Titles', href: '/settings/job-titles', icon: '💼' },
    { name: 'Roles', href: '/settings/roles', icon: '🔑' },
  ];

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Mobile menu */}
      <div className="lg:hidden">
        <div className="fixed inset-0 flex z-40">
          {/* Sidebar overlay */}
          {isMobileMenuOpen && (
            <div
              className="fixed inset-0 bg-gray-800 bg-opacity-75"
              onClick={() => setIsMobileMenuOpen(false)}
            />
          )}

          {/* Sidebar */}
          <div
            className={`fixed inset-y-0 left-0 flex flex-col w-64 bg-white transition-transform duration-300 transform ${
              isMobileMenuOpen ? 'translate-x-0' : '-translate-x-full'
            }`}
          >
            <div className="flex items-center justify-between h-16 px-4 border-b border-neutral-200">
              <div className="flex items-center">
                <img
                  className="h-8 w-auto"
                  src={Logo}
                  alt="WorkforceHub"
                />
                <span className="ml-2 text-xl font-bold text-neutral-800">WorkforceHub</span>
              </div>
              <button
                onClick={() => setIsMobileMenuOpen(false)}
                className="text-neutral-500 hover:text-neutral-700"
              >
                <span className="sr-only">Close sidebar</span>
                <svg className="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                </svg>
              </button>
            </div>

            <div className="flex-1 overflow-y-auto">
              <nav className="px-2 py-4 space-y-1">
                {navigation.map((item) => (
                  <NavLink
                    key={item.name}
                    to={item.href}
                    className={({ isActive }) =>
                      `flex items-center px-4 py-2 text-sm font-medium rounded-md ${
                        isActive
                          ? 'bg-primary-100 text-primary-700'
                          : 'text-neutral-500 hover:bg-neutral-100'
                      }`
                    }
                    onClick={() => setIsMobileMenuOpen(false)}
                  >
                    <span className="mr-3">{item.icon}</span>
                    {item.name}
                  </NavLink>
                ))}

                {isHRAdmin && (
                  <div className="pt-4 mt-4 border-t border-neutral-200">
                    <h3 className="px-4 text-xs font-semibold text-neutral-500 uppercase tracking-wider">
                      Admin Settings
                    </h3>
                    <div className="mt-2 space-y-1">
                      {adminNavigation.map((item) => (
                        <NavLink
                          key={item.name}
                          to={item.href}
                          className={({ isActive }) =>
                            `flex items-center px-4 py-2 text-sm font-medium rounded-md ${
                              isActive
                                ? 'bg-primary-100 text-primary-700'
                                : 'text-neutral-500 hover:bg-neutral-100'
                            }`
                          }
                          onClick={() => setIsMobileMenuOpen(false)}
                        >
                          <span className="mr-3">{item.icon}</span>
                          {item.name}
                        </NavLink>
                      ))}
                    </div>
                  </div>
                )}
              </nav>
            </div>

            <div className="flex items-center p-4 border-t border-neutral-200">
              <div className="flex-shrink-0">
                <div className="h-10 w-10 rounded-full bg-primary-200 flex items-center justify-center text-primary-700 font-bold">
                  {user?.firstName?.charAt(0)}{user?.lastName?.charAt(0)}
                </div>
              </div>
              <div className="ml-3">
                <p className="text-sm font-medium text-neutral-800">
                  {user?.firstName} {user?.lastName}
                </p>
                <button
                  onClick={logout}
                  className="text-sm text-neutral-500 hover:text-neutral-700"
                >
                  Sign out
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Desktop sidebar */}
      <div className="hidden lg:flex lg:flex-col lg:w-64 lg:fixed lg:inset-y-0 lg:border-r lg:border-neutral-200 lg:bg-white">
        <div className="flex items-center h-16 px-4 border-b border-neutral-200">
          <img
            className="h-8 w-auto"
            src={Logo}
            alt="WorkforceHub"
          />
          <span className="ml-2 text-xl font-bold text-neutral-800">WorkforceHub</span>
        </div>

        <div className="flex-1 overflow-y-auto">
          <nav className="px-2 py-4 space-y-1">
            {navigation.map((item) => (
              <NavLink
                key={item.name}
                to={item.href}
                className={({ isActive }) =>
                  `flex items-center px-4 py-2 text-sm font-medium rounded-md ${
                    isActive
                      ? 'bg-primary-100 text-primary-700'
                      : 'text-neutral-500 hover:bg-neutral-100'
                  }`
                }
              >
                <span className="mr-3">{item.icon}</span>
                {item.name}
              </NavLink>
            ))}

            {isHRAdmin && (
              <div className="pt-4 mt-4 border-t border-neutral-200">
                <h3 className="px-4 text-xs font-semibold text-neutral-500 uppercase tracking-wider">
                  Admin Settings
                </h3>
                <div className="mt-2 space-y-1">
                  {adminNavigation.map((item) => (
                    <NavLink
                      key={item.name}
                      to={item.href}
                      className={({ isActive }) =>
                        `flex items-center px-4 py-2 text-sm font-medium rounded-md ${
                          isActive
                            ? 'bg-primary-100 text-primary-700'
                            : 'text-neutral-500 hover:bg-neutral-100'
                        }`
                      }
                    >
                      <span className="mr-3">{item.icon}</span>
                      {item.name}
                    </NavLink>
                  ))}
                </div>
              </div>
            )}
          </nav>
        </div>

        <div className="flex items-center p-4 border-t border-neutral-200">
          <div className="flex-shrink-0">
            <div className="h-10 w-10 rounded-full bg-primary-200 flex items-center justify-center text-primary-700 font-bold">
              {user?.firstName?.charAt(0)}{user?.lastName?.charAt(0)}
            </div>
          </div>
          <div className="ml-3">
            <p className="text-sm font-medium text-neutral-800">
              {user?.firstName} {user?.lastName}
            </p>
            <button
              onClick={logout}
              className="text-sm text-neutral-500 hover:text-neutral-700"
            >
              Sign out
            </button>
          </div>
        </div>
      </div>

      {/* Content */}
      <div className="lg:pl-64">
        {/* Top bar */}
        <div className="sticky top-0 z-10 flex items-center justify-between h-16 px-4 bg-white border-b border-neutral-200 lg:hidden">
          <button
            onClick={() => setIsMobileMenuOpen(true)}
            className="text-neutral-500 hover:text-neutral-700"
          >
            <span className="sr-only">Open sidebar</span>
            <svg className="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 6h16M4 12h16M4 18h16" />
            </svg>
          </button>
          <div className="flex items-center">
            <img
              className="h-8 w-auto"
              src={Logo}
              alt="WorkforceHub"
            />
            <span className="ml-2 text-xl font-bold text-neutral-800">WorkforceHub</span>
          </div>
          <div className="flex items-center">
            <div 
              onClick={() => navigate('/profile')}
              className="h-8 w-8 rounded-full bg-primary-200 flex items-center justify-center text-primary-700 font-bold cursor-pointer"
            >
              {user?.firstName?.charAt(0)}{user?.lastName?.charAt(0)}
            </div>
          </div>
        </div>

        {/* Main content */}
        <main className="flex-1">
          <Outlet />
        </main>
      </div>
    </div>
  );
};

export default DashboardLayout; 