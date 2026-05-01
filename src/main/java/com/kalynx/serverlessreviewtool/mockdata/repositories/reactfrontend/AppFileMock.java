package com.kalynx.serverlessreviewtool.mockdata.repositories.reactfrontend;

import com.kalynx.serverlessreviewtool.mockdata.repositories.BaseRepository;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Random;

public class AppFileMock extends BaseRepository {
    private static final String FILE_PATH = "src/App.tsx";
    private static final Random random = new Random();

    public static void create(Path repoPath) throws Exception {
        int commitCount = 15 + random.nextInt(26);
        for (int i = 0; i < commitCount; i++) {
            Files.writeString(repoPath.resolve(FILE_PATH), generate(i));
            commitFile(repoPath, FILE_PATH, getCommitMessage(i));
        }
    }

    private static String generate(int i) {
        var c = new StringBuilder();
        c.append("import React");
        if (i >= 3) c.append(", { useState, useEffect }");
        c.append(" from 'react';\n");
        if (i >= 4) c.append("import { LoginForm } from './components/LoginForm';\n");
        if (i >= 6) c.append("import { UserList } from './components/UserList';\n");
        if (i >= 8) c.append("import { api } from './services/api';\n");
        if (i >= 1) c.append("import './App.css';\n");
        c.append("\n");
        if (i >= 11) c.append("interface User { id: number; username: string; email: string; }\n\n");
        c.append("function App() {\n");
        if (i >= 3) c.append("  const [isAuthenticated, setIsAuthenticated] = useState(false);\n");
        if (i >= 7) c.append("  const [token, setToken] = useState<string | null>(null);\n");
        if (i >= 11) c.append("  const [currentUser, setCurrentUser] = useState<User | null>(null);\n");
        c.append("\n");
        if (i >= 9) {
            c.append("  useEffect(() => {\n    const savedToken = localStorage.getItem('authToken');\n");
            c.append("    if (savedToken) {\n      setToken(savedToken);\n      setIsAuthenticated(true);\n");
            if (i >= 12) c.append("      loadCurrentUser(savedToken);\n");
            c.append("    }\n  }, []);\n\n");
        }
        if (i >= 5) {
            c.append("  const handleLogin = (token: string) => {\n");
            if (i >= 7) c.append("    setToken(token);\n    localStorage.setItem('authToken', token);\n");
            c.append("    setIsAuthenticated(true);\n");
            if (i >= 12) c.append("    loadCurrentUser(token);\n");
            c.append("  };\n\n");
        }
        if (i >= 10) {
            c.append("  const handleLogout = () => {\n    setIsAuthenticated(false);\n    setToken(null);\n");
            if (i >= 11) c.append("    setCurrentUser(null);\n");
            c.append("    localStorage.removeItem('authToken');\n  };\n\n");
        }
        if (i >= 12) {
            c.append("  const loadCurrentUser = async (authToken: string) => {\n    try {\n");
            c.append("      const user = await api.getCurrentUser(authToken);\n      setCurrentUser(user);\n");
            c.append("    } catch (error) {\n      console.error('Failed to load user:', error);\n");
            if (i >= 13) c.append("      handleLogout();\n");
            c.append("    }\n  };\n\n");
        }
        c.append("  return (\n    <div className=\"App\">\n");
        if (i >= 2) {
            c.append("      <header className=\"App-header\">\n        <h1>");
            c.append(i >= 11 ? "User Management System" : "Welcome to the App");
            c.append("</h1>\n");
            if (i >= 10) c.append("        {isAuthenticated && <button onClick={handleLogout}>Logout</button>}\n");
            c.append("      </header>\n");
        }
        if (i >= 3) {
            c.append("      <main>\n");
            if (i >= 4) {
                c.append("        {!isAuthenticated ? (\n          <LoginForm onLogin={handleLogin} />\n        ) : (\n");
                if (i >= 6) {
                    c.append("          <div>\n");
                    if (i >= 14) {
                        c.append("            {currentUser && (\n              <div className=\"user-profile\">\n");
                        c.append("                <p>Welcome, {currentUser.username}!</p>\n");
                        c.append("                <p>Email: {currentUser.email}</p>\n");
                        c.append("              </div>\n            )}\n");
                    } else if (i >= 11) {
                        c.append("            <p>Welcome, {currentUser?.username || 'User'}!</p>\n");
                    }
                    c.append("            <UserList");
                    if (i >= 8) c.append(" token={token}");
                    c.append(" />\n          </div>\n");
                } else {
                    c.append("          <p>You are logged in!</p>\n");
                }
                c.append("        )}\n");
            } else {
                c.append("        <p>Content goes here</p>\n");
            }
            c.append("      </main>\n");
        } else {
            c.append("      <p>Hello World</p>\n");
        }
        c.append("    </div>\n  );\n}\n\nexport default App;\n");
        return c.toString();
    }

    private static String getCommitMessage(int i) {
        return switch (i) {
            case 0 -> "feat: Create initial App component";
            case 1 -> "feat: Add CSS import";
            case 2 -> "feat: Add header section";
            case 3 -> "feat: Add authentication state";
            case 4 -> "feat: Integrate LoginForm";
            case 5 -> "feat: Add handleLogin";
            case 6 -> "feat: Show UserList when authenticated";
            case 7 -> "feat: Add token management";
            case 8 -> "feat: Pass token to UserList";
            case 9 -> "feat: Load token from localStorage";
            case 10 -> "feat: Add logout";
            case 11 -> "feat: Add current user state";
            case 12 -> "feat: Load current user on login";
            case 13 -> "fix: Logout on user load failure";
            case 14 -> "refactor: Improve user profile display";
            default -> "refactor: App improvements " + (i + 1);
        };
    }
}

