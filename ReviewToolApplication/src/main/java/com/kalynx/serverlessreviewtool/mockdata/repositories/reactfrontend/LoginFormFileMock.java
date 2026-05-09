package com.kalynx.serverlessreviewtool.mockdata.repositories.reactfrontend;

import com.kalynx.serverlessreviewtool.mockdata.repositories.BaseRepository;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Random;

public class LoginFormFileMock extends BaseRepository {
    private static final String FILE_PATH = "src/components/LoginForm.tsx";
    private static final Random random = new Random();

    public static void create(Path repoPath) throws Exception {
        int commitCount = 15 + random.nextInt(16);
        for (int i = 0; i < commitCount; i++) {
            Files.writeString(repoPath.resolve(FILE_PATH), generate(i));
            commitFile(repoPath, FILE_PATH, getCommitMessage(i));
        }
    }

    private static String generate(int i) {
        var c = new StringBuilder();
        c.append("import React, { useState } from 'react';\n");
        if (i >= 3) c.append("import { api } from '../services/api';\n");
        c.append("\ninterface LoginFormProps { onLogin: (token: string) => void; }\n\n");
        c.append("export const LoginForm: React.FC<LoginFormProps> = ({ onLogin }) => {\n");
        c.append("  const [username, setUsername] = useState('');\n  const [password, setPassword] = useState('');\n");
        if (i >= 4) c.append("  const [error, setError] = useState<string | null>(null);\n");
        if (i >= 6) c.append("  const [isLoading, setIsLoading] = useState(false);\n");
        if (i >= 9) c.append("  const [showRegister, setShowRegister] = useState(false);\n  const [email, setEmail] = useState('');\n");
        c.append("\n  const handleSubmit = async (e: React.FormEvent) => {\n    e.preventDefault();\n");
        if (i >= 4) c.append("    setError(null);\n");
        if (i >= 6) c.append("    setIsLoading(true);\n");
        if (i >= 5) {
            c.append("\n    if (!username || !password) {\n");
            if (i >= 4) c.append("      setError('Please fill in all fields');\n");
            if (i >= 6) c.append("      setIsLoading(false);\n");
            c.append("      return;\n    }\n");
        }
        if (i >= 3) {
            c.append("\n    try {\n");
            if (i >= 9) {
                c.append("      let token: string;\n");
                c.append("      if (showRegister) {\n        await api.register(username, email, password);\n");
                c.append("        token = await api.login(username, password);\n");
                c.append("      } else {\n        token = await api.login(username, password);\n      }\n");
            } else {
                c.append("      const token = await api.login(username, password);\n");
            }
            c.append("      onLogin(token);\n    } catch (err) {\n");
            if (i >= 4) c.append("      setError(err instanceof Error ? err.message : 'Login failed');\n");
            if (i >= 7) c.append("      console.error('Login error:', err);\n");
            c.append("    }");
            if (i >= 6) c.append(" finally {\n      setIsLoading(false);\n    }");
            c.append("\n");
        } else {
            c.append("    onLogin('mock-token');\n");
        }
        c.append("  };\n\n  return (\n    <div className=\"login-form\">\n");
        if (i >= 9) {
            c.append("      <h2>{showRegister ? 'Register' : 'Login'}</h2>\n");
        } else if (i >= 2) {
            c.append("      <h2>Login</h2>\n");
        }
        if (i >= 4) c.append("      {error && <div className=\"error\">{error}</div>}\n");
        c.append("      <form onSubmit={handleSubmit}>\n");
        c.append("        <input type=\"text\" placeholder=\"Username\" value={username} onChange={(e) => setUsername(e.target.value)}");
        if (i >= 6) c.append(" disabled={isLoading}");
        c.append(" />\n");
        if (i >= 9) {
            c.append("        {showRegister && <input type=\"email\" placeholder=\"Email\" value={email} onChange={(e) => setEmail(e.target.value)}");
            if (i >= 6) c.append(" disabled={isLoading}");
            c.append(" />}\n");
        }
        c.append("        <input type=\"password\" placeholder=\"Password\" value={password} onChange={(e) => setPassword(e.target.value)}");
        if (i >= 6) c.append(" disabled={isLoading}");
        c.append(" />\n        <button type=\"submit\"");
        if (i >= 6) c.append(" disabled={isLoading}");
        c.append(">\n          ");
        if (i >= 6) {
            c.append("{isLoading ? 'Loading...' : ");
            if (i >= 9) c.append("showRegister ? 'Register' : 'Login'}");
            else c.append("'Login'}");
        } else if (i >= 9) {
            c.append("{showRegister ? 'Register' : 'Login'}");
        } else {
            c.append("Login");
        }
        c.append("\n        </button>\n");
        if (i >= 9) {
            c.append("        <button type=\"button\" onClick={() => setShowRegister(!showRegister)}");
            if (i >= 6) c.append(" disabled={isLoading}");
            c.append(">\n");
            c.append("          {showRegister ? 'Already have an account?' : 'Need an account?'}\n");
            c.append("        </button>\n");
        }
        c.append("      </form>\n    </div>\n  );\n};\n");
        return c.toString();
    }

    private static String getCommitMessage(int i) {
        return switch (i) {
            case 0 -> "feat: Create LoginForm component";
            case 1 -> "feat: Add state management";
            case 2 -> "feat: Add form title";
            case 3 -> "feat: Integrate API service";
            case 4 -> "feat: Add error handling";
            case 5 -> "feat: Add input validation";
            case 6 -> "feat: Add loading state";
            case 7 -> "feat: Add error logging";
            case 9 -> "feat: Add registration functionality";
            default -> "refactor: LoginForm improvements " + (i + 1);
        };
    }
}

