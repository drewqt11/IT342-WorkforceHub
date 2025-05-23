const API_URL = process.env.NEXT_PUBLIC_API_URL;

export const defaultFetchOptions = {
    credentials: 'include' as const,
    headers: {
        'Content-Type': 'application/json',
        'Accept': 'application/json',
    },
};

export interface LoginCredentials {
    email: string;
    password: string;
}

export interface SignUpData {
    firstName: string;
    lastName: string;
    email: string;
    password: string;
}

export interface AuthResponse {
    token: string;
    refreshToken: string;
    userId: string;
    email: string;
    role: string;
    employeeId: string;
    firstName: string;
    lastName: string;
    createdAt: string;
}

export const authService = {
    async login(credentials: LoginCredentials): Promise<AuthResponse> {
        const response = await fetch(`${API_URL}/auth/login`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(credentials),
        });

        if (!response.ok) {
            throw new Error('Login failed');
        }

        const data = await response.json();
        this.setTokens(data.token, data.refreshToken);
        return data;
    },

    async signUp(data: SignUpData): Promise<AuthResponse> {
        const response = await fetch(`${API_URL}/auth/register`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(data),
        });

        if (!response.ok) {
            throw new Error('Sign up failed');
        }

        const responseData = await response.json();
        this.setTokens(responseData.token, responseData.refreshToken);
        return responseData;
    },

    async loginWithMicrosoft() {
        window.location.href = `${API_URL}/oauth2/authorization/microsoft`;
    },

    async getOAuth2UserInfo() {
        const token = this.getToken();
        if (!token) {
            throw new Error('No authentication token found');
        }

        const response = await fetch(`${API_URL}/auth/oauth2/user-info`, {
            headers: {
                'Authorization': `Bearer ${token}`,
            },
        });

        if (!response.ok) {
            if (response.status === 401) {
                this.clearTokens();
                window.location.href = '/';
                throw new Error('Session expired');
            }
            throw new Error('Failed to fetch OAuth2 user info');
        }

        return response.json();
    },

    async getOAuth2TokenInfo(email: string): Promise<AuthResponse> {
        const response = await fetch(`${API_URL}/auth/oauth2/token-info/${email}`, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json',
            },
        });

        if (!response.ok) {
            throw new Error('Failed to get OAuth2 token info');
        }

        const data = await response.json();
        this.setTokens(data.token, data.refreshToken);
        return data;
    },

    async getEmployeeDashboard() {
        const token = this.getToken();
        if (!token) {
            window.location.href = '/';
            throw new Error('No authentication token found');
        }

        const response = await fetch(`${API_URL}/dashboard/employee`, {
            headers: {
                'Authorization': `Bearer ${token}`,
            },
        });

        if (!response.ok) {
            if (response.status === 401) {
                this.clearTokens();
                window.location.href = '/';
                throw new Error('Session expired');
            }
            throw new Error('Failed to fetch employee dashboard');
        }

        return response.json();
    },

    async getAdminDashboard() {
        const token = this.getToken();
        if (!token) {
            window.location.href = '/';
            throw new Error('No authentication token found');
        }

        const response = await fetch(`${API_URL}/dashboard/admin`, {
            headers: {
                'Authorization': `Bearer ${token}`,
            },
        });

        if (!response.ok) {
            if (response.status === 401) {
                this.clearTokens();
                window.location.href = '/';
                throw new Error('Session expired');
            }
            throw new Error('Failed to fetch admin dashboard');
        }

        return response.json();
    },

    async getEmployeeProfile() {
        const token = this.getToken();
        if (!token) {
            throw new Error('No authentication token found');
        }

        try {
            const response = await fetch(`${API_URL}/employee/profile`, {
                method: 'GET',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${token}`,
                },
            });

            if (!response.ok) {
                if (response.status === 401) {
                    this.clearTokens();
                    window.location.href = '/';
                    throw new Error('Session expired. Please log in again.');
                }
                throw new Error(`Failed to fetch employee profile: ${response.status} ${response.statusText}`);
            }

            return response.json();
        } catch (error) {
            if (error instanceof TypeError && error.message === 'Failed to fetch') {
                console.error('Network error details:', error);
                throw new Error('Unable to connect to the server. Please ensure the backend server is running at http://localhost:8081');
            }
            throw error;
        }
    },

    async refreshToken(): Promise<string> {
        const refreshToken = this.getRefreshToken();
        if (!refreshToken) {
            throw new Error('No refresh token found');
        }

        const response = await fetch(`${API_URL}/auth/refresh-token`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({ refreshToken }),
        });

        if (!response.ok) {
            this.clearTokens();
            throw new Error('Failed to refresh token');
        }

        const data = await response.json();
        this.setTokens(data.token, data.refreshToken);
        return data.token;
    },

    setTokens(token: string, refreshToken: string) {
        document.cookie = `token=${token}; path=/; secure; samesite=strict`;
        document.cookie = `refreshToken=${refreshToken}; path=/; secure; samesite=strict`;
    },

    getToken(): string | null {
        return document.cookie.split('; ').find(row => row.startsWith('token='))?.split('=')[1] || null;
    },

    getRefreshToken(): string | null {
        return document.cookie.split('; ').find(row => row.startsWith('refreshToken='))?.split('=')[1] || null;
    },

    clearTokens() {
        document.cookie = 'token=; path=/; expires=Thu, 01 Jan 1970 00:00:00 GMT';
        document.cookie = 'refreshToken=; path=/; expires=Thu, 01 Jan 1970 00:00:00 GMT';
    },

    clearAllCookies() {
        const cookies = document.cookie.split(';');
        cookies.forEach(cookie => {
            const eqPos = cookie.indexOf('=');
            const name = eqPos > -1 ? cookie.substr(0, eqPos).trim() : cookie.trim();
            document.cookie = `${name}=;expires=Thu, 01 Jan 1970 00:00:00 GMT;path=/`;
            document.cookie = `${name}=;expires=Thu, 01 Jan 1970 00:00:00 GMT;path=/;domain=${window.location.hostname}`;
            document.cookie = `${name}=;expires=Thu, 01 Jan 1970 00:00:00 GMT;path=/;domain=.${window.location.hostname}`;
        });
    },

    async logout() {
        const token = this.getToken();
        if (token) {
            try {
                const jwt = token.replace(/^Bearer /, "");
                const payload = JSON.parse(Buffer.from(jwt.split('.')[1], 'base64').toString());
                const userId = payload.userId;

                await fetch(`${API_URL}/auth/logout?userId=${userId}`, {
                    method: 'POST',
                    headers: {
                        'Authorization': `Bearer ${token}`,
                    },
                });
            } catch (error) {
                console.error('Logout error:', error);
            }
        }
        this.clearTokens();
        this.clearAllCookies();
        localStorage.clear();
        window.location.href = '/';
    },

    async getLastLogin(email: string): Promise<string> {
        const token = this.getToken();
        if (!token) {
            throw new Error('No authentication token found');
        }

        const response = await fetch(`${API_URL}/hr/user-accounts/${email}/last-login`, {
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json',
            },
        });

        if (!response.ok) {
            throw new Error('Failed to fetch last login time');
        }

        return response.json();
    },

    async getActiveStatus(email: string): Promise<boolean> {
        const token = this.getToken();
        if (!token) {
            throw new Error('No authentication token found');
        }

        const response = await fetch(`${API_URL}/hr/user-accounts/${email}/active-status`, {
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json',
            },
        });

        if (!response.ok) {
            throw new Error('Failed to fetch active status');
        }

        return response.json();
    },
}; 