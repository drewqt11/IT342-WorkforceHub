import React from 'react';
import { useAuth } from '../hooks/useAuth';

const DashboardPage = () => {
  const { user } = useAuth();
  const isHRAdmin = user?.role === 'HR_ADMIN';

  return (
    <div className="py-6 px-4 sm:px-6 lg:px-8">
      <div className="mb-8">
        <h1 className="text-2xl font-bold text-neutral-800">Dashboard</h1>
        <p className="mt-1 text-sm text-neutral-500">
          Welcome, {user?.firstName}! Here's your overview.
        </p>
      </div>

      <div className="grid grid-cols-1 gap-5 sm:grid-cols-2 lg:grid-cols-3">
        {/* Quick access card */}
        <div className="bg-white overflow-hidden shadow rounded-lg">
          <div className="p-5">
            <div className="flex items-center">
              <div className="flex-shrink-0 bg-primary-100 rounded-md p-3">
                <span className="text-2xl">👤</span>
              </div>
              <div className="ml-5 w-0 flex-1">
                <dl>
                  <dt className="text-sm font-medium text-neutral-500 truncate">Profile Completion</dt>
                  <dd>
                    <div className="text-lg font-medium text-neutral-800">85%</div>
                    <div className="mt-1 w-full bg-neutral-200 rounded-full h-2">
                      <div className="bg-primary-500 h-2 rounded-full" style={{ width: '85%' }}></div>
                    </div>
                  </dd>
                </dl>
              </div>
            </div>
          </div>
          <div className="bg-neutral-50 px-5 py-3">
            <div className="text-sm">
              <a href="/profile" className="font-medium text-primary-600 hover:text-primary-800">
                Complete your profile
              </a>
            </div>
          </div>
        </div>

        {/* Recent certifications */}
        <div className="bg-white overflow-hidden shadow rounded-lg">
          <div className="p-5">
            <div className="flex items-center">
              <div className="flex-shrink-0 bg-secondary-100 rounded-md p-3">
                <span className="text-2xl">🏆</span>
              </div>
              <div className="ml-5 w-0 flex-1">
                <dl>
                  <dt className="text-sm font-medium text-neutral-500 truncate">Active Certifications</dt>
                  <dd>
                    <div className="text-lg font-medium text-neutral-800">3</div>
                  </dd>
                </dl>
              </div>
            </div>
          </div>
          <div className="bg-neutral-50 px-5 py-3">
            <div className="text-sm">
              <a href="/certifications" className="font-medium text-primary-600 hover:text-primary-800">
                View all certifications
              </a>
            </div>
          </div>
        </div>

        {/* HR Admin specific card */}
        {isHRAdmin && (
          <div className="bg-white overflow-hidden shadow rounded-lg">
            <div className="p-5">
              <div className="flex items-center">
                <div className="flex-shrink-0 bg-blue-100 rounded-md p-3">
                  <span className="text-2xl">👥</span>
                </div>
                <div className="ml-5 w-0 flex-1">
                  <dl>
                    <dt className="text-sm font-medium text-neutral-500 truncate">Total Employees</dt>
                    <dd>
                      <div className="text-lg font-medium text-neutral-800">42</div>
                    </dd>
                  </dl>
                </div>
              </div>
            </div>
            <div className="bg-neutral-50 px-5 py-3">
              <div className="text-sm">
                <a href="/employees" className="font-medium text-primary-600 hover:text-primary-800">
                  View all employees
                </a>
              </div>
            </div>
          </div>
        )}
      </div>

      {/* Recent activity section */}
      <div className="mt-8">
        <h2 className="text-lg font-medium text-neutral-800 mb-4">Recent Activity</h2>
        <div className="bg-white shadow overflow-hidden sm:rounded-md">
          <ul className="divide-y divide-neutral-200">
            <li>
              <div className="px-4 py-4 sm:px-6">
                <div className="flex items-center justify-between">
                  <p className="text-sm font-medium text-primary-600 truncate">
                    Your certification was approved
                  </p>
                  <div className="ml-2 flex-shrink-0 flex">
                    <p className="px-2 inline-flex text-xs leading-5 font-semibold rounded-full bg-green-100 text-green-800">
                      Approved
                    </p>
                  </div>
                </div>
                <div className="mt-2 sm:flex sm:justify-between">
                  <div className="sm:flex">
                    <p className="flex items-center text-sm text-neutral-500">
                      Project Management Professional (PMP)
                    </p>
                  </div>
                  <div className="mt-2 flex items-center text-sm text-neutral-500 sm:mt-0">
                    <p>
                      2 days ago
                    </p>
                  </div>
                </div>
              </div>
            </li>
            <li>
              <div className="px-4 py-4 sm:px-6">
                <div className="flex items-center justify-between">
                  <p className="text-sm font-medium text-primary-600 truncate">
                    Profile updated
                  </p>
                  <div className="ml-2 flex-shrink-0 flex">
                    <p className="px-2 inline-flex text-xs leading-5 font-semibold rounded-full bg-blue-100 text-blue-800">
                      Updated
                    </p>
                  </div>
                </div>
                <div className="mt-2 sm:flex sm:justify-between">
                  <div className="sm:flex">
                    <p className="flex items-center text-sm text-neutral-500">
                      Contact information updated
                    </p>
                  </div>
                  <div className="mt-2 flex items-center text-sm text-neutral-500 sm:mt-0">
                    <p>
                      1 week ago
                    </p>
                  </div>
                </div>
              </div>
            </li>
            {isHRAdmin && (
              <li>
                <div className="px-4 py-4 sm:px-6">
                  <div className="flex items-center justify-between">
                    <p className="text-sm font-medium text-primary-600 truncate">
                      New employee enrolled
                    </p>
                    <div className="ml-2 flex-shrink-0 flex">
                      <p className="px-2 inline-flex text-xs leading-5 font-semibold rounded-full bg-yellow-100 text-yellow-800">
                        Pending
                      </p>
                    </div>
                  </div>
                  <div className="mt-2 sm:flex sm:justify-between">
                    <div className="sm:flex">
                      <p className="flex items-center text-sm text-neutral-500">
                        Jane Smith requested enrollment approval
                      </p>
                    </div>
                    <div className="mt-2 flex items-center text-sm text-neutral-500 sm:mt-0">
                      <p>
                        Just now
                      </p>
                    </div>
                  </div>
                </div>
              </li>
            )}
          </ul>
        </div>
      </div>
    </div>
  );
};

export default DashboardPage;