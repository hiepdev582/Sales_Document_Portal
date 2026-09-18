export interface User {
  id: number;
  username: string;
  fullName: string;
  role: "ROLE_STAFF" | "ROLE_MANAGER" | "ROLE_ADMIN";
  department: "SALES" | "MARKETING" | "FINANCE" | "IT" | "EXECUTIVE";
}

export const useAuth = () => {
  const accessToken = useState<string | null>("accessToken", () => null);
  const user = useState<User | null>("currentUser", () => null);
  const initialized = useState<boolean>("authInitialized", () => false);

  const setAuthData = (token: string, userData: User) => {
    accessToken.value = token;
    user.value = userData;
  };

  const clearAuthData = () => {
    accessToken.value = null;
    user.value = null;
  };

  const fetchCurrentUser = async () => {
    if (!accessToken.value) return null;
    try {
      const data = await $fetch<User>("http://localhost:8080/api/auth/me", {
        headers: {
          Authorization: `Bearer ${accessToken.value}`,
        },
        credentials: "include",
      });
      user.value = data;
      return data;
    } catch (e) {
      clearAuthData();
      console.error(e);
      return null;
    }
  };

  const refreshSession = async () => {
    try {
      const res = await $fetch<{ accessToken: string; user: User }>(
        "http://localhost:8080/api/auth/refresh",
        {
          method: "POST",
          credentials: "include",
        },
      );
      setAuthData(res.accessToken, res.user);
      return true;
    } catch (e) {
      clearAuthData();
      console.error(e);
      return false;
    } finally {
      initialized.value = true;
    }
  };

  const logout = async () => {
    try {
      await $fetch("http://localhost:8080/api/auth/logout", {
        method: "POST",
        headers: accessToken.value
          ? { Authorization: `Bearer ${accessToken.value}` }
          : {},
        credentials: "include",
      });
    } catch (e) {
      // Ignore network errors on logout
    } finally {
      clearAuthData();
      navigateTo("/login");
    }
  };

  return {
    accessToken,
    user,
    initialized,
    setAuthData,
    clearAuthData,
    fetchCurrentUser,
    refreshSession,
    logout,
  };
};
