package com.kalynx.serverlessreviewtool.mockdata.repositories.reactfrontend;

import com.kalynx.serverlessreviewtool.mockdata.repositories.BaseRepository;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Random;

public class ApiFileMock extends BaseRepository {
    private static final String FILE_PATH = "src/services/api.ts";
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
        if (i >= 1) {
            c.append("const API_BASE_URL = ");
            if (i >= 8) c.append("process.env.REACT_APP_API_URL || 'http://localhost:5000/api'");
            else c.append("'http://localhost:5000/api'");
            c.append(";\n\n");
        }
        if (i >= 6) c.append("interface ApiError extends Error { status?: number; }\n\n");
        if (i >= 4) {
            c.append("const handleResponse = async (response: Response) => {\n");
            c.append("  if (!response.ok) {\n");
            if (i >= 6) {
                c.append("    const error: ApiError = new Error('API request failed');\n");
                c.append("    error.status = response.status;\n");
            } else {
                c.append("    const error = new Error('API request failed');\n");
            }
            if (i >= 7) {
                c.append("    try {\n      const errorData = await response.json();\n");
                c.append("      error.message = errorData.error || errorData.message || 'Unknown error';\n");
                c.append("    } catch { error.message = response.statusText; }\n");
            }
            c.append("    throw error;\n  }\n  return response.json();\n};\n\n");
        }
        c.append("export const api = {\n");
        c.append("  login: async (username: string, password: string): Promise<string> => {\n");
        c.append("    const response = await fetch(`").append(i >= 1 ? "${API_BASE_URL}" : "/api").append("/auth/login`, {\n");
        c.append("      method: 'POST',\n      headers: { 'Content-Type': 'application/json' },\n");
        c.append("      body: JSON.stringify({ username, password })\n    });\n");
        if (i >= 4) {
            c.append("    const data = await handleResponse(response);\n");
        } else if (i >= 2) {
            c.append("    if (!response.ok) throw new Error('Login failed');\n");
            c.append("    const data = await response.json();\n");
        } else {
            c.append("    const data = await response.json();\n");
        }
        c.append("    return data.token;\n  },\n\n");
        if (i >= 9) {
            c.append("  register: async (username: string, email: string, password: string): Promise<void> => {\n");
            c.append("    const response = await fetch(`${API_BASE_URL}/auth/register`, {\n");
            c.append("      method: 'POST',\n      headers: { 'Content-Type': 'application/json' },\n");
            c.append("      body: JSON.stringify({ username, email, password })\n    });\n");
            c.append("    await handleResponse(response);\n  },\n\n");
        }
        if (i >= 3) {
            c.append("  getUsers: async (token: string): Promise<any[]> => {\n");
            c.append("    const response = await fetch(`").append(i >= 1 ? "${API_BASE_URL}" : "/api").append("/users`, {\n");
            if (i >= 5) {
                c.append("      headers: { 'Content-Type': 'application/json', 'Authorization': `Bearer ${token}` }\n");
            }
            c.append("    });\n");
            if (i >= 4) {
                c.append("    return handleResponse(response);\n");
            } else if (i >= 2) {
                c.append("    if (!response.ok) throw new Error('Failed to fetch users');\n");
                c.append("    return response.json();\n");
            } else {
                c.append("    return response.json();\n");
            }
            c.append("  },\n\n");
        }
        if (i >= 10) {
            c.append("  getCurrentUser: async (token: string): Promise<any> => {\n");
            c.append("    const response = await fetch(`${API_BASE_URL}/users/me`, {\n");
            c.append("      headers: { 'Content-Type': 'application/json', 'Authorization': `Bearer ${token}` }\n");
            c.append("    });\n    return handleResponse(response);\n  },\n\n");
        }
        if (i >= 11) {
            c.append("  updateUser: async (token: string, userId: number, data: any): Promise<any> => {\n");
            c.append("    const response = await fetch(`${API_BASE_URL}/users/${userId}`, {\n");
            c.append("      method: 'PUT',\n      headers: { 'Content-Type': 'application/json', 'Authorization': `Bearer ${token}` },\n");
            c.append("      body: JSON.stringify(data)\n    });\n    return handleResponse(response);\n  },\n\n");
        }
        if (i >= 12) {
            c.append("  deleteUser: async (token: string, userId: number): Promise<void> => {\n");
            c.append("    const response = await fetch(`${API_BASE_URL}/users/${userId}`, {\n");
            c.append("      method: 'DELETE',\n      headers: { 'Authorization': `Bearer ${token}` }\n");
            c.append("    });\n    await handleResponse(response);\n  },\n\n");
        }
        if (i >= 13) {
            c.append("  refreshToken: async (token: string): Promise<string> => {\n");
            c.append("    const response = await fetch(`${API_BASE_URL}/auth/refresh`, {\n");
            c.append("      method: 'POST',\n      headers: { 'Authorization': `Bearer ${token}` }\n");
            c.append("    });\n    const data = await handleResponse(response);\n");
            c.append("    return data.token;\n  }\n");
        }
        c.append("};\n");
        return c.toString();
    }

    private static String getCommitMessage(int i) {
        return switch (i) {
            case 0 -> "feat: Create initial API service";
            case 1 -> "feat: Add API base URL";
            case 2 -> "feat: Add basic error handling";
            case 3 -> "feat: Add getUsers endpoint";
            case 4 -> "refactor: Extract handleResponse";
            case 5 -> "feat: Add authorization header";
            case 6 -> "feat: Add ApiError interface";
            case 7 -> "refactor: Improve error extraction";
            case 8 -> "feat: Use environment variable for URL";
            case 9 -> "feat: Add register endpoint";
            case 10 -> "feat: Add getCurrentUser endpoint";
            case 11 -> "feat: Add updateUser endpoint";
            case 12 -> "feat: Add deleteUser endpoint";
            case 13 -> "feat: Add token refresh endpoint";
            default -> "refactor: API improvements " + (i + 1);
        };
    }
}

