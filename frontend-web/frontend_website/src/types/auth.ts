export interface LoginCredentials {
  email: string;
  password: string;
}

export interface RegisterData {
  firstName: string;
  lastName: string;
  email: string;
  password: string;
  confirmPassword: string;
}

export interface AuthResponse {
  token: string;
  userId: string;
  emailAddress: string;
  role: string;
  employeeId?: string;
  firstName: string;
  lastName: string;
}

export interface User {
  userId: string;
  email: string;
  role: string;
  employeeId?: string;
  firstName: string;
  lastName: string;
}

export interface AuthContextType {
  user: User | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  isError: boolean;
  errorMessage: string;
  login: (credentials: LoginCredentials) => Promise<void>;
  register: (data: RegisterData) => Promise<void>;
  logout: () => void;
  handleOAuthLogin: () => void;
  setUser: (user: User | null) => void;
  setIsLoading: (isLoading: boolean) => void;
} 